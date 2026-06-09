package com.university.fintech.infrastructure.secondary.jpa.repository;

import com.university.fintech.domain.entity.WalletEntity;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WalletRepository extends BaseJPARepository<WalletEntity, UUID> {

    boolean existsByUserId(UUID userId);
}
