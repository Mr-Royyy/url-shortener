package com.example.demo.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.ShortLink;
import com.example.demo.service.ShortLinkService;

@Controller
public class ShortLinkController {

    private final ShortLinkService service;

    public ShortLinkController(ShortLinkService service) {
        this.service = service;
    }

    // Home
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // Web form shorten (POST)
    @PostMapping("/shorten")
    public String shortenUrl(@RequestParam("url") String originalUrl, Model model) {
        Optional<ShortLink> result = service.createShortLink(originalUrl);

        if (result.isEmpty()) {
            model.addAttribute("errorMessage", "Invalid URL. Please enter a valid link (e.g., https://example.com)");
            return "error";
        }

        ShortLink shortLink = result.get();
        model.addAttribute("shortUrl", "http://localhost:8080/" + shortLink.getShortCode());
        model.addAttribute("clickCount", shortLink.getClickCount());
        return "result";
    }

    // Redirect (Web)
    @GetMapping("/{shortCode}") // This is fine as long as /api/u/{shortCode} has no overlap
    public String redirect(@PathVariable String shortCode) {
        Optional<ShortLink> link = service.getByShortCode(shortCode);

        if (link.isEmpty()) {
            return "error";
        }

        service.incrementClickCount(shortCode);
        return "redirect:" + link.get().getOriginalUrl();
    }

    // Analytics page (Web)
    @GetMapping("/analytics")
    public String analytics(@RequestParam(value = "code", required = false) String code, Model model) {
        if (code == null || code.isBlank()) {
            return "analytics";
        }
        return service.getByShortCode(code)
                .map(link -> {
                    model.addAttribute("shortLink", link);
                    return "analytics";
                })
                .orElseGet(() -> {
                    model.addAttribute("errorMessage", "No link found for short code: " + code);
                    return "analytics";
                });
    }

    // About
    @GetMapping("/about")
    public String about() {
        return "about";
    }
}
