package com.university.coreservice.infrastructure.secondary.jpa.adapter;

import com.university.coreservice.domain.entity.BankEntity;
import com.university.coreservice.domain.port.secondary.BankRepositoryPort;
import com.university.coreservice.infrastructure.secondary.jpa.repository.BankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BankJPAAdapter implements BankRepositoryPort {

    private final BankRepository bankRepository;

    @Override
    public Optional<BankEntity> findByBankCodeAndAccountNumberForUpdate(String bankCode, String accountNumber) {
        return bankRepository.findByBankCodeAndAccountNumberForUpdate(bankCode, accountNumber);
    }

    @Override
    public BankEntity save(BankEntity bank) {
        return bankRepository.save(bank);
    }
}
