package com.university.fintech.service.wallet;

import com.university.fintech.domain.dto.LinkedBankAccountDTO;
import com.university.fintech.domain.dto.WalletTransactionDTO;
import com.university.fintech.domain.entity.BankEntity;
import com.university.fintech.domain.entity.LinkedBankAccountEntity;
import com.university.fintech.domain.entity.UserEntity;
import com.university.fintech.domain.entity.WalletEntity;
import com.university.fintech.domain.entity.WalletTransactionEntity;
import com.university.fintech.domain.mapper.LinkedBankAccountMapper;
import com.university.fintech.domain.mapper.WalletMapper;
import com.university.fintech.domain.mapper.WalletTransactionMapper;
import com.university.fintech.domain.port.primary.WalletBankService;
import com.university.fintech.exception.domain.AuthException;
import com.university.fintech.infrastructure.primary.restful.request.wallet.LinkBankAccountRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletBankTransferRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletTransferRequest;
import com.university.fintech.infrastructure.primary.restful.response.PageResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.LinkedBankAccountResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletTransactionResponse;
import com.university.fintech.infrastructure.secondary.jpa.repository.BankRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.LinkedBankAccountRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.UserRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.WalletRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.WalletTransactionRepository;
import com.university.fintech.shared.constant.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletBankServiceImpl implements WalletBankService {

    private final UserRepository userRepository;
    private final BankRepository bankRepository;
    private final WalletRepository walletRepository;
    private final LinkedBankAccountRepository linkedBankAccountRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletMapper walletMapper;
    private final LinkedBankAccountMapper linkedBankAccountMapper;
    private final WalletTransactionMapper walletTransactionMapper;

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWallet(UUID userId) {
        return toWalletResponse(walletMapper.toDto(findWallet(userId)));
    }

    @Override
    @Transactional
    public LinkedBankAccountResponse linkBankAccount(UUID userId, LinkBankAccountRequest request) {
        WalletEntity wallet = findWallet(userId);
        String bankCode = request.getBankCode().trim().toUpperCase();
        String accountNumber = request.getAccountNumber().trim();
        BankEntity bank = bankRepository.findByBankCodeAndAccountNumber(bankCode, accountNumber)
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_BANK_NOT_FOUND, Constant.ERROR_BANK_NOT_FOUND));

        if (linkedBankAccountRepository.existsByBankId(bank.getId())) {
            throw new AuthException(
                    Constant.MESSAGE_BANK_ACCOUNT_ALREADY_LINKED,
                    Constant.ERROR_BANK_ACCOUNT_ALREADY_LINKED
            );
        }

        LinkedBankAccountDTO bankAccountDto = LinkedBankAccountDTO.builder()
                .walletId(wallet.getId())
                .bankId(bank.getId())
                .status(Constant.STATUS_ACTIVE)
                .build();
        LinkedBankAccountEntity bankAccount = linkedBankAccountMapper.toEntity(bankAccountDto);
        bankAccount.setWallet(wallet);
        bankAccount.setBank(bank);
        return toLinkedBankAccountResponse(linkedBankAccountMapper.toDto(linkedBankAccountRepository.save(bankAccount)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LinkedBankAccountResponse> getLinkedBankAccounts(UUID userId) {
        return linkedBankAccountRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(linkedBankAccountMapper::toDto)
                .map(this::toLinkedBankAccountResponse)
                .toList();
    }

    @Override
    @Transactional
    public WalletTransactionResponse topUpFromBank(UUID userId, WalletBankTransferRequest request) {
        return executeTransfer(
                userId,
                request,
                Constant.WALLET_TRANSACTION_TYPE_BANK_TO_WALLET
        );
    }

    @Override
    @Transactional
    public WalletTransactionResponse withdrawToBank(UUID userId, WalletBankTransferRequest request) {
        return executeTransfer(
                userId,
                request,
                Constant.WALLET_TRANSACTION_TYPE_WALLET_TO_BANK
        );
    }

    @Override
    @Transactional
    public WalletTransactionResponse transferToWallet(UUID userId, WalletTransferRequest request) {
        String idempotencyKey = request.getIdempotencyKey().trim();
        var existingTransaction = walletTransactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existingTransaction.isPresent()) {
            WalletTransactionEntity transaction = existingTransaction.get();
            if (!transaction.getUser().getId().equals(userId)) {
                throw new AuthException(Constant.MESSAGE_DUPLICATED_TRANSACTION, Constant.ERROR_DUPLICATED_TRANSACTION);
            }
            return toWalletTransactionResponse(walletTransactionMapper.toDto(transaction));
        }

        UserEntity recipient = userRepository.findByPhoneNumber(request.getRecipientPhoneNumber().trim())
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_RECIPIENT_NOT_FOUND, Constant.ERROR_RECIPIENT_NOT_FOUND));
        if (recipient.getId().equals(userId)) {
            throw new AuthException(Constant.MESSAGE_SELF_WALLET_TRANSFER, Constant.ERROR_SELF_WALLET_TRANSFER);
        }

        List<UUID> orderedUserIds = List.of(userId, recipient.getId()).stream()
                .sorted(Comparator.naturalOrder())
                .toList();
        WalletEntity firstWallet = walletRepository.findByUserIdForUpdate(orderedUserIds.get(0))
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_WALLET_NOT_FOUND, Constant.ERROR_WALLET_NOT_FOUND));
        WalletEntity secondWallet = walletRepository.findByUserIdForUpdate(orderedUserIds.get(1))
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_WALLET_NOT_FOUND, Constant.ERROR_WALLET_NOT_FOUND));

        WalletEntity senderWallet = firstWallet.getUser().getId().equals(userId) ? firstWallet : secondWallet;
        WalletEntity recipientWallet = firstWallet.getUser().getId().equals(recipient.getId()) ? firstWallet : secondWallet;
        BigDecimal amount = request.getAmount();
        ensureEnoughBalance(
                senderWallet.getAvailableBalance(),
                amount,
                Constant.MESSAGE_INSUFFICIENT_WALLET_BALANCE,
                Constant.ERROR_INSUFFICIENT_WALLET_BALANCE
        );

        senderWallet.setAvailableBalance(senderWallet.getAvailableBalance().subtract(amount));
        recipientWallet.setAvailableBalance(recipientWallet.getAvailableBalance().add(amount));

        String referenceCode = UUID.randomUUID().toString();
        WalletTransactionEntity debitTransaction = buildWalletTransferTransaction(
                senderWallet,
                recipientWallet,
                userId,
                recipient.getId(),
                Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_OUT,
                amount,
                idempotencyKey,
                referenceCode
        );
        WalletTransactionEntity creditTransaction = buildWalletTransferTransaction(
                recipientWallet,
                senderWallet,
                recipient.getId(),
                userId,
                Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_IN,
                amount,
                idempotencyKey + Constant.WALLET_TRANSFER_CREDIT_IDEMPOTENCY_SUFFIX,
                referenceCode
        );

        walletTransactionRepository.save(creditTransaction);
        return toWalletTransactionResponse(walletTransactionMapper.toDto(walletTransactionRepository.save(debitTransaction)));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WalletTransactionResponse> getTransactions(UUID userId, Pageable pageable) {
        return PageResponse.of(
                walletTransactionRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable),
                entity -> toWalletTransactionResponse(walletTransactionMapper.toDto(entity))
        );
    }

    private WalletTransactionResponse executeTransfer(
            UUID userId,
            WalletBankTransferRequest request,
            String transactionType
    ) {
        String idempotencyKey = request.getIdempotencyKey().trim();
        var existingTransaction = walletTransactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existingTransaction.isPresent()) {
            WalletTransactionEntity transaction = existingTransaction.get();
            if (!transaction.getUser().getId().equals(userId)) {
                throw new AuthException(Constant.MESSAGE_DUPLICATED_TRANSACTION, Constant.ERROR_DUPLICATED_TRANSACTION);
            }
            return toWalletTransactionResponse(walletTransactionMapper.toDto(transaction));
        }

        WalletEntity wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_WALLET_NOT_FOUND, Constant.ERROR_WALLET_NOT_FOUND));
        LinkedBankAccountEntity bankAccount = linkedBankAccountRepository.findByIdAndUserIdForUpdate(request.getLinkedBankAccountId(), userId)
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_BANK_ACCOUNT_NOT_FOUND, Constant.ERROR_BANK_ACCOUNT_NOT_FOUND));

        BigDecimal amount = request.getAmount();
        if (Constant.WALLET_TRANSACTION_TYPE_BANK_TO_WALLET.equals(transactionType)) {
            wallet.setAvailableBalance(wallet.getAvailableBalance().add(amount));
        } else {
            ensureEnoughBalance(
                    wallet.getAvailableBalance(),
                    amount,
                    Constant.MESSAGE_INSUFFICIENT_WALLET_BALANCE,
                    Constant.ERROR_INSUFFICIENT_WALLET_BALANCE
            );
            wallet.setAvailableBalance(wallet.getAvailableBalance().subtract(amount));
        }

        WalletTransactionDTO transactionDto = WalletTransactionDTO.builder()
                .walletId(wallet.getId())
                .userId(userId)
                .linkedBankAccountId(bankAccount.getId())
                .type(transactionType)
                .amount(amount)
                .status(Constant.STATUS_SUCCESS)
                .idempotencyKey(idempotencyKey)
                .referenceCode(UUID.randomUUID().toString())
                .build();
        WalletTransactionEntity transaction = walletTransactionMapper.toEntity(transactionDto);
        transaction.setWallet(wallet);
        transaction.setUser(userRepository.getReferenceById(userId));
        transaction.setLinkedBankAccount(bankAccount);
        return toWalletTransactionResponse(walletTransactionMapper.toDto(walletTransactionRepository.save(transaction)));
    }

    private WalletEntity findWallet(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_WALLET_NOT_FOUND, Constant.ERROR_WALLET_NOT_FOUND));
    }

    private void ensureEnoughBalance(BigDecimal balance, BigDecimal amount, String message, String error) {
        if (balance.compareTo(amount) < 0) {
            throw new AuthException(message, error);
        }
    }

    private WalletTransactionEntity buildWalletTransferTransaction(
            WalletEntity wallet,
            WalletEntity counterpartyWallet,
            UUID userId,
            UUID counterpartyUserId,
            String type,
            BigDecimal amount,
            String idempotencyKey,
            String referenceCode
    ) {
        WalletTransactionDTO transactionDto = WalletTransactionDTO.builder()
                .walletId(wallet.getId())
                .userId(userId)
                .counterpartyWalletId(counterpartyWallet.getId())
                .counterpartyUserId(counterpartyUserId)
                .type(type)
                .amount(amount)
                .status(Constant.STATUS_SUCCESS)
                .idempotencyKey(idempotencyKey)
                .referenceCode(referenceCode)
                .build();
        WalletTransactionEntity transaction = walletTransactionMapper.toEntity(transactionDto);
        transaction.setWallet(wallet);
        transaction.setUser(userRepository.getReferenceById(userId));
        transaction.setCounterpartyWallet(counterpartyWallet);
        transaction.setCounterpartyUser(userRepository.getReferenceById(counterpartyUserId));
        return transaction;
    }

    private WalletResponse toWalletResponse(com.university.fintech.domain.dto.WalletDTO wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .availableBalance(wallet.getAvailableBalance())
                .holdBalance(wallet.getHoldBalance())
                .status(wallet.getStatus())
                .build();
    }

    private LinkedBankAccountResponse toLinkedBankAccountResponse(LinkedBankAccountDTO bankAccount) {
        return LinkedBankAccountResponse.builder()
                .id(bankAccount.getId())
                .walletId(bankAccount.getWalletId())
                .bankId(bankAccount.getBankId())
                .bankCode(bankAccount.getBankCode())
                .bankName(bankAccount.getBankName())
                .accountNumber(bankAccount.getAccountNumber())
                .accountHolderName(bankAccount.getAccountHolderName())
                .status(bankAccount.getStatus())
                .build();
    }

    private WalletTransactionResponse toWalletTransactionResponse(WalletTransactionDTO transaction) {
        return WalletTransactionResponse.builder()
                .id(transaction.getId())
                .walletId(transaction.getWalletId())
                .linkedBankAccountId(transaction.getLinkedBankAccountId())
                .counterpartyWalletId(transaction.getCounterpartyWalletId())
                .counterpartyUserId(transaction.getCounterpartyUserId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .idempotencyKey(transaction.getIdempotencyKey())
                .referenceCode(transaction.getReferenceCode())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

}
