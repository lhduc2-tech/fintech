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

    List<LinkedBankAccountEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<LinkedBankAccountEntity> findByIdAndUserId(UUID id, UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select bankAccount from LinkedBankAccountEntity bankAccount where bankAccount.id = :id and bankAccount.user.id = :userId")
    Optional<LinkedBankAccountEntity> findByIdAndUserIdForUpdate(@Param("id") UUID id, @Param("userId") UUID userId);
}
