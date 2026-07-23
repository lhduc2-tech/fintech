package com.university.coreservice.infrastructure.secondary.jpa.repository;

import com.university.coreservice.domain.entity.BankEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankRepository extends JpaRepository<BankEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BankEntity b WHERE b.bankCode = :bankCode AND b.accountNumber = :accountNumber")
    Optional<BankEntity> findByBankCodeAndAccountNumberForUpdate(
            @Param("bankCode") String bankCode,
            @Param("accountNumber") String accountNumber
    );
}
