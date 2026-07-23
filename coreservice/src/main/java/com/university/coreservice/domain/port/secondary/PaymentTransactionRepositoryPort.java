package com.university.coreservice.domain.port.secondary;

import com.university.coreservice.domain.entity.PaymentTransactionEntity;

import java.util.Optional;

/**
 * Secondary (driven) port thao tác với bảng payment_transactions (audit & idempotency).
 */
public interface PaymentTransactionRepositoryPort {

    Optional<PaymentTransactionEntity> findByIdempotencyKey(String idempotencyKey);

    PaymentTransactionEntity save(PaymentTransactionEntity entity);
}
