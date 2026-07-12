package com.university.fintech.infrastructure.secondary.jpa.repository;

import com.university.fintech.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends BaseJPARepository<UserEntity, UUID> {

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<UserEntity> findByPhoneNumber(String phoneNumber);

    Optional<UserEntity> findByEmail(String email);
}
