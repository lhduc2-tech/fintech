package com.university.fintech.infrastructure.secondary.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@SuppressWarnings("all")
@NoRepositoryBean
public interface BaseJPARepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
}