package com.university.fintech.infrastructure.secondary.jpa.repository;

import com.university.fintech.domain.entity.RoleEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends BaseJPARepository<RoleEntity, UUID> {

    Optional<RoleEntity> findByCode(String code);

    java.util.List<RoleEntity> findByCodeIn(java.util.Collection<String> codes);
}
