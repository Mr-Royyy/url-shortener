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

    private String generateShortCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(BASE62.charAt(random.nextInt(BASE62.length())));
        }
        return sb.toString();
    }

    @Transactional
    public UrlMapping createShortLink(String originalUrl) {
        String shortCode;
        do {
            shortCode = generateShortCode();
        } while (repository.existsByShortCode(shortCode));

        UrlMapping mapping = new UrlMapping();
        mapping.setShortCode(shortCode);
        mapping.setOriginalUrl(originalUrl);
        mapping.setClickCount(0);

        UrlMapping saved = repository.save(mapping);
        redisTemplate.opsForValue().set("short:" + shortCode, originalUrl, 24, TimeUnit.HOURS);

        return saved;
    }

    public Optional<UrlMapping> getByShortCode(String shortCode) {
        String cached = redisTemplate.opsForValue().get("short:" + shortCode);
        if (cached != null) {
            UrlMapping mapping = new UrlMapping();
            mapping.setShortCode(shortCode);
            mapping.setOriginalUrl(cached);
            return Optional.of(mapping);
        }
        return repository.findByShortCode(shortCode)
                .map(mapping -> {
                    redisTemplate.opsForValue().set("short:" + shortCode, mapping.getOriginalUrl(), 24, TimeUnit.HOURS);
                    return mapping;
                });
    }

    @Transactional
    public void incrementClickCount(String shortCode) {
        repository.findByShortCode(shortCode).ifPresent(mapping -> {
            mapping.setClickCount(mapping.getClickCount() + 1);
            repository.save(mapping);
        });
    }
}
