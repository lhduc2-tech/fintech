package com.university.fintech.infrastructure.primary.restful.response.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response trả về context của giao dịch thất bại để client có thể
 * pre-fill lại màn hình xác nhận chuyển tiền khi retry.
 *
 * <p>Field {@code retryable} cho biết giao dịch này có thể thử lại không.
 * Chỉ {@code FAILED} và {@code TIMED_OUT} mới có thể retry.
 *
 * <p>Các field {@code recipientPhoneNumber}, {@code linkedBankAccountId}
 * sẽ được populate tùy theo loại giao dịch ({@code type}).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletRetryContextResponse {

    /** ID của giao dịch gốc bị lỗi. */
    private UUID transactionId;

    /**
     * Loại giao dịch:
     * {@code WALLET_TRANSFER_OUT}, {@code WALLET_TO_BANK}, {@code BANK_TO_WALLET}.
     */
    private String type;

    /**
     * Trạng thái hiện tại: {@code FAILED} hoặc {@code TIMED_OUT}.
     */
    private String status;

    /**
     * {@code true} nếu giao dịch có thể thử lại (status là FAILED hoặc TIMED_OUT).
     * {@code false} nếu giao dịch đã SUCCESS hoặc đang PENDING.
     */
    private boolean retryable;

    /** Số tiền giao dịch gốc. */
    private BigDecimal amount;

    /**
     * Số điện thoại người nhận — chỉ có giá trị khi {@code type = WALLET_TRANSFER_OUT}.
     * Null với các loại giao dịch bank.
     */
    private String recipientPhoneNumber;

    /**
     * Tên người nhận — chỉ có giá trị khi {@code type = WALLET_TRANSFER_OUT}.
     * Null với các loại giao dịch bank.
     */
    private String recipientFullName;

    /**
     * ID tài khoản ngân hàng đã liên kết — chỉ có giá trị khi
     * {@code type = WALLET_TO_BANK} hoặc {@code BANK_TO_WALLET}.
     * Null với giao dịch chuyển ví.
     */
    private UUID linkedBankAccountId;

    /** Mô tả giao dịch gốc (ví dụ: "Chuyển tiền đến 0901234567"). */
    private String description;

    /** Thời điểm giao dịch gốc được tạo. */
    private OffsetDateTime originalCreatedAt;

    /**
     * Lý do không thể retry (nếu {@code retryable = false}).
     * Ví dụ: "Giao dich dang xu ly" (PENDING), "Giao dich da thanh cong" (SUCCESS).
     * Null nếu retryable = true.
     */
    private String nonRetryableReason;
}
