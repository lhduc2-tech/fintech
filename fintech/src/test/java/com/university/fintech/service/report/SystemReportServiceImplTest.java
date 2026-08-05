package com.university.fintech.service.report;

import com.university.fintech.domain.dto.SystemReportDTO;
import com.university.fintech.domain.entity.UserEntity;
import com.university.fintech.domain.entity.WalletTransactionEntity;
import com.university.fintech.infrastructure.secondary.jpa.repository.UserRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.WalletTransactionRepository;
import com.university.fintech.shared.constant.Constant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemReportServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @InjectMocks
    private SystemReportServiceImpl systemReportService;

    private OffsetDateTime from;
    private OffsetDateTime to;

    @BeforeEach
    void setUp() {
        from = OffsetDateTime.now().minusDays(30);
        to = OffsetDateTime.now();
    }

    @Test
    @DisplayName("Thống kê trả về STABLE khi tỷ lệ lỗi dưới 3%")
    void testGetSystemStabilityMetrics_StableStatus() {
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countByStatus(Constant.STATUS_ACTIVE)).thenReturn(90L);
        when(userRepository.countByStatus(Constant.STATUS_LOCKED)).thenReturn(5L);
        when(userRepository.countByStatus(Constant.STATUS_PENDING_EMAIL_VERIFICATION)).thenReturn(5L);
        when(userRepository.countByCreatedAtBetween(any(), any())).thenReturn(10L);
        when(userRepository.countByPinFailedAttemptsGreaterThanEqual(5)).thenReturn(1L);
        when(userRepository.countByFailedLoginAttemptsGreaterThanEqual(1)).thenReturn(2L);

        when(walletTransactionRepository.countByCreatedAtBetween(any(), any())).thenReturn(100L);
        when(walletTransactionRepository.sumTotalAmountBetween(any(), any())).thenReturn(BigDecimal.valueOf(10000000));
        when(walletTransactionRepository.countByStatusAndCreatedAtBetween(eq(Constant.STATUS_SUCCESS), any(), any())).thenReturn(98L);
        when(walletTransactionRepository.sumAmountByStatusAndCreatedAtBetween(eq(Constant.STATUS_SUCCESS), any(), any())).thenReturn(BigDecimal.valueOf(9800000));

        when(walletTransactionRepository.countByStatusAndCreatedAtBetween(eq(Constant.STATUS_FAILED), any(), any())).thenReturn(1L);
        when(walletTransactionRepository.sumAmountByStatusAndCreatedAtBetween(eq(Constant.STATUS_FAILED), any(), any())).thenReturn(BigDecimal.valueOf(100000));

        when(walletTransactionRepository.countByStatusAndCreatedAtBetween(eq(Constant.STATUS_TIMED_OUT), any(), any())).thenReturn(1L);
        when(walletTransactionRepository.sumAmountByStatusAndCreatedAtBetween(eq(Constant.STATUS_TIMED_OUT), any(), any())).thenReturn(BigDecimal.valueOf(100000));

        when(walletTransactionRepository.countByStatusAndCreatedAtBetween(eq(Constant.STATUS_PENDING), any(), any())).thenReturn(0L);
        when(walletTransactionRepository.sumAmountByStatusAndCreatedAtBetween(eq(Constant.STATUS_PENDING), any(), any())).thenReturn(BigDecimal.ZERO);

        when(walletTransactionRepository.countBySuspiciousTrueAndCreatedAtBetween(any(), any())).thenReturn(0L);
        when(walletTransactionRepository.findErrorTransactionsBetween(any(), any(), any())).thenReturn(Collections.emptyList());

        SystemReportDTO report = systemReportService.getSystemStabilityMetrics(from, to);

        assertNotNull(report);
        assertEquals("STABLE", report.getStabilityMetrics().getSystemStatus());
        assertEquals(98.0, report.getStabilityMetrics().getSuccessRatePercent());
        assertEquals(2.0, report.getStabilityMetrics().getErrorRatePercent());
        assertEquals(100, report.getUserMetrics().getTotalUsers());
    }

    @Test
    @DisplayName("Thống kê xuất file Excel thành công")
    void testGenerateSystemStabilityExcelReport_Success() {
        when(userRepository.count()).thenReturn(50L);
        when(walletTransactionRepository.countByCreatedAtBetween(any(), any())).thenReturn(10L);
        when(walletTransactionRepository.sumTotalAmountBetween(any(), any())).thenReturn(BigDecimal.valueOf(500000));
        when(walletTransactionRepository.countByStatusAndCreatedAtBetween(eq(Constant.STATUS_SUCCESS), any(), any())).thenReturn(10L);

        byte[] excelData = systemReportService.generateSystemStabilityExcelReport(from, to);

        assertNotNull(excelData);
        assertTrue(excelData.length > 0, "File Excel tạo ra không được rỗng");
    }
}
