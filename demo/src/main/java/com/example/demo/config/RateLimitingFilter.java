package com.example.demo.config;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.example.demo.exception.TooManyRequestsException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitingFilter extends HttpFilter {

    private static final long LIMIT_WINDOW_MILLIS = 60 * 1000; // 1 minute
    private static final int MAX_REQUESTS_PER_WINDOW = 30; // Limit per IP

    private final Map<String, ClientRequestInfo> requestCounts = new ConcurrentHashMap<>();

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String clientIp = request.getRemoteAddr();
        long currentTime = Instant.now().toEpochMilli();

        ClientRequestInfo info = requestCounts.compute(clientIp, (ip, existing) -> {
            if (existing == null || currentTime - existing.windowStart >= LIMIT_WINDOW_MILLIS) {
                return new ClientRequestInfo(1, currentTime);
            } else {
                existing.requestCount++;
                return existing;
            }
        });

        if (info.requestCount > MAX_REQUESTS_PER_WINDOW) {
            throw new TooManyRequestsException("Rate limit exceeded. Try again later.");
        }

        chain.doFilter(request, response);
    }

    private static class ClientRequestInfo {
        int requestCount;
        long windowStart;

        ClientRequestInfo(int requestCount, long windowStart) {
            this.requestCount = requestCount;
            this.windowStart = windowStart;
        }
    }
}
