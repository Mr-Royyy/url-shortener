package com.example.demo.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
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

    @GetMapping("/")
    public String index(Model model, @RequestParam(value = "errorMessage", required = false) String errorMessage) {
        if (errorMessage != null)
            model.addAttribute("errorMessage", errorMessage);
        return "index";
    }

    @PostMapping("/shorten")
    public String shortenUrl(@RequestParam String url, @RequestParam(required = false) String customCode,
                             @RequestParam(required = false) String expiry, Model model) {
        if (url == null || url.isBlank()) {
            model.addAttribute("errorMessage", "URL cannot be empty");
            return "index";
        }
        try {
            new URI(url);
        } catch (URISyntaxException e) {
            model.addAttribute("errorMessage", "Invalid URL format");
            return "index";
        }
        LocalDateTime expiryDate = null;
        if (expiry != null && !expiry.isBlank()) {
            try {
                expiryDate = LocalDateTime.parse(expiry);
            } catch (DateTimeParseException e) {
                model.addAttribute("errorMessage", "Invalid expiry format");
                return "index";
            }
        }
        try {
            UrlMapping mapping;
            if (customCode != null && !customCode.isBlank()) {
                mapping = service.createLink(url, customCode, expiryDate);
            } else {
                mapping = service.createLink(url, expiryDate);
            }
            model.addAttribute("shortLink", mapping);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "index";
    }

    @GetMapping("/api/u/{code}")
    public String redirect(@PathVariable String code) {
        Optional<UrlMapping> opt = service.getByCode(code);
        if (opt.isEmpty()) {
            return "redirect:/?errorMessage=Short URL not found";
        }
        service.incrementClicks(code);
        return "redirect:" + opt.get().getOriginalUrl();
    }

    @GetMapping("/analytics")
    public String analyticsPage() {
        return "analytics";
    }

    @GetMapping("/analytics/check")
    public String checkAnalytics(@RequestParam String code, Model model) {
        Optional<UrlMapping> opt = service.getByCode(code);
        if (opt.isEmpty()) {
            model.addAttribute("errorMessage", "Short URL not found");
        } else {
            model.addAttribute("shortLink", opt.get());
        }
        return "analytics";
    }

    @GetMapping("/error")
    public String handleError(Model model) {
        model.addAttribute("errorMessage", "Page not found");
        return "index";
    }
}
