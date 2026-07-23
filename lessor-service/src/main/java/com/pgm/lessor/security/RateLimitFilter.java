package com.pgm.lessor.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

/**
 * Per-IP request throttling for the two classes of endpoint this service exposes
 * with no auth barrier at all: /auth/** (brute-forceable login/register) and
 * /api/public/** (unauthenticated, internet-facing discovery). Everything else is
 * already behind a JWT, so it isn't in scope here - a compromised token is a
 * TokenDenylistService problem, not a rate-limit one.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiter rateLimiter;
    private final int authLimit;
    private final Duration authWindow;
    private final int publicLimit;
    private final Duration publicWindow;

    public RateLimitFilter(
            RateLimiter rateLimiter,
            @Value("${app.ratelimit.auth.limit}") int authLimit,
            @Value("${app.ratelimit.auth.window-seconds}") long authWindowSeconds,
            @Value("${app.ratelimit.public.limit}") int publicLimit,
            @Value("${app.ratelimit.public.window-seconds}") long publicWindowSeconds) {
        this.rateLimiter = rateLimiter;
        this.authLimit = authLimit;
        this.authWindow = Duration.ofSeconds(authWindowSeconds);
        this.publicLimit = publicLimit;
        this.publicWindow = Duration.ofSeconds(publicWindowSeconds);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        String bucket;
        int limit;
        Duration window;
        if (path.startsWith("/api/lessor/auth/")) {
            bucket = "auth";
            limit = authLimit;
            window = authWindow;
        } else if (path.startsWith("/api/public/")) {
            bucket = "public";
            limit = publicLimit;
            window = publicWindow;
        } else {
            filterChain.doFilter(request, response);
            return;
        }

        String key = "lessor:ratelimit:" + bucket + ":" + clientIp(request);
        if (!rateLimiter.allow(key, limit, window)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(window.toSeconds()));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"status\":429,\"error\":\"Too many requests, try again shortly\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
