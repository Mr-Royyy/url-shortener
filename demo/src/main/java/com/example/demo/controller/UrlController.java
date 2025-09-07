package com.example.demo.controller;

import java.net.URI;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.UrlMapping;
import com.example.demo.service.UrlShortenerService;

@RestController
@RequestMapping("/api") // Prefixes all endpoints with /api
public class UrlController {

    private final UrlShortenerService service;

    public UrlController(UrlShortenerService service) {
        this.service = service;
    }

    // API: Create new short link
    @PostMapping("/shorten")
    public ResponseEntity<?> shorten(@RequestBody Map<String, String> request) {
        String originalUrl = request.get("url");
        if (originalUrl == null || originalUrl.isBlank()) {
            return ResponseEntity.badRequest().body("Missing URL");
        }
        UrlMapping mapping = service.createShortLink(originalUrl);
        return ResponseEntity.ok(Map.of(
                "shortCode", mapping.getShortCode(),
                "shortUrl", "http://localhost:8080/api/u/" + mapping.getShortCode(),
                "originalUrl", mapping.getOriginalUrl()
        ));
    }

    // API: Redirect from short code (API)
    @GetMapping("/u/{shortCode}") // Note the /u prefix
    public ResponseEntity<?> redirect(@PathVariable String shortCode) {
        return service.getOriginalUrl(shortCode)
                .map(url -> ResponseEntity.status(302).location(URI.create(url)).build())
                .orElse(ResponseEntity.notFound().build());
    }
}
