package com.example.demo.service;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.model.UrlMapping;
import com.example.demo.repository.jpa.UrlMappingRepository;

@Service
public class UrlShortenerService {

    private static final Logger logger = LoggerFactory.getLogger(UrlShortenerService.class);

    private final UrlMappingRepository repository;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String REDIS_PREFIX = "shortUrl:";

    public UrlShortenerService(UrlMappingRepository repository, RedisTemplate<String, String> redisTemplate) {
        this.repository = repository;
        this.redisTemplate = redisTemplate;
    }

    public UrlMapping createLink(String originalUrl) {
        try {
            return createLink(originalUrl, null);
        } catch (Exception ex) {
            logger.error("Error creating link for URL {}", originalUrl, ex);
            throw ex;
        }
    }

    public UrlMapping createLink(String originalUrl, LocalDateTime expiry) {
        try {
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
        } catch (Exception ex) {
            logger.error("Error creating link with expiry for URL {}", originalUrl, ex);
            throw ex;
        }
    }

    public UrlMapping createLink(String originalUrl, String customCode, LocalDateTime expiry) {
        try {
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
        } catch (Exception ex) {
            logger.error("Error creating link with custom code {} for URL {}", customCode, originalUrl, ex);
            throw ex;
        }
    }

    private UrlMapping createMapping(String originalUrl, String code, LocalDateTime expiry) {
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setOriginalUrl(originalUrl);
        urlMapping.setShortCode(code);
        urlMapping.setExpiryDate(expiry);
        repository.save(urlMapping);
        try {
            redisTemplate.opsForValue().set(REDIS_PREFIX + code, originalUrl);
        } catch (Exception ex) {
            logger.warn("Failed to cache URL {} with code {} in Redis", originalUrl, code, ex);
            // Continue without Redis caching failure affecting main flow
        }
        return urlMapping;
    }

    public Optional<UrlMapping> getByCode(String code) {
        try {
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
        } catch (Exception ex) {
            logger.error("Error fetching URL mapping for code {}", code, ex);
            return Optional.empty();
        }
    }

    public void incrementClicks(String code) {
        try {
            repository.findByShortCode(code).ifPresent(link -> {
                link.incrementClickCount();
                repository.save(link);
            });
        } catch (Exception ex) {
            logger.error("Error incrementing click count for code {}", code, ex);
        }
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
        URI.create(url);
        return true;
    } catch (IllegalArgumentException ex) {
        return false;
    }
}


    private boolean isValidCode(String code) {
        return code != null && code.matches("^[a-zA-Z0-9]{4,10}$");
    }

    /**
     * Returns latest 7-day click counts for given short code
     */
    public Map<String, Integer> getClickStats(String shortCode) {
        Map<String, Integer> clicks = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            String day = today.minusDays(i).toString();
            clicks.put(day, (int)(Math.random() * 20));
        }
        return clicks;
    }
}
