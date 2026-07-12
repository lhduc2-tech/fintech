package com.university.fintech.infrastructure.secondary.jpa.repository;

import com.university.fintech.domain.entity.WalletEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends BaseJPARepository<WalletEntity, UUID> {

    boolean existsByUserId(UUID userId);

    Optional<WalletEntity> findByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select wallet from WalletEntity wallet where wallet.user.id = :userId")
    Optional<WalletEntity> findByUserIdForUpdate(@Param("userId") UUID userId);
}
