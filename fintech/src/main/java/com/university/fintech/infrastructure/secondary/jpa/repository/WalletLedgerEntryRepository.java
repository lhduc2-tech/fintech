package com.university.fintech.infrastructure.secondary.jpa.repository;

import com.university.fintech.domain.entity.WalletLedgerEntryEntity;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WalletLedgerEntryRepository extends BaseJPARepository<WalletLedgerEntryEntity, UUID> {
}
