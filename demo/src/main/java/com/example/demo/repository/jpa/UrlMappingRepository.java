package com.example.demo.repository.jpa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.UrlMapping;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {
    Optional<UrlMapping> findByOriginalUrl(String url);
    Optional<UrlMapping> findByShortCode(String code);
    List<UrlMapping> findAllByExpiryDateBefore(LocalDateTime now);
}
