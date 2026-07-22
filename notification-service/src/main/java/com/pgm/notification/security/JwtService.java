package com.pgm.notification.security;

import com.pgm.notification.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

/**
 * Validation only - this service never issues a token, it just verifies the
 * same HS256 secret (app.jwt.secret) that lessor-service and renter-service
 * sign with, so a token issued by either one is accepted here.
 */
@Service
public class JwtService {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TENANT = "tenant";
    private static final String CLAIM_EMAIL = "email";

    private final SecretKey key;

    public JwtService(@Value("${app.jwt.secret}") String base64Secret) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret));
    }

    public Optional<UserPrincipal> parseAndValidate(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Long userId = Long.valueOf(claims.getSubject());
            Role role = Role.valueOf(claims.get(CLAIM_ROLE, String.class));
            String tenantId = claims.get(CLAIM_TENANT, String.class);
            String email = claims.get(CLAIM_EMAIL, String.class);
            Instant expiresAt = claims.getExpiration().toInstant();
            return Optional.of(new UserPrincipal(userId, email, role, tenantId, claims.getId(), expiresAt));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
