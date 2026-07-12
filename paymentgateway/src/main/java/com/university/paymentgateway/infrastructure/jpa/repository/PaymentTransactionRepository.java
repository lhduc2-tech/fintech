package com.university.paymentgateway.infrastructure.jpa.repository;

import com.university.paymentgateway.domain.entity.PaymentTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, UUID> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    Optional<PaymentTransactionEntity> findByIdempotencyKey(String idempotencyKey);

    Optional<PaymentTransactionEntity> findByCorrelationId(String correlationId);
}
