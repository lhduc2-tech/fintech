package com.university.fintech.domain.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import com.university.fintech.domain.audit.BaseAuditing;
import com.university.fintech.shared.constant.Constant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletEntity extends BaseAuditing {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Builder.Default
    @Column(name = "available_balance", nullable = false)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "hold_balance", nullable = false)
    private BigDecimal holdBalance = BigDecimal.ZERO;

    private String status;

    @Builder.Default
    @Column(name = "limitation", nullable = false)
    private Integer limitation = 1;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
        if (availableBalance == null) {
            availableBalance = BigDecimal.ZERO;
        }
        if (holdBalance == null) {
            holdBalance = BigDecimal.ZERO;
        }
        if (status == null) {
            status = Constant.STATUS_ACTIVE;
        }
        if (limitation == null) {
            limitation = 1;
        }
    }
}
