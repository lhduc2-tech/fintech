package com.university.coreservice.domain.port.secondary;

import com.university.coreservice.domain.entity.BankEntity;

import java.util.Optional;

/**
 * Secondary (driven) port thao tác với bảng banks.
 */
public interface BankRepositoryPort {

    Optional<BankEntity> findByBankCodeAndAccountNumberForUpdate(String bankCode, String accountNumber);

    BankEntity save(BankEntity bank);
}
