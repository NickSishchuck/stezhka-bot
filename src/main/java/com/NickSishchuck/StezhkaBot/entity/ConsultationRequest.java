package com.NickSishchuck.StezhkaBot.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultation_requests")
public class ConsultationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "telegram_user_id", nullable = false)
    private Long telegramUserId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ConsultationStatus status = ConsultationStatus.NEW;

    @Column(name = "admin_message_id")
    private Integer adminMessageId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "processed_by")
    private Long processedBy;

    // Constructors
    public ConsultationRequest() {
        this.createdAt = LocalDateTime.now();
        this.status = ConsultationStatus.NEW;
    }

    public ConsultationRequest(String name, String phone, Long telegramUserId) {
        this();
        this.name = name;
        this.phone = phone;
        this.telegramUserId = telegramUserId;
    }

    // Enum for status
    public enum ConsultationStatus {
        NEW, PROCESSED
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Long getTelegramUserId() { return telegramUserId; }
    public void setTelegramUserId(Long telegramUserId) { this.telegramUserId = telegramUserId; }

    public ConsultationStatus getStatus() { return status; }
    public void setStatus(ConsultationStatus status) { this.status = status; }

    public Integer getAdminMessageId() { return adminMessageId; }
    public void setAdminMessageId(Integer adminMessageId) { this.adminMessageId = adminMessageId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public Long getProcessedBy() { return processedBy; }
    public void setProcessedBy(Long processedBy) { this.processedBy = processedBy; }
}