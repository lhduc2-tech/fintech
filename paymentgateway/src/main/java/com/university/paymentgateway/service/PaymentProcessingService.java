package com.university.paymentgateway.service;

import com.university.paymentgateway.domain.entity.BankAccountEntity;
import com.university.paymentgateway.domain.entity.PaymentTransactionEntity;
import com.university.paymentgateway.infrastructure.jpa.repository.BankAccountRepository;
import com.university.paymentgateway.infrastructure.jpa.repository.PaymentTransactionRepository;
import com.university.paymentgateway.infrastructure.kafka.model.PaymentRequestMessage;
import com.university.paymentgateway.infrastructure.kafka.model.PaymentResultMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Core business logic của PaymentGateway:
 * - Kiểm tra idempotency (chống xử lý trùng khi Kafka retry)
 * - Kiểm tra và cập nhật số dư ngân hàng demo (bank_accounts)
 * - Lưu audit trail (payment_transactions)
 * - Trả về kết quả approve/reject
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProcessingService {

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";
    private static final String TYPE_BANK_TO_WALLET = "BANK_TO_WALLET";
    private static final String TYPE_WALLET_TO_BANK = "WALLET_TO_BANK";
    private static final String TYPE_WALLET_TRANSFER_OUT = "WALLET_TRANSFER_OUT";

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final BankAccountRepository bankAccountRepository;

    @Transactional
    public PaymentResultMessage process(PaymentRequestMessage request) {
        // ── Idempotency check ────────────────────────────────────────────────
        var existing = paymentTransactionRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existing.isPresent()) {
            log.warn("Duplicate payment request detected, returning cached result: idempotencyKey={}",
                    request.getIdempotencyKey());
            PaymentTransactionEntity cached = existing.get();
            return buildResult(request, cached.getStatus(), cached.getFailureReason());
        }

        // ── Xử lý theo loại giao dịch ────────────────────────────────────────
        String status;
        String failureReason = null;

        try {
            status = switch (request.getTransactionType()) {
                case TYPE_BANK_TO_WALLET -> processBankToWallet(request);
                case TYPE_WALLET_TO_BANK -> processWalletToBank(request);
                case TYPE_WALLET_TRANSFER_OUT -> processWalletTransfer(request);
                default -> {
                    log.warn("Unknown transaction type: {}", request.getTransactionType());
                    yield STATUS_FAILED;
                }
            };
        } catch (InsufficientBalanceException e) {
            log.warn("Insufficient balance: {}", e.getMessage());
            status = STATUS_FAILED;
            failureReason = e.getMessage();
        } catch (BankAccountNotFoundException e) {
            log.warn("Bank account not found: {}", e.getMessage());
            status = STATUS_FAILED;
            failureReason = e.getMessage();
        }

        // ── Lưu audit trail ─────────────────────────────────────────────────
        PaymentTransactionEntity paymentTransaction = PaymentTransactionEntity.builder()
                .correlationId(request.getCorrelationId())
                .referenceCode(request.getReferenceCode())
                .transactionType(request.getTransactionType())
                .amount(request.getAmount())
                .status(status)
                .failureReason(failureReason)
                .idempotencyKey(request.getIdempotencyKey())
                .processedAt(OffsetDateTime.now())
                .build();
        paymentTransactionRepository.save(paymentTransaction);

        log.info("Processed payment: referenceCode={}, type={}, status={}",
                request.getReferenceCode(), request.getTransactionType(), status);
        return buildResult(request, status, failureReason);
    }

    // ─── BANK_TO_WALLET: ngân hàng trừ tiền → ví nhận tiền ─────────────────

    private String processBankToWallet(PaymentRequestMessage request) {
        BankAccountEntity bank = lockBankAccount(request.getBankCode(), request.getAccountNumber());
        BigDecimal amount = request.getAmount();

        if (bank.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient bank balance: available=" + bank.getBalance() + ", required=" + amount);
        }
        bank.setBalance(bank.getBalance().subtract(amount));
        return STATUS_SUCCESS;
    }

    // ─── WALLET_TO_BANK: ví trừ tiền → ngân hàng nhận tiền ─────────────────

    private String processWalletToBank(PaymentRequestMessage request) {
        // Fintech đã hold balance từ ví → PG chỉ cộng vào ngân hàng
        BankAccountEntity bank = lockBankAccount(request.getBankCode(), request.getAccountNumber());
        bank.setBalance(bank.getBalance().add(request.getAmount()));
        return STATUS_SUCCESS;
    }

    // ─── WALLET_TRANSFER_OUT: ví → ví (PG approve, fintech xử lý balance) ──

    private String processWalletTransfer(PaymentRequestMessage request) {
        // Fintech đã hold balance của sender → PG chỉ cần approve
        // Không cần kiểm tra hay cập nhật bank_accounts
        return STATUS_SUCCESS;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private BankAccountEntity lockBankAccount(String bankCode, String accountNumber) {
        return bankAccountRepository
                .findByBankCodeAndAccountNumberForUpdate(bankCode, accountNumber)
                .orElseThrow(() -> new BankAccountNotFoundException(
                        "Bank account not found: bankCode=" + bankCode + ", accountNumber=" + accountNumber));
    }

    private PaymentResultMessage buildResult(PaymentRequestMessage request, String status, String failureReason) {
        return PaymentResultMessage.builder()
                .correlationId(request.getCorrelationId())
                .referenceCode(request.getReferenceCode())
                .transactionType(request.getTransactionType())
                .status(status)
                .failureReason(failureReason)
                .timestamp(OffsetDateTime.now().toString())
                .build();
    }

    // ─── Inner exceptions ─────────────────────────────────────────────────────

    public static class InsufficientBalanceException extends RuntimeException {
        public InsufficientBalanceException(String message) { super(message); }
    }

    public static class BankAccountNotFoundException extends RuntimeException {
        public BankAccountNotFoundException(String message) { super(message); }
    }
}
