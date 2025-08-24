package com.example.demo.repository;

import com.example.demo.model.ShortLink;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ShortLinkRepository extends JpaRepository<ShortLink, Long> {
    Optional<ShortLink> findByShortCode(String shortCode);
}
