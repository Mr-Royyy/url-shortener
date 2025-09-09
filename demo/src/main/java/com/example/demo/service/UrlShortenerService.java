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

    public UrlMapping createLink(String originalUrl) {
        if (!isValidUrl(originalUrl)) {
            throw new IllegalArgumentException("Invalid URL: " + originalUrl);
        }
        Optional<UrlMapping> existing = repository.findByOriginalUrl(originalUrl);
        if (existing.isPresent()) {
            return existing.get();
        }
        String code;
        do {
            code = generateCode();
        } while (repository.findByShortCode(code).isPresent());
        return createMapping(originalUrl, code);
    }

    public UrlMapping createLink(String originalUrl, String customCode) {
        if (!isValidUrl(originalUrl)) {
            throw new IllegalArgumentException("Invalid URL: " + originalUrl);
        }
        if (!isValidCode(customCode)) {
            throw new IllegalArgumentException("Invalid custom code: " + customCode);
        }
        if (repository.findByShortCode(customCode).isPresent()) {
            throw new IllegalArgumentException("Custom code already in use");
        }
        return createMapping(originalUrl, customCode);
    }

    private UrlMapping createMapping(String originalUrl, String code) {
        UrlMapping mapping = new UrlMapping();
        mapping.setOriginalUrl(originalUrl);
        mapping.setShortCode(code);
        repository.save(mapping);
        redisTemplate.opsForValue().set(REDIS_PREFIX + code, originalUrl);
        return mapping;
    }

    public Optional<UrlMapping> getByCode(String code) {
        String cached = redisTemplate.opsForValue().get(REDIS_PREFIX + code);
        if (cached != null) {
            UrlMapping m = new UrlMapping();
            m.setShortCode(code);
            m.setOriginalUrl(cached);
            return Optional.of(m);
        }
        Optional<UrlMapping> found = repository.findByShortCode(code);
        found.ifPresent(f -> redisTemplate.opsForValue().set(REDIS_PREFIX + code, f.getOriginalUrl()));
        return found;
    }

    public void incrementClicks(String code) {
        repository.findByShortCode(code).ifPresent(m -> {
            m.incrementClickCount();
            repository.save(m);
        });
    }

    private String generateCode() {
        String allowed = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(allowed.charAt(rnd.nextInt(allowed.length())));
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

    private boolean isValidCode(String code) {
        return code != null && code.matches("^[a-zA-Z0-9]{4,10}$");
    }
}
