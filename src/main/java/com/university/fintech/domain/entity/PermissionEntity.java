package com.university.fintech.domain.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import com.university.fintech.domain.audit.BaseAuditing;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionEntity extends BaseAuditing {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(columnDefinition = "text")
    private String description;

    @Builder.Default
    @ManyToMany(mappedBy = "permissions")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<RoleEntity> roles = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
    }
}
