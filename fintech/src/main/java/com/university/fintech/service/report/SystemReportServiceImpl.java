package com.university.fintech.service.report;

import com.university.fintech.domain.dto.SystemReportDTO;
import com.university.fintech.domain.entity.WalletTransactionEntity;
import com.university.fintech.domain.port.primary.SystemReportService;
import com.university.fintech.infrastructure.secondary.jpa.repository.UserRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.WalletTransactionRepository;
import com.university.fintech.shared.constant.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SystemReportServiceImpl implements SystemReportService {

    private final UserRepository userRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    private static final OffsetDateTime DEFAULT_MIN_DATE = OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    @Override
    public SystemReportDTO getSystemStabilityMetrics(OffsetDateTime from, OffsetDateTime to) {
        OffsetDateTime safeFrom = from != null ? from : DEFAULT_MIN_DATE;
        OffsetDateTime safeTo = to != null ? to : OffsetDateTime.now();

        // 1. User Metrics
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(Constant.STATUS_ACTIVE);
        long lockedUsers = userRepository.countByStatus(Constant.STATUS_LOCKED);
        long pendingVerificationUsers = userRepository.countByStatus(Constant.STATUS_PENDING_EMAIL_VERIFICATION);
        long newUsersInPeriod = userRepository.countByCreatedAtBetween(safeFrom, safeTo);
        long lockedPinUsersCount = userRepository.countByPinFailedAttemptsGreaterThanEqual(5);
        long failedLoginUsersCount = userRepository.countByFailedLoginAttemptsGreaterThanEqual(1);

        SystemReportDTO.UserMetricsDTO userMetrics = SystemReportDTO.UserMetricsDTO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .lockedUsers(lockedUsers)
                .pendingVerificationUsers(pendingVerificationUsers)
                .newUsersInPeriod(newUsersInPeriod)
                .lockedPinUsersCount(lockedPinUsersCount)
                .failedLoginAttemptsUsersCount(failedLoginUsersCount)
                .build();

        // 2. Transaction Metrics
        long totalTx = walletTransactionRepository.countByCreatedAtBetween(safeFrom, safeTo);
        BigDecimal totalAmount = defaultZero(walletTransactionRepository.sumTotalAmountBetween(safeFrom, safeTo));

        long successCount = walletTransactionRepository.countByStatusAndCreatedAtBetween(Constant.STATUS_SUCCESS, safeFrom, safeTo);
        BigDecimal successAmount = defaultZero(walletTransactionRepository.sumAmountByStatusAndCreatedAtBetween(Constant.STATUS_SUCCESS, safeFrom, safeTo));

        long failedCount = walletTransactionRepository.countByStatusAndCreatedAtBetween(Constant.STATUS_FAILED, safeFrom, safeTo);
        BigDecimal failedAmount = defaultZero(walletTransactionRepository.sumAmountByStatusAndCreatedAtBetween(Constant.STATUS_FAILED, safeFrom, safeTo));

        long timedOutCount = walletTransactionRepository.countByStatusAndCreatedAtBetween(Constant.STATUS_TIMED_OUT, safeFrom, safeTo);
        BigDecimal timedOutAmount = defaultZero(walletTransactionRepository.sumAmountByStatusAndCreatedAtBetween(Constant.STATUS_TIMED_OUT, safeFrom, safeTo));

        long pendingCount = walletTransactionRepository.countByStatusAndCreatedAtBetween(Constant.STATUS_PENDING, safeFrom, safeTo);
        BigDecimal pendingAmount = defaultZero(walletTransactionRepository.sumAmountByStatusAndCreatedAtBetween(Constant.STATUS_PENDING, safeFrom, safeTo));

        long suspiciousCount = walletTransactionRepository.countBySuspiciousTrueAndCreatedAtBetween(safeFrom, safeTo);

        // Type Breakdown
        List<String> types = List.of(
                Constant.WALLET_TRANSACTION_TYPE_BANK_TO_WALLET,
                Constant.WALLET_TRANSACTION_TYPE_WALLET_TO_BANK,
                Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_OUT,
                Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_IN
        );

        List<SystemReportDTO.TypeBreakdownDTO> typeBreakdowns = new ArrayList<>();
        for (String type : types) {
            long typeTotal = walletTransactionRepository.countByTypeAndCreatedAtBetween(type, safeFrom, safeTo);
            long typeSuccess = walletTransactionRepository.countByTypeAndStatusAndCreatedAtBetween(type, Constant.STATUS_SUCCESS, safeFrom, safeTo);
            long typeFailed = walletTransactionRepository.countByTypeAndStatusAndCreatedAtBetween(type, Constant.STATUS_FAILED, safeFrom, safeTo)
                    + walletTransactionRepository.countByTypeAndStatusAndCreatedAtBetween(type, Constant.STATUS_TIMED_OUT, safeFrom, safeTo);
            BigDecimal typeAmount = defaultZero(walletTransactionRepository.sumAmountByTypeAndStatusAndCreatedAtBetween(type, Constant.STATUS_SUCCESS, safeFrom, safeTo));

            typeBreakdowns.add(SystemReportDTO.TypeBreakdownDTO.builder()
                    .type(type)
                    .totalCount(typeTotal)
                    .successCount(typeSuccess)
                    .failedCount(typeFailed)
                    .totalAmount(typeAmount)
                    .build());
        }

        SystemReportDTO.TransactionMetricsDTO txMetrics = SystemReportDTO.TransactionMetricsDTO.builder()
                .totalTransactions(totalTx)
                .totalAmount(totalAmount)
                .successCount(successCount)
                .successAmount(successAmount)
                .failedCount(failedCount)
                .failedAmount(failedAmount)
                .timedOutCount(timedOutCount)
                .timedOutAmount(timedOutAmount)
                .pendingCount(pendingCount)
                .pendingAmount(pendingAmount)
                .suspiciousCount(suspiciousCount)
                .typeBreakdowns(typeBreakdowns)
                .build();

        // 3. Stability Evaluation
        double successRate = totalTx == 0 ? 100.0 : ((double) successCount * 100.0 / totalTx);
        double errorRate = totalTx == 0 ? 0.0 : ((double) (failedCount + timedOutCount) * 100.0 / totalTx);

        String status;
        String description;

        if (totalTx == 0) {
            status = "STABLE";
            description = "Hệ thống chưa có giao dịch phát sinh trong khoảng thời gian đã chọn.";
        } else if (errorRate < 3.0 && suspiciousCount == 0) {
            status = "STABLE";
            description = "Hệ thống hoạt động rất ổn định. Tỷ lệ lỗi nhỏ hơn 3% và không có sự cố an ninh.";
        } else if (errorRate < 10.0) {
            status = "WARNING";
            description = "Cảnh báo: Tỷ lệ lỗi ở mức " + String.format("%.2f", errorRate) + "%. Cần kiểm tra kết nối gateway hoặc cơ sở dữ liệu.";
        } else {
            status = "CRITICAL";
            description = "Nghiêm trọng: Tỷ lệ lỗi cao (" + String.format("%.2f", errorRate) + "%). Yêu cầu rà soát hệ thống khẩn cấp!";
        }

        SystemReportDTO.StabilityMetricsDTO stabilityMetrics = SystemReportDTO.StabilityMetricsDTO.builder()
                .successRatePercent(successRate)
                .errorRatePercent(errorRate)
                .systemStatus(status)
                .statusDescription(description)
                .build();

        // 4. Error Transaction Details
        List<WalletTransactionEntity> errorEntities = walletTransactionRepository.findErrorTransactionsBetween(
                List.of(Constant.STATUS_FAILED, Constant.STATUS_TIMED_OUT), safeFrom, safeTo
        );

        List<SystemReportDTO.ErrorTransactionDetailDTO> errorDetails = errorEntities.stream()
                .map(t -> SystemReportDTO.ErrorTransactionDetailDTO.builder()
                        .id(t.getId())
                        .referenceCode(t.getReferenceCode())
                        .userFullName(t.getUser() != null ? t.getUser().getFullName() : null)
                        .userPhoneNumber(t.getUser() != null ? t.getUser().getPhoneNumber() : null)
                        .type(t.getType())
                        .amount(t.getAmount())
                        .status(t.getStatus())
                        .description(t.getDescription())
                        .createdAt(t.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return SystemReportDTO.builder()
                .fromDate(from)
                .toDate(to)
                .generatedAt(OffsetDateTime.now())
                .userMetrics(userMetrics)
                .transactionMetrics(txMetrics)
                .stabilityMetrics(stabilityMetrics)
                .errorTransactions(errorDetails)
                .build();
    }

    @Override
    public byte[] generateSystemStabilityExcelReport(OffsetDateTime from, OffsetDateTime to) {
        SystemReportDTO metrics = getSystemStabilityMetrics(from, to);
        return SystemReportExcelExporter.exportToExcel(metrics);
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
