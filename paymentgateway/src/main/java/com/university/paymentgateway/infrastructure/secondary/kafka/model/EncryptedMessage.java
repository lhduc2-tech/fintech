package com.university.paymentgateway.infrastructure.secondary.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EncryptedMessage {
    private String iv;
    private String ciphertext;
}
