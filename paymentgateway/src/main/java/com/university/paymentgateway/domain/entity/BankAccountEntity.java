package com.university.paymentgateway.domain.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import com.university.paymentgateway.domain.audit.BaseAuditing;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Tài khoản ngân hàng demo được quản lý bởi PaymentGateway.
 * PG là nguồn sự thật (source of truth) cho số dư ngân hàng,
 * thay vì để fintech quản lý như trước đây.
 */
@Entity
@Table(name = "bank_accounts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_bank_account_code_number",
                columnNames = {"bank_code", "account_number"}
        ))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountEntity extends BaseAuditing {

    @Id
    private UUID id;

    @Column(name = "bank_code", nullable = false)
    private String bankCode;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Builder.Default
    @Column(name = "balance", nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
        if (balance == null) balance = BigDecimal.ZERO;
        if (status == null) status = "ACTIVE";
    }
}
