package com.university.fintech.service.wallet;

import com.university.fintech.domain.entity.UserEntity;
import com.university.fintech.domain.entity.WalletEntity;
import com.university.fintech.domain.entity.WalletTransactionEntity;
import com.university.fintech.domain.port.primary.WalletNotificationMailService;
import com.university.fintech.infrastructure.secondary.jpa.repository.WalletRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.WalletTransactionRepository;
import com.university.fintech.shared.constant.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Xử lý từng giao dịch timeout trong transaction riêng biệt.
 *
 * <p>Tách ra khỏi {@link PendingTransactionTimeoutJob} để Spring AOP proxy
 * có thể intercept đúng {@code @Transactional} — tránh self-invocation issue.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PendingTransactionTimeoutProcessor {

    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletRepository walletRepository;
    private final WalletNotificationMailService walletNotificationMailService;

    /**
     * Scan danh sách giao dịch PENDING bị stuck (plain SELECT, không cần transaction).
     */
    @Transactional(readOnly = true)
    public List<WalletTransactionEntity> findStuckTransactions(
            OffsetDateTime cutoff,
            Collection<String> eligibleTypes
    ) {
        return walletTransactionRepository.findPendingTransactionsCreatedBefore(cutoff, eligibleTypes);
    }

    /**
     * Xử lý timeout cho từng giao dịch trong transaction độc lập.
     * Nếu một giao dịch fail thì không ảnh hưởng đến các giao dịch khác.
     *
     * <p>Double-check status trước khi xử lý: Kafka result có thể đến
     * đúng lúc job đang chạy — trường hợp đó bỏ qua an toàn.
     */
    @Transactional
    public void expireSingleTransaction(UUID transactionId) {
        WalletTransactionEntity tx = walletTransactionRepository.findById(transactionId)
                .orElse(null);

        if (tx == null) {
            log.warn("Timeout job: transaction {} not found, skipping.", transactionId);
            return;
        }

        // Double-check: Kafka có thể đã update status trong lúc job scan xong
        if (!Constant.STATUS_PENDING.equals(tx.getStatus())) {
            log.info("Timeout job: transaction {} already in status={}, skipping.",
                    transactionId, tx.getStatus());
            return;
        }

        String type = tx.getType();
        log.warn("Timeout job: expiring transaction id={}, referenceCode={}, type={}, createdAt={}",
                tx.getId(), tx.getReferenceCode(), type, tx.getCreatedAt());

        switch (type) {
            case Constant.WALLET_TRANSACTION_TYPE_WALLET_TO_BANK ->
                    handleWalletToBankTimeout(tx);

            case Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_OUT ->
                    handleWalletTransferOutTimeout(tx);

            case Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_IN ->
                    // Credit leg — không có hold, chỉ đánh TIMED_OUT.
                    // Sender leg (TRANSFER_OUT) đã hoàn tiền ở case riêng.
                    markTimedOut(tx);

            case Constant.WALLET_TRANSACTION_TYPE_BANK_TO_WALLET ->
                    // Không có hold → chỉ đánh TIMED_OUT
                    markTimedOut(tx);

            default ->
                    log.warn("Timeout job: unknown type={} for transaction {}, marking TIMED_OUT.",
                            type, transactionId);
        }

        walletTransactionRepository.save(tx);
        sendTimeoutNotification(tx);
    }

    // ─── Handlers ────────────────────────────────────────────────────────────

    /**
     * WALLET_TO_BANK timeout: hoàn lại hold về available cho user.
     */
    private void handleWalletToBankTimeout(WalletTransactionEntity tx) {
        WalletEntity wallet = lockWallet(tx.getWallet().getUser().getId());
        BigDecimal amount = tx.getAmount();

        wallet.setHoldBalance(wallet.getHoldBalance().subtract(amount));
        wallet.setAvailableBalance(wallet.getAvailableBalance().add(amount));
        walletRepository.save(wallet);

        log.info("Timeout job: WALLET_TO_BANK refunded hold={} to available for user={}",
                amount, tx.getUser().getId());
        markTimedOut(tx);
    }

    /**
     * WALLET_TRANSFER_OUT timeout: hoàn lại hold về available cho sender,
     * đồng thời đánh TIMED_OUT cho credit leg (TRANSFER_IN) trong cùng transaction.
     */
    private void handleWalletTransferOutTimeout(WalletTransactionEntity tx) {
        // Đánh TIMED_OUT cho credit leg cùng referenceCode
        walletTransactionRepository
                .findByReferenceCodeAndType(
                        tx.getReferenceCode(),
                        Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_IN)
                .ifPresent(creditTx -> {
                    if (Constant.STATUS_PENDING.equals(creditTx.getStatus())) {
                        markTimedOut(creditTx);
                        walletTransactionRepository.save(creditTx);
                        log.info("Timeout job: marked TRANSFER_IN leg TIMED_OUT, referenceCode={}",
                                tx.getReferenceCode());
                    }
                });

        // Hoàn tiền hold về available cho sender
        WalletEntity senderWallet = lockWallet(tx.getWallet().getUser().getId());
        BigDecimal amount = tx.getAmount();

        senderWallet.setHoldBalance(senderWallet.getHoldBalance().subtract(amount));
        senderWallet.setAvailableBalance(senderWallet.getAvailableBalance().add(amount));
        walletRepository.save(senderWallet);

        log.info("Timeout job: WALLET_TRANSFER_OUT refunded hold={} to available for sender={}",
                amount, tx.getUser().getId());
        markTimedOut(tx);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void markTimedOut(WalletTransactionEntity tx) {
        tx.setStatus(Constant.STATUS_TIMED_OUT);
    }

    private WalletEntity lockWallet(UUID userId) {
        return walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new RuntimeException(
                        "Timeout job: wallet not found for userId: " + userId));
    }

    private void sendTimeoutNotification(WalletTransactionEntity tx) {
        try {
            UserEntity user = tx.getUser();
            if (user == null || user.getEmail() == null || user.getEmail().isBlank()) return;
            walletNotificationMailService.sendTransactionNotification(
                    user.getEmail(),
                    user.getFullName(),
                    tx.getType(),
                    tx.getAmount(),
                    Constant.STATUS_TIMED_OUT,
                    tx.getReferenceCode(),
                    tx.getDescription(),
                    null,
                    null
            );
        } catch (Exception e) {
            log.warn("Timeout job: could not send timeout notification for transaction {}: {}",
                    tx.getId(), e.getMessage());
        }
    }
}
