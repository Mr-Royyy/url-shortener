package com.example.demo.service;

import java.security.SecureRandom;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.demo.model.ShortLink;
import com.example.demo.repository.ShortLinkRepository;

@Service
public class ShortLinkService {

    private final ShortLinkRepository shortLinkRepository;
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6; // e.g., "abc123"
    private final SecureRandom random = new SecureRandom();

    public ShortLinkService(ShortLinkRepository shortLinkRepository) {
        this.shortLinkRepository = shortLinkRepository;
    }

    // ✅ Generate short code
    private String generateShortCode() {
        StringBuilder code;
        do {
            code = new StringBuilder();
            for (int i = 0; i < CODE_LENGTH; i++) {
                int idx = random.nextInt(ALPHABET.length());
                code.append(ALPHABET.charAt(idx));
            }
        } while (shortLinkRepository.existsByShortCode(code.toString())); // ensure uniqueness
        return code.toString();
    }

    // ✅ Shorten a URL
    public ShortLink createShortLink(String originalUrl) {
        if (!isValidUrl(originalUrl)) {
            throw new IllegalArgumentException("Invalid URL: " + originalUrl);
        }

        String shortCode = generateShortCode();
        ShortLink shortLink = new ShortLink(shortCode, originalUrl);
        return shortLinkRepository.save(shortLink);
    }

    // ✅ Fetch by short code & increment click count
    public Optional<ShortLink> getAndTrack(String shortCode) {
        Optional<ShortLink> optional = shortLinkRepository.findByShortCode(shortCode);
        optional.ifPresent(link -> {
            link.setClickCount(link.getClickCount() + 1);
            shortLinkRepository.save(link);
        });
        return optional;
    }

    // ✅ Fetch analytics without increment
    public Optional<ShortLink> getAnalytics(String shortCode) {
        return shortLinkRepository.findByShortCode(shortCode);
    }

    // ✅ Simple URL validation
    private boolean isValidUrl(String url) {
        if (!StringUtils.hasText(url)) return false;
        return url.startsWith("http://") || url.startsWith("https://");
    }
}
