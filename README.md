# Quick Link - URL Shortener

A modern URL shortener application built with Java and Spring Boot that enables users to create short, secure links redirecting to original URLs. This project includes a web UI, REST API, analytics, expiry management, caching, and rate limiting.

## Features

- Shorten long URLs into concise, unique codes with optional custom codes and expiry dates
- Redirect shortened URLs to the original destination
- Track click counts and basic analytics for each short link
- Automatic cleanup of expired URLs via scheduled tasks
- High-performance Redis caching layer for faster lookups
- IP-based rate limiting to prevent abuse with HTTP 429 responses
- Responsive web UI built with Thymeleaf and Bootstrap
- REST API support with JSON request and response bodies
- Centralized error handling with clear user feedback and API error messages
- Robust input validation both client- and server-side
- Containerized deployment support with Docker



- Java 24
- Spring Boot 3.5.5 (Web, Data JPA, Data Redis, Scheduling)
- MySQL 8.x for persistent storage
- Redis for caching URL mappings
- Thymeleaf & Bootstrap 5 for frontend UI
- Maven for build and dependencies
- JUnit & Mockito for testing
- SLF4J / Logback for logging

## Under Implementation
- Docker for containerization 
