package com.university.fintech.infrastructure.primary.restful.wallet;

import com.university.fintech.domain.port.primary.WalletBankService;
import com.university.fintech.infrastructure.primary.restful.request.wallet.LinkBankAccountRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletBankTransferRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletRecipientLookupRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletTransactionHistoryRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletTransferRequest;
import com.university.fintech.infrastructure.primary.restful.response.PageResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.LinkedBankAccountResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletRecipientLookupResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletTransactionDetailResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletTransactionHistoryResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletTransactionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
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

    @GetMapping
    public WalletResponse getWallet(Authentication authentication) {
        return walletBankService.getWallet(extractUserId(authentication));
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

    @PostMapping("/transactions/search")
    public PageResponse<WalletTransactionHistoryResponse> getTransactions(
            Authentication authentication,
            @RequestBody(required = false) WalletTransactionHistoryRequest request,
            Pageable pageable
    ) {
        WalletTransactionHistoryRequest safeRequest = request == null ? new WalletTransactionHistoryRequest() : request;
        return walletBankService.getTransactions(
                extractUserId(authentication),
                safeRequest.getType(),
                safeRequest.getStatus(),
                safeRequest.getFrom(),
                safeRequest.getTo(),
                pageable
        );
    }

    @SuppressWarnings("unchecked")
    private UUID extractUserId(Authentication authentication) {
        Map<String, String> principal = (Map<String, String>) authentication.getPrincipal();
        return UUID.fromString(principal.get("userId"));
    }
}
