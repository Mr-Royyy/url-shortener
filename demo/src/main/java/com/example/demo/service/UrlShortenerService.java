package com.example.demo.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.Random;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.model.UrlMapping;
import com.example.demo.repository.jpa.UrlMappingRepository;

@Service
public class UrlShortenerService {

    private final UrlMappingRepository repository;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String REDIS_PREFIX = "shortUrl:";

    public UrlShortenerService(UrlMappingRepository repository, RedisTemplate<String, String> redisTemplate) {
        this.repository = repository;
        this.redisTemplate = redisTemplate;
    }

    // Create short link
    public UrlMapping createShortLink(String originalUrl) {
        if (!isValidUrl(originalUrl)) {
        throw new IllegalArgumentException("Invalid URL: " + originalUrl);
    }
        Optional<UrlMapping> existing = repository.findByOriginalUrl(originalUrl);
        if (existing.isPresent()) {
            return existing.get();
        }
        String code;
        do {
            code = generateShortCode();
        } while (repository.findByShortCode(code).isPresent());
        UrlMapping mapping = new UrlMapping();
        mapping.setOriginalUrl(originalUrl);
        mapping.setShortCode(code);
        repository.save(mapping);
        // Cache the shortCode -> originalUrl mapping in Redis
        redisTemplate.opsForValue().set(REDIS_PREFIX + code, originalUrl);
        return mapping;
    }

    // Lookup by short code with Redis cache
    public Optional<UrlMapping> getByShortCode(String shortCode) {
        // Check cache first
        String cachedUrl = redisTemplate.opsForValue().get(REDIS_PREFIX + shortCode);
        if (cachedUrl != null) {
            // Create a UrlMapping object on the fly (clickCount and createdAt unknown here)
            UrlMapping cachedMapping = new UrlMapping();
            cachedMapping.setShortCode(shortCode);
            cachedMapping.setOriginalUrl(cachedUrl);
            return Optional.of(cachedMapping);
        }
        // Cache miss: lookup DB
        Optional<UrlMapping> mappingOpt = repository.findByShortCode(shortCode);
        mappingOpt.ifPresent(mapping -> {
            // Update cache
            redisTemplate.opsForValue().set(REDIS_PREFIX + shortCode, mapping.getOriginalUrl());
        });
        return mappingOpt;
    }

    // Increment click count (DB update + optionally invalidate cache)
    public void incrementClickCount(String shortCode) {
        repository.findByShortCode(shortCode).ifPresent(link -> {
            link.incrementClickCount();
            repository.save(link);
            // No need to update cache for originalUrl, so no cache action here
        });
    }

    // Generate random 6-char code
    private String generateShortCode() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public boolean isValidUrl(String url) {
    try {
        new URL(url);
        return true;
    } catch (MalformedURLException e) {
        return false;
    }
}
}
