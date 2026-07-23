package com.pgm.lessor.config;

import com.pgm.lessor.security.JwtAuthenticationFilter;
import com.pgm.lessor.security.JwtService;
import com.pgm.lessor.security.RateLimitFilter;
import com.pgm.lessor.security.TokenDenylistService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtService jwtService;
    private final TokenDenylistService tokenDenylistService;
    private final RateLimitFilter rateLimitFilter;
    private final String allowedOrigins;

    public SecurityConfig(
            JwtService jwtService,
            TokenDenylistService tokenDenylistService,
            RateLimitFilter rateLimitFilter,
            @Value("${app.cors.allowed-origins}") String allowedOrigins) {
        this.jwtService = jwtService;
        this.tokenDenylistService = tokenDenylistService;
        this.rateLimitFilter = rateLimitFilter;
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/lessor/auth/register", "/api/lessor/auth/login", "/api/lessor/auth/refresh").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, ex) -> writeJsonError(response, HttpStatus.UNAUTHORIZED, "Authentication required"))
                        .accessDeniedHandler((request, response, ex) -> writeJsonError(response, HttpStatus.FORBIDDEN, "Access denied")))
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, tokenDenylistService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private void writeJsonError(jakarta.servlet.http.HttpServletResponse response, HttpStatus status, String message) throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"status\":%d,\"error\":\"%s\"}".formatted(status.value(), message));
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
