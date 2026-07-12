package com.university.fintech.infrastructure.secondary.banking;

import com.university.fintech.domain.port.secondary.WalletTransactionProcessor;
import com.university.fintech.domain.port.secondary.model.WalletProcessingRequest;
import com.university.fintech.domain.port.secondary.model.WalletProcessingResult;
import com.university.fintech.shared.constant.Constant;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@ConditionalOnProperty(name = "application.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class SimulatedWalletTransactionProcessor implements WalletTransactionProcessor {

    @Override
    public WalletProcessingResult process(WalletProcessingRequest request) {
        String key = request.getIdempotencyKey() == null
                ? Constant.EMPTY
                : request.getIdempotencyKey().toUpperCase(Locale.ROOT);
        if (key.contains(Constant.WALLET_PROCESSING_TEST_PENDING_TOKEN)) {
            return WalletProcessingResult.builder()
                    .status(Constant.STATUS_PENDING)
                    .message("External processor keeps the transaction pending.")
                    .build();
        }
        if (key.contains(Constant.WALLET_PROCESSING_TEST_FAILED_TOKEN)) {
            return WalletProcessingResult.builder()
                    .status(Constant.STATUS_FAILED)
                    .message("External processor rejected the transaction.")
                    .build();
        }
        return WalletProcessingResult.builder()
                .status(Constant.STATUS_SUCCESS)
                .message("External processor approved the transaction.")
                .build();
    }
}
