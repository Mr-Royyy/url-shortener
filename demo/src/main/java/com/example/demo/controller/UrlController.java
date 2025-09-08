package com.example.demo.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.UrlMapping;
import com.example.demo.service.UrlShortenerService;

@Controller
public class UrlController {

    private final UrlShortenerService service;

    public UrlController(UrlShortenerService service) {
        this.service = service;
    }

    // Home page
    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(value = "errorMessage", required = false) String errorMessage) {
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
        }
        return "index";
    }

    // Shorten URL
    @PostMapping("/shorten")
    public String shortenUrl(@RequestParam("url") String originalUrl, Model model) {
        if (originalUrl == null || originalUrl.isBlank()) {
            model.addAttribute("errorMessage", "URL cannot be empty");
            return "index";
        }

        try {
            new URI(originalUrl);
        } catch (URISyntaxException e) {
            model.addAttribute("errorMessage", "Invalid URL format");
            return "index";
        }

        UrlMapping mapping = service.createShortLink(originalUrl);
        model.addAttribute("shortLink", mapping);
        return "index";
    }

    // Redirect short URL
    @GetMapping("/api/u/{shortCode}")
    public String redirectShortUrl(@PathVariable String shortCode) {
        Optional<UrlMapping> link = service.getByShortCode(shortCode);
        if (link.isEmpty()) {
            return "redirect:/?errorMessage=Short URL not found";
        }

        service.incrementClickCount(shortCode);
        return "redirect:" + link.get().getOriginalUrl();
    }

    // Analytics page
    @GetMapping("/analytics")
    public String analyticsPage() {
        return "analytics";
    }

    // Check analytics for a short code
    @GetMapping("/analytics/check")
    public String checkAnalytics(@RequestParam("code") String shortCode, Model model) {
        Optional<UrlMapping> link = service.getByShortCode(shortCode);
        if (link.isEmpty()) {
            model.addAttribute("errorMessage", "Short URL not found");
        } else {
            model.addAttribute("shortLink", link.get());
        }
        return "analytics";
    }

    // Fallback error mapping
    @GetMapping("/error")
    public String handleError(Model model) {
        model.addAttribute("errorMessage", "Page not found");
        return "index";
    }
}
