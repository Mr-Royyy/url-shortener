package com.example.demo.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.example.demo.model.UrlMapping;
import com.example.demo.repository.jpa.UrlMappingRepository;

class UrlShortenerServiceTest {

    @Mock
    private UrlMappingRepository repository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private UrlShortenerService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new UrlShortenerService(repository, redisTemplate);
    }

    @Test
    void testCreateLinkReturnsExistingIfPresent() {
        String originalUrl = "http://example.com";
        UrlMapping existingMapping = new UrlMapping();
        existingMapping.setOriginalUrl(originalUrl);

        when(repository.findByOriginalUrl(originalUrl)).thenReturn(Optional.of(existingMapping));

        UrlMapping mapping = service.createLink(originalUrl);

        assertEquals(existingMapping, mapping);
        verify(repository, never()).save(any());
        verify(valueOperations, never()).set(anyString(), anyString());
    }

    @Test
    void testCreateLinkCreatesAndCachesNew() {
        String originalUrl = "http://newurl.com";

        when(repository.findByOriginalUrl(originalUrl)).thenReturn(Optional.empty());
        when(repository.findByShortCode(anyString())).thenReturn(Optional.empty());

        doAnswer(invocation -> {
            UrlMapping arg = invocation.getArgument(0);
            arg.setShortCode("abc123");
            return arg;
        }).when(repository).save(any());

        UrlMapping mapping = service.createLink(originalUrl);

        assertNotNull(mapping.getShortCode());
        assertEquals(originalUrl, mapping.getOriginalUrl());
        verify(repository).save(any());
        verify(valueOperations).set(argThat(s -> s.startsWith("shortUrl:")), eq(originalUrl));
    }

    @Test
    void testGetByCodeCacheHitReturnsMapping() {
        String shortCode = "abc123";
        String cachedUrl = "http://cached.com";

        when(valueOperations.get("shortUrl:" + shortCode)).thenReturn(cachedUrl);

        Optional<UrlMapping> optionalMapping = service.getByCode(shortCode);

        assertTrue(optionalMapping.isPresent());
        assertEquals(shortCode, optionalMapping.get().getShortCode());
        assertEquals(cachedUrl, optionalMapping.get().getOriginalUrl());
        verify(repository, never()).findByShortCode(anyString());
    }

    @Test
    void testGetByCodeCacheMissQueriesDbAndUpdatesCache() {
        String shortCode = "xyz789";
        UrlMapping dbMapping = new UrlMapping();
        dbMapping.setShortCode(shortCode);
        dbMapping.setOriginalUrl("http://dburl.com");

        when(valueOperations.get("shortUrl:" + shortCode)).thenReturn(null);
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(dbMapping));

        Optional<UrlMapping> optionalMapping = service.getByCode(shortCode);

        assertTrue(optionalMapping.isPresent());
        assertEquals(dbMapping, optionalMapping.get());
        verify(valueOperations).set("shortUrl:" + shortCode, dbMapping.getOriginalUrl());
    }

    @Test
    void testIncrementClicksUpdatesRepository() {
        String shortCode = "increment1";
        UrlMapping mapping = new UrlMapping();
        mapping.setShortCode(shortCode);
        mapping.setClickCount(0);

        when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(mapping));

        service.incrementClicks(shortCode);

        assertEquals(1, mapping.getClickCount());
        verify(repository).save(mapping);
    }
}
