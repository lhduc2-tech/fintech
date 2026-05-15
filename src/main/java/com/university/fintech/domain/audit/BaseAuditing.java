package com.university.fintech.domain.audit;

import com.university.fintech.wire.audit.CustomeAuditingEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.OffsetDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners({AuditingEntityListener.class, CustomeAuditingEntityListener.class})
public abstract class BaseAuditing {

    @Column(updatable = false)
    private OffsetDateTime createdAt = Instant.now().atOffset(OffsetDateTime.now().getOffset());

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    private OffsetDateTime updatedAt = Instant.now().atOffset(OffsetDateTime.now().getOffset());

    @LastModifiedBy
    private String updatedBy;
}