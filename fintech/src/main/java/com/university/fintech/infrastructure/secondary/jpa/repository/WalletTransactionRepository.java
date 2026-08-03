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
import java.util.List;
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

    /**
     * Tìm tất cả giao dịch PENDING được tạo trước thời điểm cutoff.
     * Không dùng PESSIMISTIC_WRITE ở đây — plain SELECT để không yêu cầu transaction.
     * Row-level lock được thực hiện bên trong PendingTransactionTimeoutProcessor
     * khi xử lý từng giao dịch riêng lẻ trong @Transactional riêng.
     */
    @Query("""
            select t from WalletTransactionEntity t
            where t.status = 'PENDING'
              and t.createdAt < :cutoff
              and t.type in :types
            """)
    List<WalletTransactionEntity> findPendingTransactionsCreatedBefore(
            @Param("cutoff") OffsetDateTime cutoff,
            @Param("types") Collection<String> types
    );

    long countByCreatedAtBetween(OffsetDateTime from, OffsetDateTime to);

    long countByStatusAndCreatedAtBetween(String status, OffsetDateTime from, OffsetDateTime to);

    long countBySuspiciousTrueAndCreatedAtBetween(OffsetDateTime from, OffsetDateTime to);

    long countByTypeAndCreatedAtBetween(String type, OffsetDateTime from, OffsetDateTime to);

    long countByTypeAndStatusAndCreatedAtBetween(String type, String status, OffsetDateTime from, OffsetDateTime to);

    @Query("select coalesce(sum(t.amount), 0) from WalletTransactionEntity t where t.createdAt >= :from and t.createdAt <= :to")
    BigDecimal sumTotalAmountBetween(@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    @Query("select coalesce(sum(t.amount), 0) from WalletTransactionEntity t where t.status = :status and t.createdAt >= :from and t.createdAt <= :to")
    BigDecimal sumAmountByStatusAndCreatedAtBetween(@Param("status") String status, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    @Query("select coalesce(sum(t.amount), 0) from WalletTransactionEntity t where t.type = :type and t.status = :status and t.createdAt >= :from and t.createdAt <= :to")
    BigDecimal sumAmountByTypeAndStatusAndCreatedAtBetween(@Param("type") String type, @Param("status") String status, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    @Query("select t from WalletTransactionEntity t left join fetch t.user where t.status in :statuses and t.createdAt >= :from and t.createdAt <= :to order by t.createdAt desc")
    List<WalletTransactionEntity> findErrorTransactionsBetween(@Param("statuses") Collection<String> statuses, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);
}
