package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ShortLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String shortCode;
    private String originalUrl;
    private int clickCount = 0;

    public ShortLink() {} // JPA requires default constructor

    public ShortLink(String shortCode, String originalUrl) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.clickCount = 0;
    }

    // Getters
    public Long getId() { return id; }
    public String getShortCode() { return shortCode; }
    public String getOriginalUrl() { return originalUrl; }
    public int getClickCount() { return clickCount; }

    // Setters
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }
    public void setClickCount(int clickCount) { this.clickCount = clickCount; }

    // Optional helper
    public void incrementClickCount() { this.clickCount++; }
}
