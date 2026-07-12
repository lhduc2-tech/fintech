package com.university.fintech.infrastructure.secondary.jpa.repository;

import com.university.fintech.domain.entity.WalletTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletTransactionRepository extends BaseJPARepository<WalletTransactionEntity, UUID> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    boolean existsByLinkedBankAccountIdAndStatus(UUID linkedBankAccountId, String status);

    Optional<WalletTransactionEntity> findByIdempotencyKey(String idempotencyKey);

    Optional<WalletTransactionEntity> findByReferenceCodeAndType(String referenceCode, String type);

    Page<WalletTransactionEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Optional<WalletTransactionEntity> findByIdAndUserId(UUID id, UUID userId);

    @Query("""
            select coalesce(sum(transaction.amount), 0)
            from WalletTransactionEntity transaction
            where transaction.user.id = :userId
              and transaction.type in :types
              and transaction.status in :statuses
              and transaction.createdAt >= :from
              and transaction.createdAt < :to
            """)
    BigDecimal sumAmountByUserIdAndTypeInAndStatusInAndCreatedAtBetween(
            @Param("userId") UUID userId,
            @Param("types") Collection<String> types,
            @Param("statuses") Collection<String> statuses,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );
}
