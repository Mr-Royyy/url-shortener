package com.example.demo.model;
import java.time.LocalDateTime;

import org.springframework.data.redis.core.RedisHash;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@RedisHash("shortLink")
@Entity
public class ShortLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String shortCode;
    private String originalUrl;
    private int clickCount = 0;
    private LocalDateTime createdAt = LocalDateTime.now();

    // Default constructor for JPA & Redis
    public ShortLink() {}

    // Constructor for creating new short links
    public ShortLink(String shortCode, String originalUrl) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.clickCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    // ===== Getters =====
    public Long getId() { return id; }
    public String getShortCode() { return shortCode; }
    public String getOriginalUrl() { return originalUrl; }
    public int getClickCount() { return clickCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // ===== Setters =====
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }
    public void setClickCount(int clickCount) { this.clickCount = clickCount; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Increment click count
    public void incrementClickCount() { this.clickCount++; }
}
