package com.example.demo.service;

// import java.net.MalformedURLException;
// import java.net.URL;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.ShortLink;
import com.example.demo.repository.ShortLinkRepository;

@Service
public class ShortLinkService {

    private final ShortLinkRepository repository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    public ShortLinkService(ShortLinkRepository repository, RedisTemplate<String, String> redisTemplate) {
        this.repository = repository;
        this.redisTemplate = redisTemplate;
    }


private boolean isValidUrl(String url) {
    try {
        URI.create(url); // throws if invalid
        return true;
    } catch (Exception e) {
        return false;
    }
}


    // ✅ Create a new short link
    @Transactional
    public Optional<ShortLink> createShortLink(String originalUrl) {
        if (!isValidUrl(originalUrl)) {
            return Optional.empty();
        }

        String shortCode;
        do {
            shortCode = generateShortCode();
        } while (repository.existsByShortCode(shortCode));

        ShortLink shortLink = new ShortLink(shortCode, originalUrl);
        ShortLink saved = repository.save(shortLink);

        // Store in Redis (cache for 24 hours)
        redisTemplate.opsForValue().set("short:" + shortCode, originalUrl, 24, TimeUnit.HOURS);

        return Optional.of(saved);
    }

    // ✅ Fetch by short code (Redis first, then DB)
    public Optional<ShortLink> getByShortCode(String shortCode) {
        // Try Redis first
        String cachedUrl = redisTemplate.opsForValue().get("short:" + shortCode);
        if (cachedUrl != null) {
            return Optional.of(new ShortLink(shortCode, cachedUrl));
        }

        // Fallback to DB
        Optional<ShortLink> dbResult = repository.findByShortCode(shortCode);
        dbResult.ifPresent(link ->
            redisTemplate.opsForValue().set("short:" + shortCode, link.getOriginalUrl(), 24, TimeUnit.HOURS)
        );

        return dbResult;
    }

    // ✅ Increment click count (DB only)
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
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(BASE62.charAt(random.nextInt(BASE62.length())));
        }
        return sb.toString();
    }
}
