package com.pgm.renter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot 4.1 auto-configures a Jackson 3 JsonMapper by default, not a
 * classic com.fasterxml.jackson.databind.ObjectMapper - EventPublisher and the
 * Kafka listeners use the latter directly (plain JSON strings over Kafka, not
 * Spring's HTTP message converters), so it has to be provided explicitly here.
 * JavaTimeModule is required for LocalDate fields (e.g. requestedCheckIn) to
 * serialize as plain ISO strings instead of failing outright.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }
}
