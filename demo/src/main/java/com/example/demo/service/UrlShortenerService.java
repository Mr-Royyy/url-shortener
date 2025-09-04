package com.example.demo.service;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.UrlMapping;
import com.example.demo.repository.UrlMappingRepository;

@Service
public class UrlShortenerService {

    private final UrlMappingRepository repository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    public UrlShortenerService(UrlMappingRepository repository, RedisTemplate<String, String> redisTemplate) {
        this.repository = repository;
        this.redisTemplate = redisTemplate;
    }

    // ✅ Generate random short code
    private String generateShortCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(BASE62.charAt(random.nextInt(BASE62.length())));
        }
        return sb.toString();
    }

    // ✅ Create a new short link
    @Transactional
    public UrlMapping createShortLink(String originalUrl) {
        String shortCode;
        do {
            shortCode = generateShortCode();
        } while (repository.existsByShortCode(shortCode));

        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setShortCode(shortCode);
        urlMapping.setOriginalUrl(originalUrl);

        UrlMapping saved = repository.save(urlMapping);

        // Save in Redis for 24h
        redisTemplate.opsForValue().set("short:" + shortCode, originalUrl, 24, TimeUnit.HOURS);

        return saved;
    }

    // ✅ Get by short code
    public Optional<String> getOriginalUrl(String shortCode) {
        // Check Redis first
        String cached = redisTemplate.opsForValue().get("short:" + shortCode);
        if (cached != null) {
            return Optional.of(cached);
        }

        // Fallback to DB
        return repository.findByShortCode(shortCode).map(urlMapping -> {
            redisTemplate.opsForValue().set("short:" + shortCode, urlMapping.getOriginalUrl(), 24, TimeUnit.HOURS);
            return urlMapping.getOriginalUrl();
        });
    }
}
