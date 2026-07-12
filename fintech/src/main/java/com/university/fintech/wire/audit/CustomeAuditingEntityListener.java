package com.university.fintech.wire.audit;

import com.university.fintech.domain.audit.BaseAuditing;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Component
public class CustomeAuditingEntityListener {

    @PrePersist
    public void prePersist(Object target) {
        if (target instanceof BaseAuditing b) {
            OffsetDateTime now = OffsetDateTime.now(ZoneId.systemDefault());
            if (b.getCreatedAt() == null) b.setCreatedAt(now);
            b.setUpdatedAt(now);
        }
    }

    @PreUpdate
    public void preUpdate(Object target) {
        if (target instanceof BaseAuditing b) {
            b.setUpdatedAt(OffsetDateTime.now(ZoneId.systemDefault()));
        }
    }
}