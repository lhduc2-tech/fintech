package com.university.fintech.infrastructure.secondary.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EncryptedMessage {

    /** Base64-encoded AES-GCM IV (nonce), 12 bytes */
    private String iv;

    /** Base64-encoded AES-GCM ciphertext (includes 16-byte auth tag appended) */
    private String ciphertext;
}
