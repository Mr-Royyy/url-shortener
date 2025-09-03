package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.ShortLink;

public interface ShortLinkRepository extends JpaRepository<ShortLink, Long> {

    // ✅ Find by short code
    @Query("SELECT s FROM ShortLink s WHERE s.shortCode = :shortCode")
    Optional<ShortLink> findByShortCode(@Param("shortCode") String shortCode);

    // ✅ Check if short code exists
    @Query("SELECT COUNT(s) > 0 FROM ShortLink s WHERE s.shortCode = :shortCode")
    boolean existsByShortCode(@Param("shortCode") String shortCode);
}
