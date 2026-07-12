package com.university.paymentgateway.infrastructure.jpa.repository;

import com.university.paymentgateway.domain.entity.BankAccountEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccountEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BankAccountEntity b where b.bankCode = :bankCode and b.accountNumber = :accountNumber")
    Optional<BankAccountEntity> findByBankCodeAndAccountNumberForUpdate(
            @Param("bankCode") String bankCode,
            @Param("accountNumber") String accountNumber
    );
}
