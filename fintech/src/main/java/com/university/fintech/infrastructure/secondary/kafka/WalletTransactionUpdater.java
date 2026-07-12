package com.university.fintech.infrastructure.secondary.kafka;

import com.university.fintech.domain.entity.UserEntity;
import com.university.fintech.domain.entity.WalletEntity;
import com.university.fintech.domain.entity.WalletLedgerEntryEntity;
import com.university.fintech.domain.entity.WalletTransactionEntity;
import com.university.fintech.domain.port.primary.WalletNotificationMailService;
import com.university.fintech.infrastructure.secondary.jpa.repository.WalletLedgerEntryRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.WalletRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.WalletTransactionRepository;
import com.university.fintech.infrastructure.secondary.kafka.model.PaymentResultMessage;
import com.university.fintech.shared.constant.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Xử lý kết quả giao dịch từ PaymentGateway.
 * Cập nhật trạng thái transaction, số dư ví, ledger entry và gửi email.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletTransactionUpdater {

    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletLedgerEntryRepository walletLedgerEntryRepository;
    private final WalletRepository walletRepository;
    private final WalletNotificationMailService walletNotificationMailService;

    @Transactional
    public void handlePaymentResult(PaymentResultMessage result) {
        String referenceCode = result.getReferenceCode();
        String transactionType = result.getTransactionType();
        String status = result.getStatus();
        log.info("Handling payment result: referenceCode={}, type={}, status={}", referenceCode, transactionType, status);

        switch (transactionType) {
            case Constant.WALLET_TRANSACTION_TYPE_BANK_TO_WALLET ->
                    handleBankToWallet(referenceCode, status, result.getFailureReason());
            case Constant.WALLET_TRANSACTION_TYPE_WALLET_TO_BANK ->
                    handleWalletToBank(referenceCode, status, result.getFailureReason());
            case Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_OUT ->
                    handleWalletTransfer(referenceCode, status, result.getFailureReason());
            default ->
                    log.warn("Unknown transaction type in payment result: {}", transactionType);
        }
    }

    // ─── BANK_TO_WALLET ───────────────────────────────────────────────────────

    private void handleBankToWallet(String referenceCode, String status, String failureReason) {
        WalletTransactionEntity transaction = findPendingTransaction(
                referenceCode, Constant.WALLET_TRANSACTION_TYPE_BANK_TO_WALLET);
        if (transaction == null) return;

        if (Constant.STATUS_SUCCESS.equals(status)) {
            WalletEntity wallet = lockWallet(transaction.getWallet().getUser().getId());
            BigDecimal amount = transaction.getAmount();
            wallet.setAvailableBalance(wallet.getAvailableBalance().add(amount));

            transaction.setStatus(Constant.STATUS_SUCCESS);
            walletTransactionRepository.save(transaction);

            createLedgerEntry(transaction, wallet, Constant.LEDGER_ENTRY_TYPE_CREDIT, amount,
                    wallet.getAvailableBalance(), transaction.getDescription());
            log.info("BANK_TO_WALLET SUCCESS: referenceCode={}, +{}", referenceCode, amount);
            notifyUser(transaction.getWallet().getUser(), transaction);
        } else {
            transaction.setStatus(Constant.STATUS_FAILED);
            walletTransactionRepository.save(transaction);
            log.info("BANK_TO_WALLET FAILED: referenceCode={}, reason={}", referenceCode, failureReason);
            notifyUser(transaction.getWallet().getUser(), transaction);
        }
    }

    // ─── WALLET_TO_BANK ───────────────────────────────────────────────────────

    private void handleWalletToBank(String referenceCode, String status, String failureReason) {
        WalletTransactionEntity transaction = findPendingTransaction(
                referenceCode, Constant.WALLET_TRANSACTION_TYPE_WALLET_TO_BANK);
        if (transaction == null) return;

        WalletEntity wallet = lockWallet(transaction.getWallet().getUser().getId());
        BigDecimal amount = transaction.getAmount();

        if (Constant.STATUS_SUCCESS.equals(status)) {
            // Hold đã trừ sẵn khi gửi request → giờ chỉ cần giảm holdBalance
            wallet.setHoldBalance(wallet.getHoldBalance().subtract(amount));

            transaction.setStatus(Constant.STATUS_SUCCESS);
            walletTransactionRepository.save(transaction);

            createLedgerEntry(transaction, wallet, Constant.LEDGER_ENTRY_TYPE_DEBIT, amount,
                    wallet.getAvailableBalance(), transaction.getDescription());
            log.info("WALLET_TO_BANK SUCCESS: referenceCode={}, -{}", referenceCode, amount);
        } else {
            // Hoàn trả hold về available
            wallet.setHoldBalance(wallet.getHoldBalance().subtract(amount));
            wallet.setAvailableBalance(wallet.getAvailableBalance().add(amount));

            transaction.setStatus(Constant.STATUS_FAILED);
            walletTransactionRepository.save(transaction);
            log.info("WALLET_TO_BANK FAILED: referenceCode={}, restored hold, reason={}", referenceCode, failureReason);
        }
        notifyUser(transaction.getWallet().getUser(), transaction);
    }

    // ─── WALLET_TRANSFER ──────────────────────────────────────────────────────

    private void handleWalletTransfer(String referenceCode, String status, String failureReason) {
        WalletTransactionEntity debitTx = findPendingTransaction(
                referenceCode, Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_OUT);
        WalletTransactionEntity creditTx = findPendingTransaction(
                referenceCode, Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_IN);

        if (debitTx == null || creditTx == null) {
            log.warn("Wallet transfer transactions not found for referenceCode={}", referenceCode);
            return;
        }

        UUID senderId = debitTx.getWallet().getUser().getId();
        UUID recipientId = creditTx.getWallet().getUser().getId();

        // Lock theo thứ tự consistent để tránh deadlock
        List<UUID> orderedIds = List.of(senderId, recipientId).stream()
                .sorted(Comparator.naturalOrder()).toList();
        WalletEntity firstWallet = lockWallet(orderedIds.get(0));
        WalletEntity secondWallet = lockWallet(orderedIds.get(1));

        WalletEntity senderWallet = firstWallet.getUser().getId().equals(senderId) ? firstWallet : secondWallet;
        WalletEntity recipientWallet = firstWallet.getUser().getId().equals(recipientId) ? firstWallet : secondWallet;
        BigDecimal amount = debitTx.getAmount();

        if (Constant.STATUS_SUCCESS.equals(status)) {
            // Sender: holdBalance đã trừ khi gửi request → giờ chỉ giảm hold
            senderWallet.setHoldBalance(senderWallet.getHoldBalance().subtract(amount));
            // Recipient: cộng vào available
            recipientWallet.setAvailableBalance(recipientWallet.getAvailableBalance().add(amount));

            debitTx.setStatus(Constant.STATUS_SUCCESS);
            creditTx.setStatus(Constant.STATUS_SUCCESS);
            walletTransactionRepository.save(debitTx);
            walletTransactionRepository.save(creditTx);

            createLedgerEntry(debitTx, senderWallet, Constant.LEDGER_ENTRY_TYPE_DEBIT, amount,
                    senderWallet.getAvailableBalance(), debitTx.getDescription());
            createLedgerEntry(creditTx, recipientWallet, Constant.LEDGER_ENTRY_TYPE_CREDIT, amount,
                    recipientWallet.getAvailableBalance(), creditTx.getDescription());

            log.info("WALLET_TRANSFER SUCCESS: referenceCode={}, amount={}", referenceCode, amount);
            notifyUser(debitTx.getWallet().getUser(), debitTx);
            notifyUser(creditTx.getWallet().getUser(), creditTx);
        } else {
            // Hoàn trả hold về available cho sender
            senderWallet.setHoldBalance(senderWallet.getHoldBalance().subtract(amount));
            senderWallet.setAvailableBalance(senderWallet.getAvailableBalance().add(amount));

            debitTx.setStatus(Constant.STATUS_FAILED);
            creditTx.setStatus(Constant.STATUS_FAILED);
            walletTransactionRepository.save(debitTx);
            walletTransactionRepository.save(creditTx);

            log.info("WALLET_TRANSFER FAILED: referenceCode={}, reason={}", referenceCode, failureReason);
            notifyUser(debitTx.getWallet().getUser(), debitTx);
            notifyUser(creditTx.getWallet().getUser(), creditTx);
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Tìm transaction PENDING theo referenceCode + type.
     * Nếu đã được xử lý (SUCCESS/FAILED) → bỏ qua (idempotency).
     */
    private WalletTransactionEntity findPendingTransaction(String referenceCode, String type) {
        return walletTransactionRepository
                .findByReferenceCodeAndType(referenceCode, type)
                .map(tx -> {
                    if (!Constant.STATUS_PENDING.equals(tx.getStatus())) {
                        log.warn("Transaction already processed: referenceCode={}, type={}, status={}",
                                referenceCode, type, tx.getStatus());
                        return null;
                    }
                    return tx;
                })
                .orElseGet(() -> {
                    log.error("Transaction not found: referenceCode={}, type={}", referenceCode, type);
                    return null;
                });
    }

    private WalletEntity lockWallet(UUID userId) {
        return walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for userId: " + userId));
    }

    private void createLedgerEntry(
            WalletTransactionEntity transaction,
            WalletEntity wallet,
            String type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            String description
    ) {
        walletLedgerEntryRepository.save(
                WalletLedgerEntryEntity.builder()
                        .transaction(transaction)
                        .wallet(wallet)
                        .type(type)
                        .amount(amount)
                        .balanceAfter(balanceAfter)
                        .description(description)
                        .build()
        );
    }

    private void notifyUser(UserEntity user, WalletTransactionEntity transaction) {
        try {
            if (user == null || user.getEmail() == null || user.getEmail().isBlank()) return;
            walletNotificationMailService.sendTransactionNotification(
                    user.getEmail(),
                    user.getFullName(),
                    transaction.getType(),
                    transaction.getAmount(),
                    transaction.getStatus(),
                    transaction.getReferenceCode(),
                    transaction.getDescription(),
                    null, null
            );
        } catch (Exception e) {
            log.warn("Could not send notification to user {}: {}", user.getId(), e.getMessage());
        }
    }
}
