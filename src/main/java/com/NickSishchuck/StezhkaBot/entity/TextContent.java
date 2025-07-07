package com.NickSishchuck.StezhkaBot.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "text_content")
public class TextContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text_key", unique = true, nullable = false)
    private String textKey;

    @Column(name = "text_value", columnDefinition = "TEXT")
    private String textValue;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public TextContent() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public TextContent(String textKey, String textValue, String description) {
        this();
        this.textKey = textKey;
        this.textValue = textValue;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTextKey() { return textKey; }
    public void setTextKey(String textKey) { this.textKey = textKey; }

    public String getTextValue() { return textValue; }
    public void setTextValue(String textValue) {
        this.textValue = textValue;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}