package com.NickSishchuck.StezhkaBot.service;

import com.NickSishchuck.StezhkaBot.entity.ConsultationRequest;
import com.NickSishchuck.StezhkaBot.repository.ConsultationRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConsultationService {

    private static final Logger logger = LoggerFactory.getLogger(ConsultationService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final ConsultationRequestRepository consultationRepository;

    // Temporary storage for consultation forms in progress
    private final Map<Long, ConsultationFormState> activeConsultations = new ConcurrentHashMap<>();

    @Autowired
    public ConsultationService(ConsultationRequestRepository consultationRepository) {
        this.consultationRepository = consultationRepository;
    }

    /**
     * Start new consultation request
     */
    public void startConsultation(Long userId) {
        ConsultationFormState state = new ConsultationFormState();
        state.setCurrentStep(ConsultationStep.NAME);
        state.setStartedAt(LocalDateTime.now());

        activeConsultations.put(userId, state);
        logger.info("Started consultation request for user {}", userId);
    }

    /**
     * Get current consultation state for user
     */
    public ConsultationFormState getConsultationState(Long userId) {
        return activeConsultations.get(userId);
    }

    /**
     * Process user input for consultation
     */
    public ConsultationStep processConsultationInput(Long userId, String input) {
        ConsultationFormState state = activeConsultations.get(userId);
        if (state == null) {
            return null;
        }

        switch (state.getCurrentStep()) {
            case NAME -> {
                state.setName(input.trim());
                state.setCurrentStep(ConsultationStep.PHONE);
            }
            case PHONE -> {
                state.setPhone(input.trim());
                state.setCurrentStep(ConsultationStep.CONFIRM);
            }
        }

        return state.getCurrentStep();
    }

    /**
     * Complete consultation request and save to database
     */
    @Transactional
    public ConsultationRequest completeConsultation(Long userId) {
        ConsultationFormState state = activeConsultations.get(userId);
        if (state == null) {
            return null;
        }

        // Create consultation request
        ConsultationRequest request = new ConsultationRequest(
                state.getName(),
                state.getPhone(),
                userId
        );

        // Save to database
        ConsultationRequest saved = consultationRepository.save(request);

        // Clean up temporary state
        activeConsultations.remove(userId);

        logger.info("Completed consultation request for user {} - Request ID: {}", userId, saved.getId());

        return saved;
    }

    /**
     * Cancel consultation request
     */
    public void cancelConsultation(Long userId) {
        activeConsultations.remove(userId);
        logger.info("Cancelled consultation request for user {}", userId);
    }

    /**
     * Check for recent duplicate consultation request
     */
    public boolean hasRecentConsultation(Long userId, int hoursAgo) {
        LocalDateTime since = LocalDateTime.now().minusHours(hoursAgo);
        Optional<ConsultationRequest> recent = consultationRepository
                .findFirstByTelegramUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, since);
        return recent.isPresent();
    }

    /**
     * Get all unprocessed consultation requests
     */
    public List<ConsultationRequest> getUnprocessedRequests() {
        return consultationRepository.findByStatusOrderByCreatedAtDesc(ConsultationRequest.ConsultationStatus.NEW);
    }

    /**
     * Mark request as processed
     */
    @Transactional
    public boolean markAsProcessed(Long requestId, Long adminUserId) {
        Optional<ConsultationRequest> requestOpt = consultationRepository.findById(requestId);
        if (requestOpt.isPresent()) {
            ConsultationRequest request = requestOpt.get();
            request.setStatus(ConsultationRequest.ConsultationStatus.PROCESSED);
            request.setProcessedAt(LocalDateTime.now());
            request.setProcessedBy(adminUserId);
            consultationRepository.save(request);
            logger.info("Consultation request {} marked as processed by admin {}", requestId, adminUserId);
            return true;
        }
        return false;
    }

    /**
     * Mark request as processed by admin message ID
     */
    @Transactional
    public boolean markAsProcessedByMessageId(Integer messageId, Long adminUserId) {
        Optional<ConsultationRequest> requestOpt = consultationRepository.findByAdminMessageId(messageId);
        if (requestOpt.isPresent()) {
            ConsultationRequest request = requestOpt.get();
            request.setStatus(ConsultationRequest.ConsultationStatus.PROCESSED);
            request.setProcessedAt(LocalDateTime.now());
            request.setProcessedBy(adminUserId);
            consultationRepository.save(request);
            logger.info("Consultation request {} marked as processed by admin {} via message {}",
                    request.getId(), adminUserId, messageId);
            return true;
        }
        return false;
    }

    /**
     * Update admin message ID for request
     */
    @Transactional
    public void updateAdminMessageId(Long requestId, Integer messageId) {
        consultationRepository.findById(requestId).ifPresent(request -> {
            request.setAdminMessageId(messageId);
            consultationRepository.save(request);
        });
    }

    /**
     * Get consultation statistics
     */
    public long getUnprocessedConsultationsCount() {
        return consultationRepository.countByStatus(ConsultationRequest.ConsultationStatus.NEW);
    }

    /**
     * Clean up old consultation states (called periodically)
     */
    public void cleanupAbandonedConsultations() {
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(30);
        activeConsultations.entrySet().removeIf(entry ->
                entry.getValue().getStartedAt().isBefore(timeout)
        );
    }

    /**
     * Format consultation request for admin notification
     */
    public String formatRequestForAdmin(ConsultationRequest request) {
        return String.format(
                "🆕 *Нова заявка на консультацію!*\n\n" +
                        "👤 *Ім'я:* %s\n" +
                        "📞 *Телефон:* %s\n\n" +
                        "📅 *Дата:* %s\n" +
                        "🆔 *ID заявки:* #%d",
                request.getName(),
                request.getPhone(),
                request.getCreatedAt().format(DATE_FORMATTER),
                request.getId()
        );
    }

    /**
     * Consultation form state
     */
    public static class ConsultationFormState {
        private String name;
        private String phone;
        private ConsultationStep currentStep;
        private LocalDateTime startedAt;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public ConsultationStep getCurrentStep() { return currentStep; }
        public void setCurrentStep(ConsultationStep currentStep) { this.currentStep = currentStep; }

        public LocalDateTime getStartedAt() { return startedAt; }
        public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    }

    /**
     * Consultation steps enum
     */
    public enum ConsultationStep {
        NAME,
        PHONE,
        CONFIRM
    }
}