package com.aegispay.ingestion.api;

import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Fixed-window rate limiter backed by Redis (INCR + EXPIRE, atomic via MULTI
 * is unnecessary — a 1-request overage inside the window is acceptable).
 * Keyed by client IP; swap for mTLS identity in production.
 */
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final org.springframework.data.redis.core.StringRedisTemplate redis;

    @Value("${aegis.ingestion.rate-limit.requests-per-minute-per-user:120}")
    private int requestsPerMinute;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api/v1/transactions")) {
            chain.doFilter(request, response);
            return;
        }
        String window = java.time.LocalDate.now() + ":" + (System.currentTimeMillis() / 60_000);
        String key = "ratelimit:" + clientIp(request) + ":" + window;
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redis.expire(key, Duration.ofSeconds(70));
        }
        if (count != null && count > requestsPerMinute) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/problem+json");
            response.getWriter().write(
                    "{\"type\":\"https://aegispay.io/problems/rate-limited\",\"title\":\"Too many requests\","
                            + "\"status\":429,\"detail\":\"Rate limit exceeded\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        var forwarded = request.getHeader("X-Forwarded-For");
        return forwarded != null ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
