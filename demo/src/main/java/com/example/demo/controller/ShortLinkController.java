package com.example.demo.controller;

import com.example.demo.model.ShortLink;
import com.example.demo.service.ShortLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class ShortLinkController {

    @Autowired
    private ShortLinkService shortLinkService;

    // Homepage - form to submit a URL
    @GetMapping("/")
    public String home() {
        return "index"; // maps to src/main/resources/templates/index.html
    }

    // Handle form submission
    @PostMapping("/shorten")
    public String shortenUrl(@RequestParam("url") String url, Model model) {
        ShortLink shortLink = shortLinkService.createShortLink(url);
        model.addAttribute("shortLink", shortLink);
        return "result"; // maps to src/main/resources/templates/result.html
    }

    // Redirect from short code
    @GetMapping("/{shortCode}")
    public String redirectToUrl(@PathVariable String shortCode) {
        Optional<ShortLink> shortLinkOptional = shortLinkService.getByShortCode(shortCode);

        if (shortLinkOptional.isPresent()) {
            ShortLink shortLink = shortLinkOptional.get();
            shortLinkService.incrementClickCount(shortLink);
            return "redirect:" + shortLink.getOriginalUrl();
        } else {
            return "error"; // show error page if code not found
        }
    }
}
