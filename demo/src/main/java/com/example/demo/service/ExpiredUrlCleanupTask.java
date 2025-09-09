package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.UrlMapping;
import com.example.demo.repository.jpa.UrlMappingRepository;


@Component
public class ExpiredUrlCleanupTask {

    private final UrlMappingRepository repository;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String REDIS_PREFIX = "shortUrl:";

    public ExpiredUrlCleanupTask(UrlMappingRepository repository, RedisTemplate<String, String> redisTemplate) {
        this.repository = repository;
        this.redisTemplate = redisTemplate;
    }

    // Runs every day at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanExpiredUrls() {
        List<UrlMapping> expiredUrls = repository.findAllByExpiryDateBefore(LocalDateTime.now());
        for (UrlMapping url : expiredUrls) {
            repository.delete(url);
            redisTemplate.delete(REDIS_PREFIX + url.getShortCode());
        }
    }
}
