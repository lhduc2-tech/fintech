package com.university.coreservice.wire.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${KAFKA_SASL_USERNAME:}")
    private String saslUsername;

    @Value("${KAFKA_SASL_PASSWORD:}")
    private String saslPassword;

    @Value("${KAFKA_CA_CERT_PATH:ca.pem}")
    private String caCertPath;

    @Value("${application.kafka.payment-result-topic:payment.result}")
    private String paymentResultTopic;

    @Value("${application.kafka.payment-mail-topic:payment.mail}")
    private String paymentMailTopic;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    private String resolveCaCertPath(String path) {
        if (path != null && !path.isBlank()) {
            java.io.File f = new java.io.File(path);
            if (f.exists()) return f.getAbsolutePath();
            java.io.File sub = new java.io.File("coreservice/" + path);
            if (sub.exists()) return sub.getAbsolutePath();
        }
        java.io.File rootCa = new java.io.File("ca.pem");
        if (rootCa.exists()) return rootCa.getAbsolutePath();
        return path;
    }

    private Map<String, Object> commonKafkaProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.mechanism", "SCRAM-SHA-256");
        props.put("sasl.jaas.config", String.format(
                "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";",
                saslUsername, saslPassword
        ));
        props.put("ssl.truststore.type", "PEM");
        props.put("ssl.truststore.location", resolveCaCertPath(caCertPath));
        return props;
    }

    @Bean
    public KafkaAdmin kafkaAdmin() {
        return new KafkaAdmin(commonKafkaProperties());
    }

    @Bean
    public NewTopic createPaymentResultTopic() {
        return TopicBuilder.name(paymentResultTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic createPaymentMailTopic() {
        return TopicBuilder.name(paymentMailTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> props = commonKafkaProperties();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
