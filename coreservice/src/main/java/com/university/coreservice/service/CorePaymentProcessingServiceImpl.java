package com.university.coreservice.service;

import com.university.coreservice.domain.entity.BankEntity;
import com.university.coreservice.domain.entity.PaymentTransactionEntity;
import com.university.coreservice.domain.model.MailNotificationMessage;
import com.university.coreservice.domain.model.PaymentRequestMessage;
import com.university.coreservice.domain.model.PaymentResultMessage;
import com.university.coreservice.domain.port.primary.CorePaymentProcessingService;
import com.university.coreservice.domain.port.secondary.BankRepositoryPort;
import com.university.coreservice.domain.port.secondary.KafkaResultPublisherPort;
import com.university.coreservice.domain.port.secondary.PaymentTransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Service implementation cho Primary Port CorePaymentProcessingService.
 * Phụ trách business logic xử lý giao dịch thanh toán.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CorePaymentProcessingServiceImpl implements CorePaymentProcessingService {

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";
    private static final String TYPE_BANK_TO_WALLET = "BANK_TO_WALLET";
    private static final String TYPE_WALLET_TO_BANK = "WALLET_TO_BANK";
    private static final String TYPE_WALLET_TRANSFER_OUT = "WALLET_TRANSFER_OUT";

    private final PaymentTransactionRepositoryPort paymentTransactionRepositoryPort;
    private final BankRepositoryPort bankRepositoryPort;
    private final KafkaResultPublisherPort kafkaResultPublisherPort;

    @Override
    @Transactional
    public void processPayment(PaymentRequestMessage request) {
        log.info("Processing payment: referenceCode={}, type={}, amount={}",
                request.getReferenceCode(), request.getTransactionType(), request.getAmount());

        // Idempotency check
        var existing = paymentTransactionRepositoryPort.findByIdempotencyKey(request.getIdempotencyKey());
        if (existing.isPresent()) {
            log.warn("Duplicate payment request detected (idempotency): idempotencyKey={}", request.getIdempotencyKey());
            PaymentTransactionEntity cached = existing.get();
            publishPaymentResultOnly(request, cached.getStatus(), cached.getFailureReason());
            return;
        }

        String status;
        String failureReason = null;

        try {
            status = switch (request.getTransactionType()) {
                case TYPE_BANK_TO_WALLET -> processBankToWallet(request);
                case TYPE_WALLET_TO_BANK -> processWalletToBank(request);
                case TYPE_WALLET_TRANSFER_OUT -> processWalletTransfer();
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

        PaymentTransactionEntity auditRecord = PaymentTransactionEntity.builder()
                .correlationId(request.getCorrelationId())
                .referenceCode(request.getReferenceCode())
                .transactionType(request.getTransactionType())
                .amount(request.getAmount())
                .status(status)
                .failureReason(failureReason)
                .idempotencyKey(request.getIdempotencyKey())
                .processedAt(OffsetDateTime.now())
                .build();
        paymentTransactionRepositoryPort.save(auditRecord);

        log.info("Payment processed: referenceCode={}, type={}, status={}",
                request.getReferenceCode(), request.getTransactionType(), status);

        final String finalStatus = status;
        final String finalFailureReason = failureReason;
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishResults(request, finalStatus, finalFailureReason);
                }
            });
        } else {
            publishResults(request, finalStatus, finalFailureReason);
        }
    }

    private String processBankToWallet(PaymentRequestMessage request) {
        BankEntity bank = lockBankAccount(request.getBankCode(), request.getAccountNumber());
        BigDecimal amount = request.getAmount();

        if (bank.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient bank balance: bankCode={}, accountNumber={}, available={}, required={}",
                    request.getBankCode(), request.getAccountNumber(), bank.getBalance(), amount);
            throw new InsufficientBalanceException("So du tai khoan ngan hang khong du de thuc hien giao dich.");
        }
        bank.setBalance(bank.getBalance().subtract(amount));
        bankRepositoryPort.save(bank);
        return STATUS_SUCCESS;
    }

    private String processWalletToBank(PaymentRequestMessage request) {
        BankEntity bank = lockBankAccount(request.getBankCode(), request.getAccountNumber());
        bank.setBalance(bank.getBalance().add(request.getAmount()));
        bankRepositoryPort.save(bank);
        return STATUS_SUCCESS;
    }

    private String processWalletTransfer() {
        return STATUS_SUCCESS;
    }

    private void publishPaymentResultOnly(PaymentRequestMessage request, String status, String failureReason) {
        PaymentResultMessage result = PaymentResultMessage.builder()
                .correlationId(request.getCorrelationId())
                .referenceCode(request.getReferenceCode())
                .transactionType(request.getTransactionType())
                .status(status)
                .failureReason(failureReason)
                .timestamp(OffsetDateTime.now().toString())
                .build();
        kafkaResultPublisherPort.publishPaymentResult(result);
    }

    private void publishResults(PaymentRequestMessage request, String status, String failureReason) {
        publishPaymentResultOnly(request, status, failureReason);

        MailNotificationMessage mail = MailNotificationMessage.builder()
                .to(request.getUserEmail())
                .fullName(request.getUserFullName())
                .transactionType(request.getTransactionType())
                .amount(request.getAmount())
                .status(status)
                .referenceCode(request.getReferenceCode())
                .description(request.getDescription())
                .failureReason(failureReason)
                .recipientFullName(request.getRecipientFullName())
                .recipientPhoneNumber(request.getRecipientPhoneNumber())
                .build();
        kafkaResultPublisherPort.publishMailNotification(mail);
    }

    private BankEntity lockBankAccount(String bankCode, String accountNumber) {
        return bankRepositoryPort
                .findByBankCodeAndAccountNumberForUpdate(bankCode, accountNumber)
                .orElseThrow(() -> {
                    log.warn("Bank account not found: bankCode={}, accountNumber={}", bankCode, accountNumber);
                    return new BankAccountNotFoundException("Khong tim thay tai khoan ngan hang.");
                });
    }

    public static class InsufficientBalanceException extends RuntimeException {
        public InsufficientBalanceException(String message) { super(message); }
    }

    public static class BankAccountNotFoundException extends RuntimeException {
        public BankAccountNotFoundException(String message) { super(message); }
    }
}
