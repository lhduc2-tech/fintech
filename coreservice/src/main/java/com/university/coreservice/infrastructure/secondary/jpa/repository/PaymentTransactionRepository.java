package com.university.coreservice.infrastructure.secondary.jpa.repository;

import com.university.coreservice.domain.entity.PaymentTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, UUID> {

    Optional<PaymentTransactionEntity> findByIdempotencyKey(String idempotencyKey);

    Optional<PaymentTransactionEntity> findByCorrelationId(String correlationId);
}
