package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;

import com.example.demo.model.UrlMapping;
import com.example.demo.repository.jpa.UrlMappingRepository;

public class ExpiredUrlCleanupTaskTest {

    @Mock
    private UrlMappingRepository repository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    private ExpiredUrlCleanupTask cleanupTask;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCleanExpiredUrls() {
        UrlMapping expiredUrl = new UrlMapping();
        expiredUrl.setShortCode("abc123");
        expiredUrl.setExpiryDate(LocalDateTime.now().minusDays(1));

        when(repository.findAllByExpiryDateBefore(any(LocalDateTime.class)))
            .thenReturn(List.of(expiredUrl));

        cleanupTask.cleanExpiredUrls();

        verify(repository).delete(expiredUrl);
        verify(redisTemplate).delete("url:" + expiredUrl.getShortCode());
    }
}
