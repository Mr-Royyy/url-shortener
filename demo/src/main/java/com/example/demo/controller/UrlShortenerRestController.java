package com.example.demo.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AnalyticsResponse;
import com.example.demo.dto.ShortenRequest;
import com.example.demo.dto.ShortenResponse;
import com.example.demo.model.UrlMapping;
import com.example.demo.service.UrlShortenerService;

@RestController
@RequestMapping("/api")
public class UrlShortenerRestController {

    private final UrlShortenerService service;

    public UrlShortenerRestController(UrlShortenerService service) {
        this.service = service;
    }

    // POST /api/shorten - create short URL
    @PostMapping("/shorten")
    public ResponseEntity<?> shortenUrl(@RequestBody ShortenRequest request) {
        String originalUrl = request.getUrl();
        if (originalUrl == null || originalUrl.isBlank()) {
            return ResponseEntity.badRequest().body("URL cannot be empty");
        }
        try {
            new URI(originalUrl);
        } catch (URISyntaxException e) {
            return ResponseEntity.badRequest().body("Invalid URL format");
        }

        UrlMapping mapping = service.createShortLink(originalUrl);
        String shortUrl = String.format("http://localhost:8081/api/u/%s", mapping.getShortCode());
        return ResponseEntity.ok(new ShortenResponse(shortUrl));
    }

    // GET /api/u/{shortCode} - redirect to original URL
    @GetMapping("/u/{shortCode}")
    public ResponseEntity<?> redirect(@PathVariable String shortCode) {
        Optional<UrlMapping> optionalMapping = service.getByShortCode(shortCode);
        if (optionalMapping.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Short URL not found");
        }
        service.incrementClickCount(shortCode);
        String originalUrl = optionalMapping.get().getOriginalUrl();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(originalUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    // GET /api/analytics/{shortCode} - analytics info for a short URL
    @GetMapping("/analytics/{shortCode}")
    public ResponseEntity<?> getAnalytics(@PathVariable String shortCode) {
        Optional<UrlMapping> optionalMapping = service.getByShortCode(shortCode);
        if (optionalMapping.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Short URL not found");
        }
        UrlMapping mapping = optionalMapping.get();
        AnalyticsResponse response = new AnalyticsResponse(
                mapping.getOriginalUrl(),
                mapping.getShortCode(),
                mapping.getClickCount(),
                mapping.getCreatedAt()
        );
        return ResponseEntity.ok(response);
    }
}
