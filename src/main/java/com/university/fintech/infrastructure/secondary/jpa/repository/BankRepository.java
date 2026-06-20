package com.university.fintech.infrastructure.secondary.jpa.repository;

import com.university.fintech.domain.entity.BankEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankRepository extends BaseJPARepository<BankEntity, UUID> {

    Optional<BankEntity> findByBankCodeAndAccountNumber(String bankCode, String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select bank from BankEntity bank where bank.id = :id")
    Optional<BankEntity> findByIdForUpdate(@Param("id") UUID id);
}
