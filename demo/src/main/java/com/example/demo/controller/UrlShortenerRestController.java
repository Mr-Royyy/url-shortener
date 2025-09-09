package com.example.demo.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
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

    @PostMapping("/shorten")
    public ResponseEntity<?> shortenUrl(@RequestBody ShortenRequest request) {
        String url = request.getUrl();
        String code = request.getCustomCode();
        String expiryStr = request.getExpiry();

        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().body("URL cannot be empty");
        }

        try {
            new URI(url);
        } catch (URISyntaxException e) {
            return ResponseEntity.badRequest().body("Invalid URL format");
        }

        LocalDateTime expiryDate = null;
        if (expiryStr != null && !expiryStr.isBlank()) {
            try {
                expiryDate = LocalDateTime.parse(expiryStr);
            } catch (DateTimeParseException e) {
                return ResponseEntity.badRequest().body("Invalid expiry date format");
            }
        }

        try {
            UrlMapping mapping;
            if (code != null && !code.isBlank()) {
                mapping = service.createLink(url, code, expiryDate);
            } else {
                mapping = service.createLink(url, expiryDate);
            }
            String shortUrl = String.format("http://localhost:8081/api/u/%s", mapping.getShortCode());
            return ResponseEntity.ok(new ShortenResponse(shortUrl));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/u/{code}")
    public ResponseEntity<?> redirect(@PathVariable String code) {
        Optional<UrlMapping> mapping = service.getByCode(code);
        if (mapping.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Short URL not found");
        }

        service.incrementClicks(code);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(mapping.get().getOriginalUrl()));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/analytics/{code}")
    public ResponseEntity<?> analytics(@PathVariable String code) {
        Optional<UrlMapping> mapping = service.getByCode(code);
        if (mapping.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Short URL not found");
        }
        UrlMapping m = mapping.get();
        AnalyticsResponse response = new AnalyticsResponse(
            m.getOriginalUrl(),
            m.getShortCode(),
            m.getClickCount(),
            m.getCreatedAt()
        );
        return ResponseEntity.ok(response);
    }
}
