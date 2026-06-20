package com.university.fintech.domain.port.primary;

import com.university.fintech.infrastructure.primary.restful.request.wallet.LinkBankAccountRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletBankTransferRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletRecipientLookupRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletTransferRequest;
import com.university.fintech.infrastructure.primary.restful.response.wallet.LinkedBankAccountResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletRecipientLookupResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletTransactionDetailResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletTransactionHistoryResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletTransactionResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface WalletBankService {

    WalletResponse getWallet(UUID userId);

    LinkedBankAccountResponse linkBankAccount(UUID userId, LinkBankAccountRequest request);

    List<LinkedBankAccountResponse> getLinkedBankAccounts(UUID userId);

    WalletTransactionResponse topUpFromBank(UUID userId, WalletBankTransferRequest request);

    WalletTransactionResponse withdrawToBank(UUID userId, WalletBankTransferRequest request);

    WalletTransactionResponse transferToWallet(UUID userId, WalletTransferRequest request);

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
}
