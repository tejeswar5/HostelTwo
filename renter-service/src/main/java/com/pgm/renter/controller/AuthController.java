package com.pgm.renter.controller;

import com.pgm.renter.dto.auth.AuthResponse;
import com.pgm.renter.dto.auth.LoginRequest;
import com.pgm.renter.dto.auth.LogoutRequest;
import com.pgm.renter.dto.auth.RefreshRequest;
import com.pgm.renter.dto.auth.RegisterRenterRequest;
import com.pgm.renter.security.UserPrincipal;
import com.pgm.renter.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/renter/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRenterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserPrincipal principal, @RequestBody(required = false) LogoutRequest request) {
        authService.logout(principal, request == null ? null : request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
