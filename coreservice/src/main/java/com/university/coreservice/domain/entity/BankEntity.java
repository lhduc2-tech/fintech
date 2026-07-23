package com.university.coreservice.domain.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import com.university.coreservice.domain.audit.BaseAuditing;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Map tới bảng `banks` trong DB fintech.
 * CoreService đọc balance ngân hàng từ bảng này để validate giao dịch BANK_TO_WALLET / WALLET_TO_BANK.
 */
@Entity
@Table(name = "banks",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_banks_code_account_number",
                columnNames = {"bank_code", "account_number"}
        ))
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
    private String status = "ACTIVE";

    @Builder.Default
    @Column(name = "balance")
    private BigDecimal balance = BigDecimal.ZERO;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
        if (status == null) status = "ACTIVE";
        if (balance == null) balance = BigDecimal.ZERO;
    }
}
