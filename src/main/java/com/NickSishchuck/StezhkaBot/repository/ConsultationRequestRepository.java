package com.NickSishchuck.StezhkaBot.repository;

import com.NickSishchuck.StezhkaBot.entity.ConsultationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationRequestRepository extends JpaRepository<ConsultationRequest, Long> {

    // Find all unprocessed requests
    List<ConsultationRequest> findByStatusOrderByCreatedAtDesc(ConsultationRequest.ConsultationStatus status);

    // Find recent request by user to check for duplicates
    Optional<ConsultationRequest> findFirstByTelegramUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
            Long telegramUserId, LocalDateTime after);

    // Count unprocessed requests
    long countByStatus(ConsultationRequest.ConsultationStatus status);

    // Find by admin message ID
    Optional<ConsultationRequest> findByAdminMessageId(Integer adminMessageId);
}