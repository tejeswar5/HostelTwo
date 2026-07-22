package com.pgm.renter.service;

import com.pgm.renter.dto.auth.AuthResponse;
import com.pgm.renter.dto.auth.LoginRequest;
import com.pgm.renter.dto.auth.RefreshRequest;
import com.pgm.renter.dto.auth.RegisterRenterRequest;
import com.pgm.renter.entity.RefreshToken;
import com.pgm.renter.entity.Role;
import com.pgm.renter.entity.User;
import com.pgm.renter.exception.ConflictException;
import com.pgm.renter.exception.ForbiddenException;
import com.pgm.renter.exception.NotFoundException;
import com.pgm.renter.repository.RefreshTokenRepository;
import com.pgm.renter.repository.UserRepository;
import com.pgm.renter.security.JwtService;
import com.pgm.renter.security.TokenDenylistService;
import com.pgm.renter.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final TokenDenylistService tokenDenylistService;
    private final PasswordEncoder passwordEncoder;
    private final long refreshTokenTtlDays;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService,
            TokenDenylistService tokenDenylistService,
            PasswordEncoder passwordEncoder,
            @Value("${app.jwt.refresh-token-ttl-days}") long refreshTokenTtlDays) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.tokenDenylistService = tokenDenylistService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenTtlDays = refreshTokenTtlDays;
    }

    @Transactional
    public AuthResponse register(RegisterRenterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("An account with this email already exists");
        }
        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new ConflictException("An account with this phone number already exists");
        }
        User user = User.builder()
                .fname(request.fname())
                .lname(request.lname())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.RENTER)
                .tenantId(java.util.UUID.randomUUID().toString())
                .active(true)
                .build();
        user = userRepository.save(user);
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ForbiddenException("Invalid email or password"));
        if (!user.isActive()) {
            throw new ForbiddenException("This account has been deactivated");
        }
        if (user.getRole() != Role.RENTER) {
            throw new ForbiddenException("Use the lessor app to sign in with this account");
        }
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ForbiddenException("Invalid email or password");
        }
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        String hash = sha256(request.refreshToken());
        RefreshToken token = refreshTokenRepository.findByTokenHashAndRevokedFalse(hash)
                .orElseThrow(() -> new ForbiddenException("Refresh token is invalid or expired"));
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ForbiddenException("Refresh token is invalid or expired");
        }
        token.setRevoked(true);
        refreshTokenRepository.save(token);
        User user = userRepository.findById(token.getUser().getId())
                .orElseThrow(() -> new NotFoundException("User no longer exists"));
        return issueTokens(user);
    }

    @Transactional
    public void logout(UserPrincipal principal, String rawRefreshToken) {
        tokenDenylistService.revoke(principal.jti(), principal.expiresAt());
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokenRepository.findByTokenHashAndRevokedFalse(sha256(rawRefreshToken))
                    .ifPresent(token -> {
                        token.setRevoked(true);
                        refreshTokenRepository.save(token);
                    });
        }
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole(), user.getTenantId());
        String rawRefreshToken = jwtService.newOpaqueRefreshToken();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(sha256(rawRefreshToken))
                .expiresAt(LocalDateTime.now().plusDays(refreshTokenTtlDays))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);
        return new AuthResponse(accessToken, rawRefreshToken, user.getId(), user.getFname(), user.getLname(), user.getEmail(), user.getRole());
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
