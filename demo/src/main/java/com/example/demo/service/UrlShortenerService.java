package com.example.demo.service;

import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.example.demo.model.UrlMapping;
import com.example.demo.repository.UrlMappingRepository;

@Service
public class UrlShortenerService {

    private final UrlMappingRepository repository;

    public UrlShortenerService(UrlMappingRepository repository) {
        this.repository = repository;
    }

    // Create a short link
    public UrlMapping createShortLink(String originalUrl) {
        // Check if URL already exists
        Optional<UrlMapping> existing = repository.findByOriginalUrl(originalUrl);
        if (existing.isPresent()) return existing.get();

        // Generate unique short code
        String code;
        do {
            code = generateShortCode();
        } while (repository.findByShortCode(code).isPresent());

        UrlMapping mapping = new UrlMapping();
        mapping.setOriginalUrl(originalUrl);
        mapping.setShortCode(code);
        repository.save(mapping);
        return mapping;
    }

    // Retrieve by short code
    public Optional<UrlMapping> getByShortCode(String shortCode) {
        return repository.findByShortCode(shortCode);
    }

    // Increment click count
    public void incrementClickCount(String shortCode) {
        repository.findByShortCode(shortCode).ifPresent(link -> {
            link.incrementClickCount();
            repository.save(link);
        });
    }

    // Generate random 6-character code
    private String generateShortCode() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }
}
