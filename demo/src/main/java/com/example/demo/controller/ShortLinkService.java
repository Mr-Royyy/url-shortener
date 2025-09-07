package com.example.demo.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.example.demo.model.ShortLink;

@Service
public class ShortLinkService {

    // In-memory storage for demo purposes (replace with DB)
    private final ConcurrentHashMap<String, ShortLink> linkStore = new ConcurrentHashMap<>();

    // Atomic integer for generating unique codes
    private final AtomicInteger counter = new AtomicInteger(1000);

    // Create or return existing short link
    public Optional<ShortLink> createShortLink(String originalUrl) {
        if (!isValidUrl(originalUrl)) {
            return Optional.empty();
        }

        // Check if URL already exists
        Optional<ShortLink> existing = linkStore.values().stream()
                .filter(link -> link.getOriginalUrl().equals(originalUrl))
                .findFirst();

        if (existing.isPresent()) {
            return existing;
        }

        // Generate new short code
        String shortCode = generateShortCode();
        ShortLink link = new ShortLink(shortCode, originalUrl);
        linkStore.put(shortCode, link);
        return Optional.of(link);
    }

    // Fetch a short link by code
    public Optional<ShortLink> getByShortCode(String code) {
        return Optional.ofNullable(linkStore.get(code));
    }

    // Increment click count
    public void incrementClickCount(String code) {
        linkStore.computeIfPresent(code, (k, v) -> {
            v.incrementClickCount();
            return v;
        });
    }

    // Validate URL format
    private boolean isValidUrl(String url) {
        try {
            URI uri = new URI(url);
            return uri.getScheme() != null && uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    // Generate simple numeric short code (can replace with Base62 later)
    private String generateShortCode() {
        return Integer.toString(counter.getAndIncrement(), 36); // Base36 short code
    }
}
