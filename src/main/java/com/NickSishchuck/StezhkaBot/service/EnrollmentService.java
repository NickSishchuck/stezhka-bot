package com.NickSishchuck.StezhkaBot.service;

import com.NickSishchuck.StezhkaBot.entity.EnrollmentRequest;
import com.NickSishchuck.StezhkaBot.repository.EnrollmentRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

@Service
public class EnrollmentService {

    private static final Logger logger = LoggerFactory.getLogger(EnrollmentService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final int MAX_REQUESTS_PER_USER = 5;

    private final EnrollmentRequestRepository enrollmentRepository;

    // Temporary storage for enrollment forms in progress
    private final Map<Long, EnrollmentFormState> activeEnrollments = new ConcurrentHashMap<>();

    // Track enrollment requests count per user with timestamps
    private final Map<Long, List<LocalDateTime>> userRequestCounts = new ConcurrentHashMap<>();

    @Value("${bot.admin.user.ids:}")
    private String adminUserIds;

    @Autowired
    public EnrollmentService(EnrollmentRequestRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    /**
     * Start new enrollment process
     */
    public void startEnrollment(Long userId, String course, String courseDisplayName) {
        EnrollmentFormState state = new EnrollmentFormState();
        state.setCourse(course);
        state.setCourseDisplayName(courseDisplayName);
        state.setCurrentStep(EnrollmentStep.CHILD_NAME);
        state.setStartedAt(LocalDateTime.now());

        activeEnrollments.put(userId, state);
        logger.info("Started enrollment for user {} for course {}", userId, course);
    }

    /**
     * Start enrollment without pre-selected course
     */
    public void startEnrollmentWithoutCourse(Long userId) {
        EnrollmentFormState state = new EnrollmentFormState();
        state.setCurrentStep(EnrollmentStep.SELECT_COURSE);
        state.setStartedAt(LocalDateTime.now());

        activeEnrollments.put(userId, state);
        logger.info("Started enrollment for user {} without course", userId);
    }

    /**
     * Get current enrollment state for user
     */
    public EnrollmentFormState getEnrollmentState(Long userId) {
        return activeEnrollments.get(userId);
    }

    /**
     * Process user input for enrollment
     */
    public EnrollmentStep processEnrollmentInput(Long userId, String input) {
        EnrollmentFormState state = activeEnrollments.get(userId);
        if (state == null) {
            return null;
        }

        switch (state.getCurrentStep()) {
            case CHILD_NAME -> {
                state.setChildName(input.trim());
                state.setCurrentStep(EnrollmentStep.CHILD_AGE);
            }
            case CHILD_AGE -> {
                state.setChildAge(input.trim());
                state.setCurrentStep(EnrollmentStep.PARENT_NAME);
            }
            case PARENT_NAME -> {
                state.setParentName(input.trim());
                state.setCurrentStep(EnrollmentStep.PARENT_PHONE);
            }
            case PARENT_PHONE -> {
                state.setParentPhone(input.trim());
                state.setCurrentStep(EnrollmentStep.CONFIRM);
            }
        }

        return state.getCurrentStep();
    }

    /**
     * Set course for enrollment
     */
    public void setCourse(Long userId, String course, String courseDisplayName) {
        EnrollmentFormState state = activeEnrollments.get(userId);
        if (state != null) {
            state.setCourse(course);
            state.setCourseDisplayName(courseDisplayName);
            state.setCurrentStep(EnrollmentStep.CHILD_NAME);
        }
    }

    /**
     * Complete enrollment and save to database
     */
    @Transactional
    public EnrollmentRequest completeEnrollment(Long userId) {
        EnrollmentFormState state = activeEnrollments.get(userId);
        if (state == null) {
            return null;
        }

        // Create enrollment request
        EnrollmentRequest request = new EnrollmentRequest(
                state.getChildName(),
                state.getChildAge(),
                state.getParentName(),
                state.getParentPhone(),
                state.getCourse(),
                state.getCourseDisplayName(),
                userId
        );

        // Save to database
        EnrollmentRequest saved = enrollmentRepository.save(request);

        // Track this request for rate limiting
        trackUserRequest(userId);

        // Clean up temporary state
        activeEnrollments.remove(userId);

        logger.info("Completed enrollment for user {} - Request ID: {}", userId, saved.getId());

        return saved;
    }

    /**
     * Cancel enrollment
     */
    public void cancelEnrollment(Long userId) {
        activeEnrollments.remove(userId);
        logger.info("Cancelled enrollment for user {}", userId);
    }

    /**
     * Check if user has exceeded the enrollment request limit
     */
    public boolean hasRecentEnrollment(Long userId, int hoursAgo) {
        // Clean up old entries first
        cleanupOldUserRequests(userId);

        // Check current count
        List<LocalDateTime> userRequests = userRequestCounts.getOrDefault(userId, new ArrayList<>());

        if (userRequests.size() >= MAX_REQUESTS_PER_USER) {
            logger.info("User {} has reached the maximum enrollment requests limit ({}/{})",
                    userId, userRequests.size(), MAX_REQUESTS_PER_USER);
            return true;
        }

        return false;
    }

    /**
     * Track a user's enrollment request
     */
    private void trackUserRequest(Long userId) {
        userRequestCounts.computeIfAbsent(userId, k -> new ArrayList<>()).add(LocalDateTime.now());
        logger.debug("Tracked enrollment request for user {}. Current count: {}",
                userId, userRequestCounts.get(userId).size());
    }

    /**
     * Clean up old requests for a specific user (older than 30 minutes)
     */
    private void cleanupOldUserRequests(Long userId) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
        List<LocalDateTime> userRequests = userRequestCounts.get(userId);

        if (userRequests != null) {
            int originalSize = userRequests.size();
            userRequests.removeIf(timestamp -> timestamp.isBefore(cutoff));

            if (userRequests.isEmpty()) {
                userRequestCounts.remove(userId);
            }

            if (originalSize != userRequests.size()) {
                logger.debug("Cleaned up old requests for user {}. Removed: {}, Remaining: {}",
                        userId, originalSize - userRequests.size(), userRequests.size());
            }
        }
    }

    /**
     * Get all unprocessed enrollment requests
     */
    public List<EnrollmentRequest> getUnprocessedRequests() {
        return enrollmentRepository.findByStatusOrderByCreatedAtDesc(EnrollmentRequest.EnrollmentStatus.NEW);
    }

    /**
     * Mark request as processed
     */
    @Transactional
    public boolean markAsProcessed(Long requestId, Long adminUserId) {
        Optional<EnrollmentRequest> requestOpt = enrollmentRepository.findById(requestId);
        if (requestOpt.isPresent()) {
            EnrollmentRequest request = requestOpt.get();
            request.setStatus(EnrollmentRequest.EnrollmentStatus.PROCESSED);
            request.setProcessedAt(LocalDateTime.now());
            request.setProcessedBy(adminUserId);
            enrollmentRepository.save(request);
            logger.info("Request {} marked as processed by admin {}", requestId, adminUserId);
            return true;
        }
        return false;
    }

    /**
     * Mark request as processed by admin message ID
     */
    @Transactional
    public boolean markAsProcessedByMessageId(Integer messageId, Long adminUserId) {
        Optional<EnrollmentRequest> requestOpt = enrollmentRepository.findByAdminMessageId(messageId);
        if (requestOpt.isPresent()) {
            EnrollmentRequest request = requestOpt.get();
            request.setStatus(EnrollmentRequest.EnrollmentStatus.PROCESSED);
            request.setProcessedAt(LocalDateTime.now());
            request.setProcessedBy(adminUserId);
            enrollmentRepository.save(request);
            logger.info("Request {} marked as processed by admin {} via message {}",
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
        enrollmentRepository.findById(requestId).ifPresent(request -> {
            request.setAdminMessageId(messageId);
            enrollmentRepository.save(request);
        });
    }

    /**
     * Get enrollment statistics
     */
    public String getStatistics() {
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);

        long totalUnprocessed = enrollmentRepository.countByStatus(EnrollmentRequest.EnrollmentStatus.NEW);
        long weekTotal = enrollmentRepository.countEnrollmentsSince(weekAgo);
        long monthTotal = enrollmentRepository.countEnrollmentsSince(monthAgo);

        return String.format(
                "📊 *Статистика заявок*\n\n" +
                        "⏳ Необроблені: %d\n" +
                        "📅 За тиждень: %d\n" +
                        "📆 За місяць: %d",
                totalUnprocessed, weekTotal, monthTotal
        );
    }

    /**
     * Clean up old enrollment states and user request counts (called periodically)
     */
    public void cleanupAbandonedEnrollments() {
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(30);

        // Clean up abandoned enrollment forms
        activeEnrollments.entrySet().removeIf(entry ->
                entry.getValue().getStartedAt().isBefore(timeout)
        );

        // Clean up old user request counts
        userRequestCounts.entrySet().removeIf(entry -> {
            List<LocalDateTime> requests = entry.getValue();
            requests.removeIf(timestamp -> timestamp.isBefore(timeout));
            return requests.isEmpty();
        });

        logger.debug("Cleaned up abandoned enrollments and old request counts");
    }

    /**
     * Get current request count for user (for debugging/admin purposes)
     */
    public int getCurrentRequestCount(Long userId) {
        cleanupOldUserRequests(userId);
        List<LocalDateTime> requests = userRequestCounts.get(userId);
        return requests != null ? requests.size() : 0;
    }

    /**
     * Format enrollment request for admin notification
     */
    public String formatRequestForAdmin(EnrollmentRequest request) {
        return String.format(
                "🆕 *Нова заявка на навчання!*\n\n" +
                        "👦 *Дитина:* %s (%s років)\n" +
                        "👨‍👩‍👧 *Батьки:* %s\n" +
                        "📞 *Телефон:* %s\n" +
                        "🎓 *Програма:* %s\n\n" +
                        "📅 *Дата:* %s\n" +
                        "🆔 *ID заявки:* #%d",
                request.getChildName(),
                request.getChildAge(),
                request.getParentName(),
                request.getParentPhone(),
                request.getCourseDisplayName(),
                request.getCreatedAt().format(DATE_FORMATTER),
                request.getId()
        );
    }

    /**
     * Enrollment form state
     */
    public static class EnrollmentFormState {
        private String childName;
        private String childAge;
        private String parentName;
        private String parentPhone;
        private String course;
        private String courseDisplayName;
        private EnrollmentStep currentStep;
        private LocalDateTime startedAt;

        // Getters and setters
        public String getChildName() { return childName; }
        public void setChildName(String childName) { this.childName = childName; }

        public String getChildAge() { return childAge; }
        public void setChildAge(String childAge) { this.childAge = childAge; }

        public String getParentName() { return parentName; }
        public void setParentName(String parentName) { this.parentName = parentName; }

        public String getParentPhone() { return parentPhone; }
        public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }

        public String getCourse() { return course; }
        public void setCourse(String course) { this.course = course; }

        public String getCourseDisplayName() { return courseDisplayName; }
        public void setCourseDisplayName(String courseDisplayName) { this.courseDisplayName = courseDisplayName; }

        public EnrollmentStep getCurrentStep() { return currentStep; }
        public void setCurrentStep(EnrollmentStep currentStep) { this.currentStep = currentStep; }

        public LocalDateTime getStartedAt() { return startedAt; }
        public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    }

    /**
     * Enrollment steps enum
     */
    public enum EnrollmentStep {
        SELECT_COURSE,
        CHILD_NAME,
        CHILD_AGE,
        PARENT_NAME,
        PARENT_PHONE,
        CONFIRM
    }
}