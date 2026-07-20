package com.pgm.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot 4.1 auto-configures a Jackson 3 JsonMapper by default, not a
 * classic com.fasterxml.jackson.databind.ObjectMapper - the Kafka listeners use
 * the latter directly (plain JSON strings over Kafka), so it has to be provided
 * explicitly here.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }
}
