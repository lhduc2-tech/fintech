package com.university.fintech.infrastructure.secondary.jpa.repository;

import com.university.fintech.domain.entity.LinkedBankAccountEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LinkedBankAccountRepository extends BaseJPARepository<LinkedBankAccountEntity, UUID> {

    @Query("select bankAccount from LinkedBankAccountEntity bankAccount where bankAccount.wallet.user.id = :userId order by bankAccount.createdAt desc")
    List<LinkedBankAccountEntity> findAllByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId);

    @Query("select bankAccount from LinkedBankAccountEntity bankAccount where bankAccount.id = :id and bankAccount.wallet.user.id = :userId")
    Optional<LinkedBankAccountEntity> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    boolean existsByWalletIdAndBankId(UUID walletId, UUID bankId);

    boolean existsByBankId(UUID bankId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select bankAccount from LinkedBankAccountEntity bankAccount where bankAccount.id = :id and bankAccount.wallet.user.id = :userId")
    Optional<LinkedBankAccountEntity> findByIdAndUserIdForUpdate(@Param("id") UUID id, @Param("userId") UUID userId);
}
