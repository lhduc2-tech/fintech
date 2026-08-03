package com.university.fintech.infrastructure.primary.restful.wallet;

import com.university.fintech.domain.port.primary.WalletBankService;
import com.university.fintech.domain.port.primary.WalletPinService;
import com.university.fintech.infrastructure.primary.restful.request.wallet.ConfirmCreatePinRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.CreatePinRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.LinkBankAccountRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletBankTransferRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletQrDecodeRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletRecipientLookupRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletTransactionHistoryRequest;
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
import com.university.fintech.shared.constant.Constant;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${application.path.base.private}/wallet")
public class WalletOperator {

    private final WalletBankService walletBankService;
    private final WalletPinService walletPinService;

    @GetMapping
    public WalletResponse getWallet(Authentication authentication) {
        return walletBankService.getWallet(extractUserId(authentication));
    }

    @GetMapping("/banks")
    public List<BankResponse> getBanks() {
        return walletBankService.getBanks();
    }

    @GetMapping("/topup-methods")
    public List<String> getTopUpMethods() {
        return walletBankService.getTopUpMethods();
    }

    @PostMapping("/bank-accounts")
    public LinkedBankAccountResponse linkBankAccount(
            Authentication authentication,
            @Valid @RequestBody LinkBankAccountRequest request
    ) {
        return walletBankService.linkBankAccount(extractUserId(authentication), request);
    }

    @GetMapping("/bank-accounts")
    public List<LinkedBankAccountResponse> getLinkedBankAccounts(Authentication authentication) {
        return walletBankService.getLinkedBankAccounts(extractUserId(authentication));
    }

    @DeleteMapping("/bank-accounts/{id}")
    public ResponseEntity<Void> unlinkBankAccount(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        walletBankService.unlinkBankAccount(extractUserId(authentication), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bank-topup")
    public WalletTransactionResponse topUpFromBank(
            Authentication authentication,
            @Valid @RequestBody WalletBankTransferRequest request
    ) {
        return walletBankService.topUpFromBank(extractUserId(authentication), request);
    }

    @PostMapping("/bank-withdraw")
    public WalletTransactionResponse withdrawToBank(
            Authentication authentication,
            @Valid @RequestBody WalletBankTransferRequest request
    ) {
        return walletBankService.withdrawToBank(extractUserId(authentication), request);
    }

    @PostMapping("/transfer")
    public WalletTransactionResponse transferToWallet(
            Authentication authentication,
            @Valid @RequestBody WalletTransferRequest request
    ) {
        return walletBankService.transferToWallet(extractUserId(authentication), request);
    }

    @GetMapping("/transfer/recipient")
    public WalletRecipientLookupResponse lookupTransferRecipient(
            Authentication authentication,
            @Valid WalletRecipientLookupRequest request
    ) {
        return walletBankService.lookupTransferRecipient(extractUserId(authentication), request);
    }

    @GetMapping("/transactions/{id}")
    public WalletTransactionDetailResponse getTransactionDetail(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        return walletBankService.getTransactionDetail(extractUserId(authentication), id);
    }

    /**
     * Lấy context của giao dịch thất bại để client pre-fill lại màn hình xác nhận khi retry.
     *
     * <p>Response bao gồm:
     * <ul>
     *   <li>{@code retryable} — có thể thử lại không (chỉ {@code FAILED}/{@code TIMED_OUT})</li>
     *   <li>{@code amount}, {@code type} — thông tin giao dịch</li>
     *   <li>{@code recipientPhoneNumber}, {@code recipientFullName} — cho chuyển ví</li>
     *   <li>{@code linkedBankAccountId} — cho giao dịch bank</li>
     *   <li>{@code nonRetryableReason} — lý do nếu không thể retry</li>
     * </ul>
     *
     * <p>Client nên generate {@code idempotencyKey} mới khi thực hiận retry,
     * và yêu cầu nhập lại PIN hoặc request OTP mới.
     */
    @GetMapping("/transactions/{id}/retry-context")
    public WalletRetryContextResponse getRetryContext(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        return walletBankService.getRetryContext(extractUserId(authentication), id);
    }

    @PostMapping("/transactions/search")
    public WalletTransactionHistoryResponse getTransactions(
            Authentication authentication,
            @RequestBody(required = false) WalletTransactionHistoryRequest request
    ) {
        WalletTransactionHistoryRequest safeRequest = request == null ? new WalletTransactionHistoryRequest() : request;
        return walletBankService.getTransactions(
                extractUserId(authentication),
                safeRequest.getType(),
                safeRequest.getStatus(),
                safeRequest.getFrom(),
                safeRequest.getTo(),
                safeRequest.toPageable()
        );
    }

    @GetMapping("/pin/status")
    public Map<String, Boolean> getPinStatus(Authentication authentication) {
        boolean hasPinValue = walletBankService.hasPin(extractUserId(authentication));
        return Map.of("hasPin", hasPinValue);
    }

    @PostMapping("/pin/request")
    public Map<String, String> requestCreatePin(
            Authentication authentication,
            @Valid @RequestBody CreatePinRequest request
    ) {
        walletPinService.requestCreatePin(extractUserId(authentication), request.getPin());
        return Map.of("message", Constant.MESSAGE_PIN_OTP_SENT);
    }

    @PostMapping("/pin/confirm")
    public Map<String, String> confirmCreatePin(
            Authentication authentication,
            @Valid @RequestBody ConfirmCreatePinRequest request
    ) {
        walletPinService.confirmCreatePin(extractUserId(authentication), request.getOtpCode());
        return Map.of("message", Constant.MESSAGE_PIN_CREATED);
    }

    @PostMapping("/pin/request-transaction-otp")
    public Map<String, String> requestTransactionOtp(Authentication authentication) {
        walletBankService.requestTransactionOtp(extractUserId(authentication));
        return Map.of("message", Constant.MESSAGE_TRANSACTION_OTP_SENT);
    }

    /**
     * Generate QR code cho ví của user hiện tại.
     * QR image trả về dưới dạng Base64 PNG string.
     * Client đưa vào: {@code <img src="data:image/png;base64,{qrBase64}">}
     */
    @GetMapping("/qr")
    public WalletQrResponse generateWalletQr(Authentication authentication) {
        return walletBankService.generateWalletQr(extractUserId(authentication));
    }

    /**
     * Decode QR content string để lấy thông tin người nhận.
     * Client tự quét QR bằng camera, lấy raw string rồi gửi vào đây.
     * Response trả về phoneNumber + fullName để tự động điền form chuyển tiền.
     */
    @PostMapping("/qr/decode")
    public WalletQrDecodeResponse decodeWalletQr(
            Authentication authentication,
            @Valid @RequestBody WalletQrDecodeRequest request
    ) {
        return walletBankService.decodeWalletQr(request);
    }

    @SuppressWarnings("unchecked")
    private UUID extractUserId(Authentication authentication) {
        Map<String, String> principal = (Map<String, String>) authentication.getPrincipal();
        return UUID.fromString(principal.get("userId"));
    }
}
