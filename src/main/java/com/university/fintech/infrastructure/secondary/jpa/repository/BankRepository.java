package com.university.fintech.infrastructure.secondary.jpa.repository;

import com.university.fintech.domain.entity.BankEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankRepository extends BaseJPARepository<BankEntity, UUID> {

    Optional<BankEntity> findByBankCodeAndAccountNumber(String bankCode, String accountNumber);
}
