package com.university.fintech.infrastructure.secondary.jpa.repository;

import com.university.fintech.domain.entity.FraudAlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository truy vấn bảng fraud_alerts.
 */
@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlertEntity, UUID>,
        JpaSpecificationExecutor<FraudAlertEntity> {
}
