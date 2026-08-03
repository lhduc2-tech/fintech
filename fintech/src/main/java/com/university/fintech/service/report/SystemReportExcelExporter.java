package com.university.fintech.service.report;

import com.university.fintech.domain.dto.SystemReportDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SystemReportExcelExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static byte[] exportToExcel(SystemReportDTO report) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Create Style Factory
            ExcelStyleFactory styles = new ExcelStyleFactory(workbook);

            // Sheet 1: Tổng Quan Hệ Thống
            createOverviewSheet(workbook, report, styles);

            // Sheet 2: Thống Kê Người Dùng
            createUserStatsSheet(workbook, report, styles);

            // Sheet 3: Thống Kê Giao Dịch
            createTransactionStatsSheet(workbook, report, styles);

            // Sheet 4: Chi Tiết Lỗi & Sự Cố
            createErrorDetailsSheet(workbook, report, styles);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi trong quá trình xuất báo cáo Excel: " + e.getMessage(), e);
        }
    }

    // ==========================================
    // SHEET 1: TỔNG QUAN HỆ THỐNG & ĐỘ ỔN ĐỊNH
    // ==========================================
    private static void createOverviewSheet(Workbook workbook, SystemReportDTO report, ExcelStyleFactory styles) {
        Sheet sheet = workbook.createSheet("Tổng Quan Hệ Thống");
        sheet.setDisplayGridlines(true);

        int rowIdx = 0;

        // Title
        Row titleRow = sheet.createRow(rowIdx++);
        titleRow.setHeightInPoints(35);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("BÁO CÁO ĐÁNH GIÁ ĐỘ ỔN ĐỊNH HỆ THỐNG FINTECH");
        titleCell.setCellStyle(styles.titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

        rowIdx++; // Empty row

        // Metadata Card
        Row infoRow1 = sheet.createRow(rowIdx++);
        infoRow1.createCell(0).setCellValue("Thời gian xuất báo cáo:");
        infoRow1.getCell(0).setCellStyle(styles.boldLabelStyle);
        infoRow1.createCell(1).setCellValue(formatDate(report.getGeneratedAt()));
        infoRow1.getCell(1).setCellStyle(styles.normalStyle);

        Row infoRow2 = sheet.createRow(rowIdx++);
        infoRow2.createCell(0).setCellValue("Từ ngày:");
        infoRow2.getCell(0).setCellStyle(styles.boldLabelStyle);
        infoRow2.createCell(1).setCellValue(formatDate(report.getFromDate()));
        infoRow2.getCell(1).setCellStyle(styles.normalStyle);
        infoRow2.createCell(2).setCellValue("Đến ngày:");
        infoRow2.getCell(2).setCellStyle(styles.boldLabelStyle);
        infoRow2.createCell(3).setCellValue(formatDate(report.getToDate()));
        infoRow2.getCell(3).setCellStyle(styles.normalStyle);

        rowIdx++; // Empty row

        // Status Card Banner
        Row statusHeaderRow = sheet.createRow(rowIdx++);
        statusHeaderRow.setHeightInPoints(24);
        Cell statusHeaderCell = statusHeaderRow.createCell(0);
        statusHeaderCell.setCellValue("TRẠNG THÁI HOẠT ĐỘNG HỆ THỐNG");
        statusHeaderCell.setCellStyle(styles.sectionHeaderStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 5));

        Row statusValRow = sheet.createRow(rowIdx++);
        statusValRow.setHeightInPoints(30);

        SystemReportDTO.StabilityMetricsDTO stability = report.getStabilityMetrics();
        CellStyle statusStyle;
        if ("STABLE".equalsIgnoreCase(stability.getSystemStatus())) {
            statusStyle = styles.statusStableStyle;
        } else if ("WARNING".equalsIgnoreCase(stability.getSystemStatus())) {
            statusStyle = styles.statusWarningStyle;
        } else {
            statusStyle = styles.statusCriticalStyle;
        }

        Cell statusCell = statusValRow.createCell(0);
        statusCell.setCellValue("ĐÁNH GIÁ: " + stability.getSystemStatus() + " - " + stability.getStatusDescription());
        statusCell.setCellStyle(statusStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 5));

        rowIdx++; // Empty row

        // KPI Summary Table Header
        Row tableHeaderRow = sheet.createRow(rowIdx++);
        tableHeaderRow.setHeightInPoints(26);
        String[] headers = {"Hạng Mục Chỉ Số", "Giá Trị Thống Kê", "Đơn Vị", "Tỷ Lệ / Chi Tiết", "Đánh Giá Độ Ổn Định"};
        for (int i = 0; i < headers.length; i++) {
            Cell c = tableHeaderRow.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(styles.tableHeaderStyle);
        }

        // Row 1: Users
        SystemReportDTO.UserMetricsDTO users = report.getUserMetrics();
        addKpiRow(sheet, rowIdx++, "Tổng số người dùng đăng ký", users.getTotalUsers(), "Người dùng",
                "Hoạt động: " + users.getActiveUsers() + " | Bị khóa: " + users.getLockedUsers(),
                users.getLockedUsers() == 0 ? "Bình thường" : "Có " + users.getLockedUsers() + " TK bị khóa", styles);

        // Row 2: Users in period
        addKpiRow(sheet, rowIdx++, "Người dùng mới đăng ký (trong kỳ)", users.getNewUsersInPeriod(), "Người dùng", "-", "Bình thường", styles);

        // Row 3: Locked PIN
        addKpiRow(sheet, rowIdx++, "Tài khoản bị khóa PIN (sai > 5 lần)", users.getLockedPinUsersCount(), "Tài khoản", "-",
                users.getLockedPinUsersCount() > 0 ? "Cảnh báo an toàn" : "Bình thường", styles);

        // Row 4: Total Transactions
        SystemReportDTO.TransactionMetricsDTO tx = report.getTransactionMetrics();
        addKpiRow(sheet, rowIdx++, "Tổng số lượng giao dịch", tx.getTotalTransactions(), "Giao dịch", "-", "Bình thường", styles);

        // Row 5: Total Volume
        addKpiAmountRow(sheet, rowIdx++, "Tổng giá trị luồng tiền giao dịch", tx.getTotalAmount(), "VND", "-", "Bình thường", styles);

        // Row 6: Success Tx
        addKpiAmountRow(sheet, rowIdx++, "Giao dịch thành công", tx.getSuccessAmount(), "VND",
                tx.getSuccessCount() + " GD (" + String.format("%.2f", stability.getSuccessRatePercent()) + "%)",
                stability.getSuccessRatePercent() >= 97.0 ? "Rất Tốt" : "Cần Theo Dõi", styles);

        // Row 7: Failed & Timed out Tx
        long totalErrorCount = tx.getFailedCount() + tx.getTimedOutCount();
        BigDecimal failedAmount = tx.getFailedAmount() != null ? tx.getFailedAmount() : BigDecimal.ZERO;
        BigDecimal timedOutAmount = tx.getTimedOutAmount() != null ? tx.getTimedOutAmount() : BigDecimal.ZERO;
        BigDecimal totalErrorAmount = failedAmount.add(timedOutAmount);
        addKpiAmountRow(sheet, rowIdx++, "Giao dịch Thất Bị / Timeout", totalErrorAmount, "VND",
                totalErrorCount + " GD (" + String.format("%.2f", stability.getErrorRatePercent()) + "%)",
                stability.getErrorRatePercent() < 3.0 ? "An Toàn (<3%)" : (stability.getErrorRatePercent() < 10.0 ? "Cảnh Báo (3-10%)" : "CRITICAL (>10%)"), styles);

        // Row 8: Suspicious Tx
        addKpiRow(sheet, rowIdx++, "Giao dịch nghi vấn (Suspicious)", tx.getSuspiciousCount(), "Giao dịch", "-",
                tx.getSuspiciousCount() > 0 ? "Cần kiểm tra an ninh" : "Không phát hiện", styles);

        autoSizeColumns(sheet, 6);
    }

    // ==========================================
    // SHEET 2: THỐNG KÊ NGƯỜI DÙNG
    // ==========================================
    private static void createUserStatsSheet(Workbook workbook, SystemReportDTO report, ExcelStyleFactory styles) {
        Sheet sheet = workbook.createSheet("Thống Kê Người Dùng");
        sheet.setDisplayGridlines(true);

        int rowIdx = 0;
        Row titleRow = sheet.createRow(rowIdx++);
        titleRow.setHeightInPoints(30);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("THỐNG KÊ CHI TIẾT TÌNH TRẠNG NGƯỜI DÙNG");
        titleCell.setCellStyle(styles.titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        rowIdx++;

        // Section 1: Breakdown by Status
        Row headerRow = sheet.createRow(rowIdx++);
        headerRow.setHeightInPoints(24);
        String[] cols = {"Trạng Thái Tài Khoản", "Số Lượng", "Tỷ Lệ (%)", "Mô Tả"};
        for (int i = 0; i < cols.length; i++) {
            Cell c = headerRow.createCell(i);
            c.setCellValue(cols[i]);
            c.setCellStyle(styles.tableHeaderStyle);
        }

        SystemReportDTO.UserMetricsDTO u = report.getUserMetrics();
        long total = u.getTotalUsers() == 0 ? 1 : u.getTotalUsers();

        addUserStatusRow(sheet, rowIdx++, "ACTIVE (Đang hoạt động)", u.getActiveUsers(), (double) u.getActiveUsers() * 100 / total, "Tài khoản hoạt động bình thường", styles);
        addUserStatusRow(sheet, rowIdx++, "LOCKED (Bị khóa do nhập sai)", u.getLockedUsers(), (double) u.getLockedUsers() * 100 / total, "Tài khoản bị khóa do sai mật khẩu", styles);
        addUserStatusRow(sheet, rowIdx++, "PENDING_EMAIL_VERIFICATION (Chờ xác thực)", u.getPendingVerificationUsers(), (double) u.getPendingVerificationUsers() * 100 / total, "Chưa hoàn tất OTP email", styles);

        Row totalRow = sheet.createRow(rowIdx++);
        totalRow.createCell(0).setCellValue("TỔNG CỘNG");
        totalRow.getCell(0).setCellStyle(styles.tableTotalStyle);
        totalRow.createCell(1).setCellValue(u.getTotalUsers());
        totalRow.getCell(1).setCellStyle(styles.tableTotalNumberStyle);
        totalRow.createCell(2).setCellValue(100.0);
        totalRow.getCell(2).setCellStyle(styles.tableTotalPercentStyle);
        totalRow.createCell(3).setCellValue("");
        totalRow.getCell(3).setCellStyle(styles.tableTotalStyle);

        rowIdx += 2;

        // Section 2: Security & PIN indicators
        Row secHeader = sheet.createRow(rowIdx++);
        secHeader.setHeightInPoints(24);
        Cell secCell = secHeader.createCell(0);
        secCell.setCellValue("CHỈ SỐ AN NINH VÀ KHÓA MÃ PIN");
        secCell.setCellStyle(styles.sectionHeaderStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 3));

        Row secCols = sheet.createRow(rowIdx++);
        secCols.createCell(0).setCellValue("Chỉ Số Phân Tích");
        secCols.getCell(0).setCellStyle(styles.tableHeaderStyle);
        secCols.createCell(1).setCellValue("Số Lượng");
        secCols.getCell(1).setCellStyle(styles.tableHeaderStyle);
        secCols.createCell(2).setCellValue("Mức Độ Rủi Ro");
        secCols.getCell(2).setCellStyle(styles.tableHeaderStyle);
        secCols.createCell(3).setCellValue("Hành Động Đề Xuất");
        secCols.getCell(3).setCellStyle(styles.tableHeaderStyle);

        addRowData(sheet, rowIdx++, "Tài khoản bị khóa PIN (nhập sai PIN >= 5 lần)", u.getLockedPinUsersCount(),
                u.getLockedPinUsersCount() > 0 ? "CẢNH BÁO" : "AN TOÀN", "Gửi email hướng dẫn cấp lại PIN", styles);

        addRowData(sheet, rowIdx++, "Tài khoản có lượt đăng nhập thất bại", u.getFailedLoginAttemptsUsersCount(),
                u.getFailedLoginAttemptsUsersCount() > 5 ? "CẢNH BÁO" : "AN TOÀN", "Theo dõi IP đăng nhập bất thường", styles);

        autoSizeColumns(sheet, 4);
    }

    // ==========================================
    // SHEET 3: THỐNG KÊ GIAO DỊCH & LUỒNG TIỀN
    // ==========================================
    private static void createTransactionStatsSheet(Workbook workbook, SystemReportDTO report, ExcelStyleFactory styles) {
        Sheet sheet = workbook.createSheet("Thống Kê Giao Dịch");
        sheet.setDisplayGridlines(true);

        int rowIdx = 0;
        Row titleRow = sheet.createRow(rowIdx++);
        titleRow.setHeightInPoints(30);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("THỐNG KÊ CHI TIẾT GIAO DỊCH VÀ LUỒNG TIỀN");
        titleCell.setCellStyle(styles.titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        rowIdx++;

        // Status Breakdown
        Row header1 = sheet.createRow(rowIdx++);
        header1.setHeightInPoints(24);
        String[] cols1 = {"Trạng Thái Giao Dịch", "Số Lượng GD", "Tỷ Lệ (%)", "Tổng Giá Trị (VND)", "Đánh Giá"};
        for (int i = 0; i < cols1.length; i++) {
            Cell c = header1.createCell(i);
            c.setCellValue(cols1[i]);
            c.setCellStyle(styles.tableHeaderStyle);
        }

        SystemReportDTO.TransactionMetricsDTO tx = report.getTransactionMetrics();
        long totalCount = tx.getTotalTransactions() == 0 ? 1 : tx.getTotalTransactions();

        addTxStatusRow(sheet, rowIdx++, "SUCCESS (Thành công)", tx.getSuccessCount(), (double) tx.getSuccessCount() * 100 / totalCount, tx.getSuccessAmount(), "Bình thường", styles);
        addTxStatusRow(sheet, rowIdx++, "FAILED (Thất bại)", tx.getFailedCount(), (double) tx.getFailedCount() * 100 / totalCount, tx.getFailedAmount(), tx.getFailedCount() > 0 ? "Cần xem nguyên nhân" : "Bình thường", styles);
        addTxStatusRow(sheet, rowIdx++, "TIMED_OUT (Hết thời hạn)", tx.getTimedOutCount(), (double) tx.getTimedOutCount() * 100 / totalCount, tx.getTimedOutAmount(), tx.getTimedOutCount() > 0 ? "Kiểm tra gateway/DB" : "Bình thường", styles);
        addTxStatusRow(sheet, rowIdx++, "PENDING (Đang xử lý)", tx.getPendingCount(), (double) tx.getPendingCount() * 100 / totalCount, tx.getPendingAmount(), "Bình thường", styles);

        Row totalRow = sheet.createRow(rowIdx++);
        totalRow.createCell(0).setCellValue("TỔNG CỘNG");
        totalRow.getCell(0).setCellStyle(styles.tableTotalStyle);
        totalRow.createCell(1).setCellValue(tx.getTotalTransactions());
        totalRow.getCell(1).setCellStyle(styles.tableTotalNumberStyle);
        totalRow.createCell(2).setCellValue(100.0);
        totalRow.getCell(2).setCellStyle(styles.tableTotalPercentStyle);
        totalRow.createCell(3).setCellValue(tx.getTotalAmount().doubleValue());
        totalRow.getCell(3).setCellStyle(styles.tableTotalCurrencyStyle);
        totalRow.createCell(4).setCellValue("");
        totalRow.getCell(4).setCellStyle(styles.tableTotalStyle);

        rowIdx += 2;

        // Type Breakdown Header
        Row secHeader = sheet.createRow(rowIdx++);
        secHeader.setHeightInPoints(24);
        Cell secCell = secHeader.createCell(0);
        secCell.setCellValue("PHÂN PHỐI THEO LOẠI GIAO DỊCH");
        secCell.setCellStyle(styles.sectionHeaderStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 4));

        Row header2 = sheet.createRow(rowIdx++);
        header2.setHeightInPoints(24);
        String[] cols2 = {"Loại Giao Dịch", "Tổng Số GD", "Thành Công", "Thất Bị / Timeout", "Tổng Tiền (VND)"};
        for (int i = 0; i < cols2.length; i++) {
            Cell c = header2.createCell(i);
            c.setCellValue(cols2[i]);
            c.setCellStyle(styles.tableHeaderStyle);
        }

        for (SystemReportDTO.TypeBreakdownDTO typeDto : tx.getTypeBreakdowns()) {
            Row r = sheet.createRow(rowIdx++);
            r.createCell(0).setCellValue(formatTxType(typeDto.getType()));
            r.getCell(0).setCellStyle(styles.normalStyle);
            r.createCell(1).setCellValue(typeDto.getTotalCount());
            r.getCell(1).setCellStyle(styles.numberStyle);
            r.createCell(2).setCellValue(typeDto.getSuccessCount());
            r.getCell(2).setCellStyle(styles.numberStyle);
            r.createCell(3).setCellValue(typeDto.getFailedCount());
            r.getCell(3).setCellStyle(styles.numberStyle);
            r.createCell(4).setCellValue(typeDto.getTotalAmount() != null ? typeDto.getTotalAmount().doubleValue() : 0);
            r.getCell(4).setCellStyle(styles.currencyStyle);
        }

        autoSizeColumns(sheet, 5);
    }

    // ==========================================
    // SHEET 4: CHI TIẾT LỖI & SỰ CỐ
    // ==========================================
    private static void createErrorDetailsSheet(Workbook workbook, SystemReportDTO report, ExcelStyleFactory styles) {
        Sheet sheet = workbook.createSheet("Chi Tiết Lỗi & Sự Cố");
        sheet.setDisplayGridlines(true);

        int rowIdx = 0;
        Row titleRow = sheet.createRow(rowIdx++);
        titleRow.setHeightInPoints(30);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("DANH SÁCH GIAO DỊCH LỖI VÀ SỰ CỐ HỆ THỐNG");
        titleCell.setCellStyle(styles.titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

        rowIdx++;

        Row header = sheet.createRow(rowIdx++);
        header.setHeightInPoints(25);
        String[] cols = {"STT", "Mã Tham Chiếu", "Họ và Tên User", "Số Điện Thoại", "Loại Giao Dịch", "Số Tiền (VND)", "Trạng Thái", "Mô Tả / Ghi Chú", "Thời Gian Tạo"};
        for (int i = 0; i < cols.length; i++) {
            Cell c = header.createCell(i);
            c.setCellValue(cols[i]);
            c.setCellStyle(styles.tableHeaderStyle);
        }

        List<SystemReportDTO.ErrorTransactionDetailDTO> errors = report.getErrorTransactions();
        if (errors == null || errors.isEmpty()) {
            Row emptyRow = sheet.createRow(rowIdx++);
            Cell c = emptyRow.createCell(0);
            c.setCellValue("Không có giao dịch bị lỗi hoặc sự cố trong khoảng thời gian đã chọn.");
            c.setCellStyle(styles.normalStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 8));
        } else {
            int stt = 1;
            for (SystemReportDTO.ErrorTransactionDetailDTO err : errors) {
                Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(stt++);
                r.getCell(0).setCellStyle(styles.centerStyle);

                r.createCell(1).setCellValue(err.getReferenceCode() != null ? err.getReferenceCode() : "-");
                r.getCell(1).setCellStyle(styles.centerStyle);

                r.createCell(2).setCellValue(err.getUserFullName() != null ? err.getUserFullName() : "-");
                r.getCell(2).setCellStyle(styles.normalStyle);

                r.createCell(3).setCellValue(err.getUserPhoneNumber() != null ? err.getUserPhoneNumber() : "-");
                r.getCell(3).setCellStyle(styles.centerStyle);

                r.createCell(4).setCellValue(formatTxType(err.getType()));
                r.getCell(4).setCellStyle(styles.normalStyle);

                r.createCell(5).setCellValue(err.getAmount() != null ? err.getAmount().doubleValue() : 0);
                r.getCell(5).setCellStyle(styles.currencyStyle);

                r.createCell(6).setCellValue(err.getStatus() != null ? err.getStatus() : "-");
                r.getCell(6).setCellStyle("TIMED_OUT".equalsIgnoreCase(err.getStatus()) ? styles.statusWarningStyle : styles.statusCriticalStyle);

                r.createCell(7).setCellValue(err.getDescription() != null ? err.getDescription() : "-");
                r.getCell(7).setCellStyle(styles.normalStyle);

                r.createCell(8).setCellValue(formatDate(err.getCreatedAt()));
                r.getCell(8).setCellStyle(styles.centerStyle);
            }
        }

        autoSizeColumns(sheet, 9);
    }

    // ==========================================
    // HELPER METHODS & STYLES
    // ==========================================
    private static void addKpiRow(Sheet sheet, int rowIdx, String label, long val, String unit, String details, String statusStr, ExcelStyleFactory styles) {
        Row r = sheet.createRow(rowIdx);
        r.createCell(0).setCellValue(label);
        r.getCell(0).setCellStyle(styles.boldLabelStyle);
        r.createCell(1).setCellValue(val);
        r.getCell(1).setCellStyle(styles.numberStyle);
        r.createCell(2).setCellValue(unit);
        r.getCell(2).setCellStyle(styles.centerStyle);
        r.createCell(3).setCellValue(details);
        r.getCell(3).setCellStyle(styles.normalStyle);
        r.createCell(4).setCellValue(statusStr);
        r.getCell(4).setCellStyle(styles.centerStyle);
    }

    private static void addKpiAmountRow(Sheet sheet, int rowIdx, String label, BigDecimal amount, String unit, String details, String statusStr, ExcelStyleFactory styles) {
        Row r = sheet.createRow(rowIdx);
        r.createCell(0).setCellValue(label);
        r.getCell(0).setCellStyle(styles.boldLabelStyle);
        r.createCell(1).setCellValue(amount != null ? amount.doubleValue() : 0);
        r.getCell(1).setCellStyle(styles.currencyStyle);
        r.createCell(2).setCellValue(unit);
        r.getCell(2).setCellStyle(styles.centerStyle);
        r.createCell(3).setCellValue(details);
        r.getCell(3).setCellStyle(styles.normalStyle);
        r.createCell(4).setCellValue(statusStr);
        r.getCell(4).setCellStyle(styles.centerStyle);
    }

    private static void addUserStatusRow(Sheet sheet, int rowIdx, String status, long count, double percent, String desc, ExcelStyleFactory styles) {
        Row r = sheet.createRow(rowIdx);
        r.createCell(0).setCellValue(status);
        r.getCell(0).setCellStyle(styles.normalStyle);
        r.createCell(1).setCellValue(count);
        r.getCell(1).setCellStyle(styles.numberStyle);
        r.createCell(2).setCellValue(percent);
        r.getCell(2).setCellStyle(styles.percentStyle);
        r.createCell(3).setCellValue(desc);
        r.getCell(3).setCellStyle(styles.normalStyle);
    }

    private static void addTxStatusRow(Sheet sheet, int rowIdx, String status, long count, double percent, BigDecimal amount, String desc, ExcelStyleFactory styles) {
        Row r = sheet.createRow(rowIdx);
        r.createCell(0).setCellValue(status);
        r.getCell(0).setCellStyle(styles.normalStyle);
        r.createCell(1).setCellValue(count);
        r.getCell(1).setCellStyle(styles.numberStyle);
        r.createCell(2).setCellValue(percent);
        r.getCell(2).setCellStyle(styles.percentStyle);
        r.createCell(3).setCellValue(amount != null ? amount.doubleValue() : 0);
        r.getCell(3).setCellStyle(styles.currencyStyle);
        r.createCell(4).setCellValue(desc);
        r.getCell(4).setCellStyle(styles.normalStyle);
    }

    private static void addRowData(Sheet sheet, int rowIdx, String col0, long count, String col2, String col3, ExcelStyleFactory styles) {
        Row r = sheet.createRow(rowIdx);
        r.createCell(0).setCellValue(col0);
        r.getCell(0).setCellStyle(styles.normalStyle);
        r.createCell(1).setCellValue(count);
        r.getCell(1).setCellStyle(styles.numberStyle);
        r.createCell(2).setCellValue(col2);
        r.getCell(2).setCellStyle("CẢNH BÁO".equalsIgnoreCase(col2) ? styles.statusWarningStyle : styles.statusStableStyle);
        r.createCell(3).setCellValue(col3);
        r.getCell(3).setCellStyle(styles.normalStyle);
    }

    private static void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.max(currentWidth + 1200, 4500));
        }
    }

    private static String formatDate(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return "Tất cả (Toàn thời gian)";
        }
        return dateTime.format(DATE_FORMATTER);
    }

    private static String formatTxType(String type) {
        if (type == null) return "-";
        switch (type) {
            case "BANK_TO_WALLET": return "Nạp tiền từ Ngân hàng";
            case "WALLET_TO_BANK": return "Rút tiền về Ngân hàng";
            case "WALLET_TRANSFER_OUT": return "Chuyển tiền ví (Đi)";
            case "WALLET_TRANSFER_IN": return "Chuyển tiền ví (Nhận)";
            default: return type;
        }
    }

    // Inner Factory Class for POI Styles
    private static class ExcelStyleFactory {
        final CellStyle titleStyle;
        final CellStyle sectionHeaderStyle;
        final CellStyle tableHeaderStyle;
        final CellStyle boldLabelStyle;
        final CellStyle normalStyle;
        final CellStyle centerStyle;
        final CellStyle numberStyle;
        final CellStyle currencyStyle;
        final CellStyle percentStyle;
        final CellStyle statusStableStyle;
        final CellStyle statusWarningStyle;
        final CellStyle statusCriticalStyle;
        final CellStyle tableTotalStyle;
        final CellStyle tableTotalNumberStyle;
        final CellStyle tableTotalCurrencyStyle;
        final CellStyle tableTotalPercentStyle;

        ExcelStyleFactory(Workbook wb) {
            DataFormat df = wb.createDataFormat();

            // Font definitions
            Font titleFont = wb.createFont();
            titleFont.setFontName("Calibri");
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setBold(true);
            titleFont.setColor(IndexedColors.WHITE.getIndex());

            Font sectionFont = wb.createFont();
            sectionFont.setFontName("Calibri");
            sectionFont.setFontHeightInPoints((short) 12);
            sectionFont.setBold(true);
            sectionFont.setColor(IndexedColors.WHITE.getIndex());

            Font headerFont = wb.createFont();
            headerFont.setFontName("Calibri");
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            Font boldFont = wb.createFont();
            boldFont.setFontName("Calibri");
            boldFont.setFontHeightInPoints((short) 11);
            boldFont.setBold(true);

            Font normalFont = wb.createFont();
            normalFont.setFontName("Calibri");
            normalFont.setFontHeightInPoints((short) 11);

            // Title Style
            titleStyle = wb.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Section Header Style
            sectionHeaderStyle = wb.createCellStyle();
            sectionHeaderStyle.setFont(sectionFont);
            sectionHeaderStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            sectionHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            sectionHeaderStyle.setAlignment(HorizontalAlignment.LEFT);
            sectionHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Table Header Style
            tableHeaderStyle = wb.createCellStyle();
            tableHeaderStyle.setFont(headerFont);
            tableHeaderStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
            tableHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            tableHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
            tableHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorders(tableHeaderStyle);

            // Bold Label Style
            boldLabelStyle = wb.createCellStyle();
            boldLabelStyle.setFont(boldFont);
            boldLabelStyle.setAlignment(HorizontalAlignment.LEFT);
            setBorders(boldLabelStyle);

            // Normal Style
            normalStyle = wb.createCellStyle();
            normalStyle.setFont(normalFont);
            normalStyle.setAlignment(HorizontalAlignment.LEFT);
            setBorders(normalStyle);

            // Center Style
            centerStyle = wb.createCellStyle();
            centerStyle.setFont(normalFont);
            centerStyle.setAlignment(HorizontalAlignment.CENTER);
            setBorders(centerStyle);

            // Number Style
            numberStyle = wb.createCellStyle();
            numberStyle.setFont(normalFont);
            numberStyle.setDataFormat(df.getFormat("#,##0"));
            numberStyle.setAlignment(HorizontalAlignment.RIGHT);
            setBorders(numberStyle);

            // Currency Style
            currencyStyle = wb.createCellStyle();
            currencyStyle.setFont(normalFont);
            currencyStyle.setDataFormat(df.getFormat("#,##0 \"VND\""));
            currencyStyle.setAlignment(HorizontalAlignment.RIGHT);
            setBorders(currencyStyle);

            // Percent Style
            percentStyle = wb.createCellStyle();
            percentStyle.setFont(normalFont);
            percentStyle.setDataFormat(df.getFormat("0.00\"%\""));
            percentStyle.setAlignment(HorizontalAlignment.RIGHT);
            setBorders(percentStyle);

            // Status Stable Style (Green)
            statusStableStyle = wb.createCellStyle();
            statusStableStyle.setFont(boldFont);
            statusStableStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            statusStableStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            statusStableStyle.setAlignment(HorizontalAlignment.CENTER);
            statusStableStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorders(statusStableStyle);

            // Status Warning Style (Yellow)
            statusWarningStyle = wb.createCellStyle();
            statusWarningStyle.setFont(boldFont);
            statusWarningStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            statusWarningStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            statusWarningStyle.setAlignment(HorizontalAlignment.CENTER);
            statusWarningStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorders(statusWarningStyle);

            // Status Critical Style (Red)
            statusCriticalStyle = wb.createCellStyle();
            Font redFont = wb.createFont();
            redFont.setFontName("Calibri");
            redFont.setFontHeightInPoints((short) 11);
            redFont.setBold(true);
            redFont.setColor(IndexedColors.WHITE.getIndex());
            statusCriticalStyle.setFont(redFont);
            statusCriticalStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            statusCriticalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            statusCriticalStyle.setAlignment(HorizontalAlignment.CENTER);
            statusCriticalStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorders(statusCriticalStyle);

            // Total Row Styles
            tableTotalStyle = wb.createCellStyle();
            tableTotalStyle.setFont(boldFont);
            tableTotalStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            tableTotalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            tableTotalStyle.setAlignment(HorizontalAlignment.LEFT);
            setBorders(tableTotalStyle);

            tableTotalNumberStyle = wb.createCellStyle();
            tableTotalNumberStyle.setFont(boldFont);
            tableTotalNumberStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            tableTotalNumberStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            tableTotalNumberStyle.setDataFormat(df.getFormat("#,##0"));
            tableTotalNumberStyle.setAlignment(HorizontalAlignment.RIGHT);
            setBorders(tableTotalNumberStyle);

            tableTotalCurrencyStyle = wb.createCellStyle();
            tableTotalCurrencyStyle.setFont(boldFont);
            tableTotalCurrencyStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            tableTotalCurrencyStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            tableTotalCurrencyStyle.setDataFormat(df.getFormat("#,##0 \"VND\""));
            tableTotalCurrencyStyle.setAlignment(HorizontalAlignment.RIGHT);
            setBorders(tableTotalCurrencyStyle);

            tableTotalPercentStyle = wb.createCellStyle();
            tableTotalPercentStyle.setFont(boldFont);
            tableTotalPercentStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            tableTotalPercentStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            tableTotalPercentStyle.setDataFormat(df.getFormat("0.00\"%\""));
            tableTotalPercentStyle.setAlignment(HorizontalAlignment.RIGHT);
            setBorders(tableTotalPercentStyle);
        }

        private void setBorders(CellStyle style) {
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            style.setTopBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
            style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
            style.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
            style.setRightBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        }
    }
}
