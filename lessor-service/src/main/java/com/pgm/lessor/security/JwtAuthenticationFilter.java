package com.pgm.lessor.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final TokenDenylistService tokenDenylistService;

    public JwtAuthenticationFilter(JwtService jwtService, TokenDenylistService tokenDenylistService) {
        this.jwtService = jwtService;
        this.tokenDenylistService = tokenDenylistService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            Optional<UserPrincipal> principal = jwtService.parseAndValidate(token);
            principal
                    .filter(p -> !tokenDenylistService.isRevoked(p.jti()))
                    .ifPresent(p -> SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(p)));
        }
        filterChain.doFilter(request, response);
    }
}
