package com.university.paymentgateway.wire.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String consumerGroupId;

    @Value("${KAFKA_SASL_USERNAME:}")
    private String saslUsername;

    @Value("${KAFKA_SASL_PASSWORD:}")
    private String saslPassword;

    @Value("${KAFKA_CA_CERT_PATH:kafka-ca.pem}")
    private String caCertPath;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }


    private String resolveCaCertPath(String path) {
        if (path != null && !path.isBlank()) {
            java.io.File f = new java.io.File(path);
            if (f.exists()) return f.getAbsolutePath();
            java.io.File sub = new java.io.File("paymentgateway/" + path);
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
                "org.apache.kafka.common.security.scram.ScramLoginModule required " +
                        "username=\"%s\" password=\"%s\";",
                saslUsername, saslPassword
        ));
        props.put("ssl.truststore.type", "PEM");
        props.put("ssl.truststore.location", resolveCaCertPath(caCertPath));
        return props;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = commonKafkaProperties();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
}
