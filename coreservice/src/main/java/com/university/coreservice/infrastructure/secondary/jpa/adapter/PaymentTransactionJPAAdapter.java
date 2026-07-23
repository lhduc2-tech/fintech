package com.university.coreservice.infrastructure.secondary.jpa.adapter;

import com.university.coreservice.domain.entity.PaymentTransactionEntity;
import com.university.coreservice.domain.port.secondary.PaymentTransactionRepositoryPort;
import com.university.coreservice.infrastructure.secondary.jpa.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentTransactionJPAAdapter implements PaymentTransactionRepositoryPort {

    private final PaymentTransactionRepository paymentTransactionRepository;

    @Override
    public Optional<PaymentTransactionEntity> findByIdempotencyKey(String idempotencyKey) {
        return paymentTransactionRepository.findByIdempotencyKey(idempotencyKey);
    }

    @Override
    public PaymentTransactionEntity save(PaymentTransactionEntity entity) {
        return paymentTransactionRepository.save(entity);
    }
}
