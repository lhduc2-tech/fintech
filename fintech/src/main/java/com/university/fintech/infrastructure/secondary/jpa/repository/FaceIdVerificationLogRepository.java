package com.university.fintech.infrastructure.secondary.jpa.repository;

import com.university.fintech.domain.entity.FaceIdVerificationLogEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FaceIdVerificationLogRepository extends BaseJPARepository<FaceIdVerificationLogEntity, UUID> {

    Optional<FaceIdVerificationLogEntity> findByCorrelationId(String correlationId);

    List<FaceIdVerificationLogEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
