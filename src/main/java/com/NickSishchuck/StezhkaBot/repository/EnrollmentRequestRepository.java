package com.NickSishchuck.StezhkaBot.repository;

import com.NickSishchuck.StezhkaBot.entity.EnrollmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRequestRepository extends JpaRepository<EnrollmentRequest, Long> {

    // Find all unprocessed requests
    List<EnrollmentRequest> findByStatusOrderByCreatedAtDesc(EnrollmentRequest.EnrollmentStatus status);

    // Find recent request by user to check for duplicates
    Optional<EnrollmentRequest> findFirstByTelegramUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
            Long telegramUserId, LocalDateTime after);

    // Count unprocessed requests
    long countByStatus(EnrollmentRequest.EnrollmentStatus status);

    // Find by admin message ID
    Optional<EnrollmentRequest> findByAdminMessageId(Integer adminMessageId);

    // Statistics queries
    @Query("SELECT COUNT(e) FROM EnrollmentRequest e WHERE e.createdAt >= :startDate")
    long countEnrollmentsSince(LocalDateTime startDate);

    @Query("SELECT COUNT(e) FROM EnrollmentRequest e WHERE e.status = :status AND e.createdAt >= :startDate")
    long countEnrollmentsByStatusSince(EnrollmentRequest.EnrollmentStatus status, LocalDateTime startDate);
}