package com.pgm.lessor.config;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /** Defaults keep local/docker-compose Kafka (no auth) working unchanged; a
     * managed broker (e.g. Aiven) needs SASL_SSL with credentials it supplies. */
    @Value("${spring.kafka.security.protocol:PLAINTEXT}")
    private String securityProtocol;

    @Value("${spring.kafka.sasl.mechanism:}")
    private String saslMechanism;

    @Value("${spring.kafka.sasl.jaas.config:}")
    private String saslJaasConfig;

    /** PEM content of a broker CA cert not already in the JVM's default trust
     * store (e.g. Aiven's project-specific CA) - inline via KIP-651 rather than
     * needing a truststore file baked into the image or mounted as a volume. */
    @Value("${spring.kafka.ssl.trust-store-certificates:}")
    private String sslTruststoreCertificates;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        if (!"PLAINTEXT".equals(securityProtocol)) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
            props.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
            props.put(SaslConfigs.SASL_JAAS_CONFIG, saslJaasConfig);
            if (!sslTruststoreCertificates.isBlank()) {
                props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "PEM");
                props.put(SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG, sslTruststoreCertificates);
            }
        }
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
