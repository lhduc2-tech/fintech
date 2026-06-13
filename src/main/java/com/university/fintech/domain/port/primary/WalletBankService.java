package com.university.fintech.domain.port.primary;

import com.university.fintech.infrastructure.primary.restful.request.wallet.LinkBankAccountRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletBankTransferRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletTransferRequest;
import com.university.fintech.infrastructure.primary.restful.response.PageResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.LinkedBankAccountResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletTransactionResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface WalletBankService {

    WalletResponse getWallet(UUID userId);

    LinkedBankAccountResponse linkBankAccount(UUID userId, LinkBankAccountRequest request);

    List<LinkedBankAccountResponse> getLinkedBankAccounts(UUID userId);

    WalletTransactionResponse topUpFromBank(UUID userId, WalletBankTransferRequest request);

    WalletTransactionResponse withdrawToBank(UUID userId, WalletBankTransferRequest request);

    WalletTransactionResponse transferToWallet(UUID userId, WalletTransferRequest request);

    PageResponse<WalletTransactionResponse> getTransactions(UUID userId, Pageable pageable);
}
