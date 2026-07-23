package com.pgm.lessor.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

import java.time.Duration;

/**
 * Backs dashboardAggregates/publicHostelListing with Redis instead of the old
 * per-JVM Caffeine cache, so a burst of identical requests hits Postgres once
 * per TTL window across all replicas of this service, not once per replica.
 * Note: without @EnableCaching, @Cacheable never actually engaged (no proxy
 * created) - the Caffeine setup this replaces had the same gap.
 *
 * Uses GenericJacksonJsonRedisSerializer (Jackson 3, tools.jackson.databind) with
 * its own dedicated ObjectMapper rather than the app's shared Jackson 2
 * (com.fasterxml.jackson.databind) ObjectMapper bean (see JacksonConfig) - that bean
 * is also used to build Kafka payloads, and this serializer needs default-typing
 * enabled for polymorphic deserialization, which would leak "@class" fields into the
 * Kafka JSON contract if applied to a shared mapper. The PolymorphicTypeValidator is
 * scoped to this service's own DTO/JDK collection types rather than
 * enableUnsafeDefaultTyping(), since default-typing deserialization of arbitrary
 * classes is a known gadget-chain risk.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.pgm.lessor.dto.")
                .allowIfSubType("java.util.")
                .build();
        GenericJacksonJsonRedisSerializer valueSerializer = GenericJacksonJsonRedisSerializer.builder()
                .enableDefaultTyping(typeValidator)
                .build();
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(30))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer));
    }
}
