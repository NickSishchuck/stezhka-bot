package com.NickSishchuck.StezhkaBot.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollment_requests")
public class EnrollmentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "child_name", nullable = false)
    private String childName;

    @Column(name = "child_age", nullable = false)
    private String childAge;

    @Column(name = "parent_name", nullable = false)
    private String parentName;

    @Column(name = "parent_phone", nullable = false)
    private String parentPhone;

    @Column(name = "course", nullable = false)
    private String course;

    @Column(name = "course_display_name", nullable = false)
    private String courseDisplayName;

    @Column(name = "telegram_user_id", nullable = false)
    private Long telegramUserId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status = EnrollmentStatus.NEW;

    @Column(name = "admin_message_id")
    private Integer adminMessageId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "processed_by")
    private Long processedBy;

    // Constructors
    public EnrollmentRequest() {
        this.createdAt = LocalDateTime.now();
        this.status = EnrollmentStatus.NEW;
    }

    public EnrollmentRequest(String childName, String childAge, String parentName,
                             String parentPhone, String course, String courseDisplayName,
                             Long telegramUserId) {
        this();
        this.childName = childName;
        this.childAge = childAge;
        this.parentName = parentName;
        this.parentPhone = parentPhone;
        this.course = course;
        this.courseDisplayName = courseDisplayName;
        this.telegramUserId = telegramUserId;
    }

    // Enum for status
    public enum EnrollmentStatus {
        NEW, PROCESSED
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public Long getTelegramUserId() { return telegramUserId; }
    public void setTelegramUserId(Long telegramUserId) { this.telegramUserId = telegramUserId; }

    public EnrollmentStatus getStatus() { return status; }
    public void setStatus(EnrollmentStatus status) { this.status = status; }

    public Integer getAdminMessageId() { return adminMessageId; }
    public void setAdminMessageId(Integer adminMessageId) { this.adminMessageId = adminMessageId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public Long getProcessedBy() { return processedBy; }
    public void setProcessedBy(Long processedBy) { this.processedBy = processedBy; }
}