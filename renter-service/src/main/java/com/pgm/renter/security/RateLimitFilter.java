package com.pgm.renter.security;

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
 * Per-IP throttling for /auth/** (brute-forceable login/register), the only
 * endpoints this service exposes with no JWT barrier at all. (Unlike lessor-service,
 * this service has no public/** discovery endpoint of its own - it proxies
 * lessor-service's, which enforces its own rate limit.)
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiter rateLimiter;
    private final int authLimit;
    private final Duration authWindow;

    public RateLimitFilter(
            RateLimiter rateLimiter,
            @Value("${app.ratelimit.auth.limit}") int authLimit,
            @Value("${app.ratelimit.auth.window-seconds}") long authWindowSeconds) {
        this.rateLimiter = rateLimiter;
        this.authLimit = authLimit;
        this.authWindow = Duration.ofSeconds(authWindowSeconds);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        if (!path.startsWith("/api/renter/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = "renter:ratelimit:auth:" + clientIp(request);
        if (!rateLimiter.allow(key, authLimit, authWindow)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(authWindow.toSeconds()));
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
