package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.ShortLink;

public interface ShortLinkRepository extends JpaRepository<ShortLink, Long> {

    // ✅ Find by short code
    Optional<ShortLink> findByShortCode(String shortCode);

    // ✅ Exists check
    boolean existsByShortCode(String shortCode);
}
