package com.example.demo.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.ShortLink;
import com.example.demo.repository.ShortLinkRepository;

@Service
public class ShortLinkService {

    private final ShortLinkRepository repository;
    private final Random random = new Random();
    private static final String BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public ShortLinkService(ShortLinkRepository repository) {
        this.repository = repository;
    }

    // ✅ Validate URL before saving
    private boolean isValidUrl(String url) {
        try {
            new URL(url); // will throw exception if invalid
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    // ✅ Create a new short link
    @Transactional
    public Optional<ShortLink> createShortLink(String originalUrl) {
        if (!isValidUrl(originalUrl)) {
            return Optional.empty(); // invalid URL → return empty
        }

        String shortCode;
        do {
            shortCode = generateShortCode();
        } while (repository.existsByShortCode(shortCode));

        ShortLink shortLink = new ShortLink();
        shortLink.setOriginalUrl(originalUrl);
        shortLink.setShortCode(shortCode);

        return Optional.of(repository.save(shortLink));
    }

    // ✅ Fetch by short code
    public Optional<ShortLink> getByShortCode(String shortCode) {
        return repository.findByShortCode(shortCode);
    }

    // ✅ Increment click count
    @Transactional
    public void incrementClickCount(String shortCode) {
        repository.findByShortCode(shortCode).ifPresent(link -> {
            link.setClickCount(link.getClickCount() + 1);
            repository.save(link);
        });
    }

    // ✅ Random Base62 short code generator
    private String generateShortCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) { // 6-char short codes
            sb.append(BASE62.charAt(random.nextInt(BASE62.length())));
        }
        return sb.toString();
    }
}
