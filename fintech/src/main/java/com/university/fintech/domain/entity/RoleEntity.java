package com.university.fintech.domain.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import com.university.fintech.domain.audit.BaseAuditing;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity extends BaseAuditing {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(length = 100)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    private String status;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permission",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<PermissionEntity> permissions = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
        if (status == null) {
            status = "ACTIVE";
        }
    }
}
