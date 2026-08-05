package com.university.fintech.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemReportDTO {

    private OffsetDateTime fromDate;
    private OffsetDateTime toDate;
    private OffsetDateTime generatedAt;

    private UserMetricsDTO userMetrics;
    private TransactionMetricsDTO transactionMetrics;
    private StabilityMetricsDTO stabilityMetrics;

    @Builder.Default
    private List<ErrorTransactionDetailDTO> errorTransactions = new ArrayList<>();

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserMetricsDTO {
        private long totalUsers;
        private long activeUsers;
        private long lockedUsers;
        private long pendingVerificationUsers;
        private long newUsersInPeriod;
        private long lockedPinUsersCount;
        private long failedLoginAttemptsUsersCount;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionMetricsDTO {
        private long totalTransactions;
        private BigDecimal totalAmount;

        private long successCount;
        private BigDecimal successAmount;

        private long failedCount;
        private BigDecimal failedAmount;

        private long timedOutCount;
        private BigDecimal timedOutAmount;

        private long pendingCount;
        private BigDecimal pendingAmount;

        private long suspiciousCount;

        @Builder.Default
        private List<TypeBreakdownDTO> typeBreakdowns = new ArrayList<>();
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeBreakdownDTO {
        private String type;
        private long totalCount;
        private long successCount;
        private long failedCount;
        private BigDecimal totalAmount;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StabilityMetricsDTO {
        private double successRatePercent;
        private double errorRatePercent;
        private String systemStatus; // STABLE, WARNING, CRITICAL
        private String statusDescription;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorTransactionDetailDTO {
        private UUID id;
        private String referenceCode;
        private String userFullName;
        private String userPhoneNumber;
        private String type;
        private BigDecimal amount;
        private String status;
        private String description;
        private OffsetDateTime createdAt;
    }
}
