package com.example.demo.controller;

import com.example.demo.model.ShortLink;
import com.example.demo.service.ShortLinkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api")
public class ShortLinkController {
    private final ShortLinkService service;

    public ShortLinkController(ShortLinkService service) {
        this.service = service;
    }

    // POST /api/shorten
    @PostMapping("/shorten")
    public ResponseEntity<String> shortenUrl(@RequestBody String url) {
        ShortLink link = service.createShortLink(url);
        return ResponseEntity.ok("http://localhost:8080/" + link.getShortCode());
    }

    // GET /{shortCode}
@GetMapping("/{shortCode}")
public ResponseEntity<Object> redirect(@PathVariable String shortCode) {
    return service.getByCode(shortCode)
            .map(link -> ResponseEntity.status(302).location(URI.create(link.getOriginalUrl())).build())
            .orElse(ResponseEntity.notFound().build());
}

}
