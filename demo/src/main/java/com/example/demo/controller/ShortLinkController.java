package com.example.demo.controller;

import com.example.demo.model.ShortLink;
import com.example.demo.service.ShortLinkService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class ShortLinkController {

    private final ShortLinkService service;

    public ShortLinkController(ShortLinkService service) {
        this.service = service;
    }

    // ✅ Home page
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // ✅ Handle shorten request
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

    // ✅ Redirect handler
    @GetMapping("/{shortCode}")
    public String redirect(@PathVariable String shortCode) {
        Optional<ShortLink> link = service.getByShortCode(shortCode);

        if (link.isEmpty()) {
            return "error";
        }

        service.incrementClickCount(shortCode);
        return "redirect:" + link.get().getOriginalUrl();
    }

    // ✅ Analytics page
    @GetMapping("/analytics")
    public String analytics(@RequestParam(value = "code", required = false) String code, Model model) {
        if (code == null || code.isBlank()) {
            return "analytics"; // show empty form first
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

    // ✅ About page
    @GetMapping("/about")
    public String about() {
        return "about";
    }
}
