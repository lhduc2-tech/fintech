package com.university.fintech.infrastructure.secondary.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.fintech.infrastructure.secondary.kafka.model.EncryptedMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM payload encryptor/decryptor cho Kafka messages.
 * Cả fintech và paymentgateway dùng chung KAFKA_PAYLOAD_SECRET_KEY.
 */
@Slf4j
@Component
public class KafkaPayloadEncryptor {

    private static final String AES_GCM_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final SecretKeySpec secretKey;
    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    public KafkaPayloadEncryptor(
            @Value("${application.kafka.payload-secret-key:}") String base64Key,
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
        if (base64Key == null || base64Key.isBlank()) {
            log.warn("KAFKA_PAYLOAD_SECRET_KEY is not set. Encryption will fail at runtime.");
            this.secretKey = null;
        } else {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            this.secretKey = new SecretKeySpec(keyBytes, "AES");
        }
    }

    /**
     * Serialize object → JSON, rồi mã hóa bằng AES-256-GCM.
     * @return JSON string của EncryptedMessage
     */
    public String encrypt(Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(json.getBytes("UTF-8"));

            EncryptedMessage envelope = EncryptedMessage.builder()
                    .iv(Base64.getEncoder().encodeToString(iv))
                    .ciphertext(Base64.getEncoder().encodeToString(ciphertext))
                    .build();
            return objectMapper.writeValueAsString(envelope);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt Kafka payload", e);
        }
    }

    /**
     * Giải mã JSON string của EncryptedMessage → deserialize thành targetType.
     */
    public <T> T decrypt(String encryptedJson, Class<T> targetType) {
        try {
            EncryptedMessage envelope = objectMapper.readValue(encryptedJson, EncryptedMessage.class);
            byte[] iv = Base64.getDecoder().decode(envelope.getIv());
            byte[] ciphertext = Base64.getDecoder().decode(envelope.getCiphertext());

            Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] plaintext = cipher.doFinal(ciphertext);

            return objectMapper.readValue(new String(plaintext, "UTF-8"), targetType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt Kafka payload", e);
        }
    }
}
