package com.university.fintech.domain.port.primary;

import com.university.fintech.infrastructure.primary.restful.request.wallet.LinkBankAccountRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletBankTransferRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletQrDecodeRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletRecipientLookupRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletTransferRequest;
import com.university.fintech.infrastructure.primary.restful.response.wallet.BankResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.LinkedBankAccountResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletQrDecodeResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletQrResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletRecipientLookupResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletRetryContextResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletTransactionDetailResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletTransactionHistoryResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletTransactionResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface WalletBankService {

    WalletResponse getWallet(UUID userId);

    List<BankResponse> getBanks();

    List<String> getTopUpMethods();

    LinkedBankAccountResponse linkBankAccount(UUID userId, LinkBankAccountRequest request);

    List<LinkedBankAccountResponse> getLinkedBankAccounts(UUID userId);

    void unlinkBankAccount(UUID userId, UUID bankAccountId);

    WalletTransactionResponse topUpFromBank(UUID userId, WalletBankTransferRequest request);

    WalletTransactionResponse withdrawToBank(UUID userId, WalletBankTransferRequest request);

    WalletTransactionResponse transferToWallet(UUID userId, WalletTransferRequest request);

    void requestTransactionOtp(UUID userId);

    boolean hasPin(UUID userId);

    WalletRecipientLookupResponse lookupTransferRecipient(UUID userId, WalletRecipientLookupRequest request);

    WalletTransactionDetailResponse getTransactionDetail(UUID userId, UUID transactionId);

    WalletTransactionHistoryResponse getTransactions(
            UUID userId,
            String type,
            String status,
            LocalDate updatedAtFrom,
            LocalDate updatedAtTo,
            Pageable pageable
    );

    /**
     * Lấy context của giao dịch thất bại để client pre-fill màn hình retry.
     *
     * <p>Chỉ trả về đầy đủ context cho giao dịch ở trạng thái {@code FAILED}
     * hoặc {@code TIMED_OUT}. Với các trạng thái khác, vẫn trả về response
     * nhưng {@code retryable = false} và {@code nonRetryableReason} giải thích lý do.
     *
     * @param userId        ID của user đang request (phải là chủ giao dịch)
     * @param transactionId ID của giao dịch cần retry
     * @return context để client hiển thị lại màn hình xác nhận
     */
    WalletRetryContextResponse getRetryContext(UUID userId, UUID transactionId);

    /**
     * Generate QR code image cho ví của userId hiện tại.
     * QR encode JSON chứa phoneNumber và fullName.
     */
    WalletQrResponse generateWalletQr(UUID userId);

    /**
     * Decode QR content string và trả về thông tin người nhận.
     * Dùng để tự động điền form chuyển tiền sau khi quét QR.
     */
    WalletQrDecodeResponse decodeWalletQr(WalletQrDecodeRequest request);
}

