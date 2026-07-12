package com.university.fintech.domain.port.secondary;

import com.university.fintech.domain.port.secondary.model.WalletProcessingRequest;
import com.university.fintech.domain.port.secondary.model.WalletProcessingResult;

public interface WalletTransactionProcessor {

    WalletProcessingResult process(WalletProcessingRequest request);
}
