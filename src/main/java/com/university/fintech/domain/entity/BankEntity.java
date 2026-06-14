package com.university.fintech.domain.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import com.university.fintech.domain.audit.BaseAuditing;
import com.university.fintech.shared.constant.Constant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "banks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankEntity extends BaseAuditing {

    @Id
    private UUID id;

    @Column(name = "bank_code", nullable = false)
    private String bankCode;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "account_holder_name", nullable = false)
    private String accountHolderName;

    @Builder.Default
    @Column(name = "status", nullable = false)
    private String status = Constant.STATUS_ACTIVE;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
        if (status == null) {
            status = Constant.STATUS_ACTIVE;
        }
    }
}
