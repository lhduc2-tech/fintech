package com.university.fintech.infrastructure.secondary.jpa.repository;

import com.university.fintech.domain.entity.WalletTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletTransactionRepository extends BaseJPARepository<WalletTransactionEntity, UUID> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    Optional<WalletTransactionEntity> findByIdempotencyKey(String idempotencyKey);

    Page<WalletTransactionEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Optional<WalletTransactionEntity> findByIdAndUserId(UUID id, UUID userId);
}
