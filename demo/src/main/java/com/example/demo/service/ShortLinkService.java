package com.example.demo.service;

import com.example.demo.model.ShortLink;
import com.example.demo.repository.ShortLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class ShortLinkService {

    @Autowired
    private ShortLinkRepository shortLinkRepository;

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SHORT_CODE_LENGTH = 6;

    // Generate a random short code
    private String generateShortCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        do {
            sb.setLength(0); // reset
            for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
                sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
            }
        } while (shortLinkRepository.existsByShortCode(sb.toString()));

        return sb.toString();
    }

    // Create short link
    public ShortLink createShortLink(String originalUrl) {
        String shortCode = generateShortCode();

        ShortLink shortLink = new ShortLink();
        shortLink.setOriginalUrl(originalUrl);
        shortLink.setShortCode(shortCode);
        shortLink.setCreatedAt(LocalDateTime.now());
        shortLink.setClickCount(0);

        return shortLinkRepository.save(shortLink);
    }

    // Retrieve by short code
    public Optional<ShortLink> getByShortCode(String shortCode) {
        return shortLinkRepository.findByShortCode(shortCode);
    }

    // Update click count
    public void incrementClickCount(ShortLink shortLink) {
        shortLink.setClickCount(shortLink.getClickCount() + 1);
        shortLinkRepository.save(shortLink);
    }
}
