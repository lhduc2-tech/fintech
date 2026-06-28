package com.university.fintech.infrastructure.secondary.jpa.repository;

import com.university.fintech.domain.entity.BankEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankRepository extends BaseJPARepository<BankEntity, UUID> {

    Optional<BankEntity> findByBankCodeAndAccountNumber(String bankCode, String accountNumber);

    @Query("select distinct bank.bankCode from BankEntity bank where bank.status = :status order by bank.bankCode")
    List<String> findDistinctBankCodesByStatusOrderByBankCode(@Param("status") String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select bank from BankEntity bank where bank.id = :id")
    Optional<BankEntity> findByIdForUpdate(@Param("id") UUID id);
}
