package com.example.demo.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
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

    public UrlMapping createLink(String originalUrl) {
        return createLink(originalUrl, null);
    }

    public UrlMapping createLink(String originalUrl, LocalDateTime expiry) {
        if (!isValidUrl(originalUrl)) {
            throw new IllegalArgumentException("Invalid URL");
        }
        Optional<UrlMapping> existing = repository.findByOriginalUrl(originalUrl);
        if (existing.isPresent() && !existing.get().isExpired()) {
            return existing.get();
        }
        String code;
        do {
            code = generateCode();
        } while (repository.findByShortCode(code).isPresent());
        return createMapping(originalUrl, code, expiry);
    }

    public UrlMapping createLink(String originalUrl, String customCode, LocalDateTime expiry) {
        if (!isValidUrl(originalUrl)) {
            throw new IllegalArgumentException("Invalid URL");
        }
        if (!isValidCode(customCode)) {
            throw new IllegalArgumentException("Invalid custom code");
        }
        if (repository.findByShortCode(customCode).isPresent()) {
            throw new IllegalArgumentException("Custom code already exists");
        }
        return createMapping(originalUrl, customCode, expiry);
    }

    private UrlMapping createMapping(String originalUrl, String code, LocalDateTime expiry) {
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setOriginalUrl(originalUrl);
        urlMapping.setShortCode(code);
        urlMapping.setExpiryDate(expiry);
        repository.save(urlMapping);
        redisTemplate.opsForValue().set(REDIS_PREFIX + code, originalUrl);
        return urlMapping;
    }

    public Optional<UrlMapping> getByCode(String code) {
        String cached = redisTemplate.opsForValue().get(REDIS_PREFIX + code);
        if (cached != null) {
            UrlMapping mapping = new UrlMapping();
            mapping.setShortCode(code);
            mapping.setOriginalUrl(cached);
            if (mapping.isExpired()) {
                return Optional.empty();
            }
            return Optional.of(mapping);
        }
        Optional<UrlMapping> dbMapping = repository.findByShortCode(code);
        if (dbMapping.isPresent() && !dbMapping.get().isExpired()) {
            redisTemplate.opsForValue().set(REDIS_PREFIX + code, dbMapping.get().getOriginalUrl());
            return dbMapping;
        }
        return Optional.empty();
    }

    public void incrementClicks(String code) {
        repository.findByShortCode(code).ifPresent(link -> {
            link.incrementClickCount();
            repository.save(link);
        });
    }

    private String generateCode() {
        String allowed = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(allowed.charAt(random.nextInt(allowed.length())));
        }
        return sb.toString();
    }

    public boolean isValidUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException ex) {
            return false;
        }
    }

    private boolean isValidCode(String code) {
        return code != null && code.matches("^[a-zA-Z0-9]{4,10}$");
    }
}
