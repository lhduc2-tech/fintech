package com.university.fintech.service.wallet;

import com.university.fintech.domain.entity.WalletTransactionEntity;
import com.university.fintech.shared.constant.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Scheduled job chạy định kỳ để phát hiện giao dịch PENDING bị "treo".
 *
 * <p>Chỉ chịu trách nhiệm điều phối (orchestration): scan danh sách và
 * gọi {@link PendingTransactionTimeoutProcessor} để xử lý từng giao dịch.
 * Logic @Transactional được đặt trong Processor để tránh Spring AOP
 * self-invocation issue.
 *
 * <p>Giao dịch bị coi là timed-out khi:
 * <ul>
 *   <li>Status vẫn là PENDING</li>
 *   <li>createdAt đã vượt quá {@code pending-timeout-minutes}</li>
 * </ul>
 *
 * <p>Hành động khi timeout:
 * <ul>
 *   <li>WALLET_TO_BANK / WALLET_TRANSFER_OUT: hoàn holdBalance → availableBalance</li>
 *   <li>BANK_TO_WALLET: không có hold, chỉ đánh TIMED_OUT</li>
 *   <li>Gửi email thông báo đến user</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PendingTransactionTimeoutJob {

    private static final List<String> TIMEOUT_ELIGIBLE_TYPES = List.of(
            Constant.WALLET_TRANSACTION_TYPE_BANK_TO_WALLET,
            Constant.WALLET_TRANSACTION_TYPE_WALLET_TO_BANK,
            Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_OUT,
            Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_IN
    );

    private final PendingTransactionTimeoutProcessor processor;

    @Value(Constant.WALLET_PENDING_TIMEOUT_MINUTES_PROPERTY)
    private int pendingTimeoutMinutes;

    /**
     * Chạy định kỳ theo {@code timeout-check-interval-ms}.
     * Scan các giao dịch stuck → delegate sang Processor để expire từng cái
     * trong transaction riêng biệt (1 fail không ảnh hưởng cái khác).
     */
    @Scheduled(fixedDelayString = "${application.wallet.timeout-check-interval-ms:300000}")
    public void expireTimedOutTransactions() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusMinutes(pendingTimeoutMinutes);
        log.info("Timeout job running: cutoff={} (pending-timeout={}m)", cutoff, pendingTimeoutMinutes);

        List<WalletTransactionEntity> stuckTransactions =
                processor.findStuckTransactions(cutoff, TIMEOUT_ELIGIBLE_TYPES);

        if (stuckTransactions.isEmpty()) {
            log.debug("Timeout job: no stuck PENDING transactions found.");
            return;
        }

        log.warn("Timeout job: found {} stuck PENDING transaction(s).", stuckTransactions.size());

        for (WalletTransactionEntity tx : stuckTransactions) {
            try {
                processor.expireSingleTransaction(tx.getId());
            } catch (Exception e) {
                log.error("Timeout job: failed to expire transaction id={}, referenceCode={}: {}",
                        tx.getId(), tx.getReferenceCode(), e.getMessage(), e);
            }
        }
    }
}
