package com.university.fintech.service.wallet;

import com.university.fintech.domain.dto.LinkedBankAccountDTO;
import com.university.fintech.domain.dto.WalletTransactionDTO;
import com.university.fintech.domain.entity.BankEntity;
import com.university.fintech.domain.entity.LinkedBankAccountEntity;
import com.university.fintech.domain.entity.UserEntity;
import com.university.fintech.domain.entity.WalletEntity;
import com.university.fintech.domain.entity.WalletLedgerEntryEntity;
import com.university.fintech.domain.entity.WalletTransactionEntity;
import com.university.fintech.domain.mapper.LinkedBankAccountMapper;
import com.university.fintech.domain.mapper.WalletMapper;
import com.university.fintech.domain.mapper.WalletTransactionMapper;
import com.university.fintech.domain.port.primary.WalletBankService;
import com.university.fintech.domain.port.secondary.WalletTransactionProcessor;
import com.university.fintech.domain.port.secondary.model.WalletProcessingRequest;
import com.university.fintech.domain.port.secondary.model.WalletProcessingResult;
import com.university.fintech.exception.domain.AuthException;
import com.university.fintech.infrastructure.primary.restful.request.wallet.LinkBankAccountRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletBankTransferRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletRecipientLookupRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletTransferRequest;
import com.university.fintech.infrastructure.primary.restful.response.wallet.BankResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.LinkedBankAccountResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletRecipientLookupResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletTransactionDetailResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletTransactionHistoryResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletTransactionResponse;
import com.university.fintech.infrastructure.secondary.jpa.repository.BankRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.LinkedBankAccountRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.UserRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.WalletLedgerEntryRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.WalletRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.WalletTransactionRepository;
import com.university.fintech.shared.constant.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
    private final WalletLedgerEntryRepository walletLedgerEntryRepository;
    private final WalletTransactionProcessor walletTransactionProcessor;
    private final WalletMapper walletMapper;
    private final LinkedBankAccountMapper linkedBankAccountMapper;
    private final WalletTransactionMapper walletTransactionMapper;

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWallet(UUID userId) {
        return toWalletResponse(walletMapper.toDto(findWallet(userId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankResponse> getBanks() {
        return bankRepository.findAll(Sort.by(Sort.Direction.ASC, "bankName", "bankCode", "accountNumber")).stream()
                .map(this::toBankResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getTopUpMethods() {
        return bankRepository.findDistinctBankCodesByStatusOrderByBankCode(Constant.STATUS_ACTIVE);
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
            ensureSameWalletTransferPayload(transaction, request);
            return toWalletTransactionResponse(walletTransactionMapper.toDto(transaction));
        }

        UserEntity recipient = findTransferRecipient(userId, request.getRecipientPhoneNumber());

        List<UUID> orderedUserIds = List.of(userId, recipient.getId()).stream()
                .sorted(Comparator.naturalOrder())
                .toList();
        WalletEntity firstWallet = walletRepository.findByUserIdForUpdate(orderedUserIds.get(0))
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_WALLET_NOT_FOUND, Constant.ERROR_WALLET_NOT_FOUND));
        WalletEntity secondWallet = walletRepository.findByUserIdForUpdate(orderedUserIds.get(1))
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_WALLET_NOT_FOUND, Constant.ERROR_WALLET_NOT_FOUND));

        WalletEntity senderWallet = firstWallet.getUser().getId().equals(userId) ? firstWallet : secondWallet;
        WalletEntity recipientWallet = firstWallet.getUser().getId().equals(recipient.getId()) ? firstWallet : secondWallet;
        ensureActiveUser(senderWallet.getUser());
        ensureActiveUser(recipientWallet.getUser());
        ensureActiveWallet(senderWallet);
        ensureActiveWallet(recipientWallet);
        BigDecimal amount = request.getAmount();
        ensureEnoughBalance(
                senderWallet.getAvailableBalance(),
                amount,
                Constant.MESSAGE_INSUFFICIENT_WALLET_BALANCE,
                Constant.ERROR_INSUFFICIENT_WALLET_BALANCE
        );
        ensureWalletTransactionLimit(userId, amount);

        String referenceCode = UUID.randomUUID().toString();
        WalletTransactionEntity debitTransaction = buildWalletTransferTransaction(
                senderWallet,
                recipientWallet,
                userId,
                Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_OUT,
                amount,
                idempotencyKey,
                referenceCode,
                Constant.STATUS_PENDING
        );
        WalletTransactionEntity creditTransaction = buildWalletTransferTransaction(
                recipientWallet,
                senderWallet,
                recipient.getId(),
                Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_IN,
                amount,
                idempotencyKey + Constant.WALLET_TRANSFER_CREDIT_IDEMPOTENCY_SUFFIX,
                referenceCode,
                Constant.STATUS_PENDING
        );

        WalletTransactionEntity savedDebitTransaction = walletTransactionRepository.save(debitTransaction);
        WalletTransactionEntity savedCreditTransaction = walletTransactionRepository.save(creditTransaction);
        WalletProcessingResult processingResult = processTransaction(
                userId,
                Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_OUT,
                amount,
                idempotencyKey,
                referenceCode
        );
        if (Constant.STATUS_PENDING.equals(processingResult.getStatus())) {
            return toWalletTransactionResponse(walletTransactionMapper.toDto(savedDebitTransaction));
        }
        if (!Constant.STATUS_SUCCESS.equals(processingResult.getStatus())) {
            savedDebitTransaction.setStatus(Constant.STATUS_FAILED);
            savedCreditTransaction.setStatus(Constant.STATUS_FAILED);
            walletTransactionRepository.save(savedCreditTransaction);
            return toWalletTransactionResponse(walletTransactionMapper.toDto(walletTransactionRepository.save(savedDebitTransaction)));
        }

        senderWallet.setAvailableBalance(senderWallet.getAvailableBalance().subtract(amount));
        recipientWallet.setAvailableBalance(recipientWallet.getAvailableBalance().add(amount));
        savedDebitTransaction.setStatus(Constant.STATUS_SUCCESS);
        savedCreditTransaction.setStatus(Constant.STATUS_SUCCESS);
        createLedgerEntry(
                savedDebitTransaction,
                senderWallet,
                Constant.LEDGER_ENTRY_TYPE_DEBIT,
                amount,
                senderWallet.getAvailableBalance(),
                buildWalletTransferDescription(Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_OUT, recipientWallet.getUser())
        );
        createLedgerEntry(
                savedCreditTransaction,
                recipientWallet,
                Constant.LEDGER_ENTRY_TYPE_CREDIT,
                amount,
                recipientWallet.getAvailableBalance(),
                buildWalletTransferDescription(Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_IN, senderWallet.getUser())
        );
        walletTransactionRepository.save(savedCreditTransaction);
        return toWalletTransactionResponse(walletTransactionMapper.toDto(walletTransactionRepository.save(savedDebitTransaction)));
    }

    @Override
    @Transactional(readOnly = true)
    public WalletTransactionDetailResponse getTransactionDetail(UUID userId, UUID transactionId) {
        WalletTransactionEntity transaction = walletTransactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_TRANSACTION_NOT_FOUND, Constant.ERROR_TRANSACTION_NOT_FOUND));
        return toWalletTransactionDetailResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletRecipientLookupResponse lookupTransferRecipient(UUID userId, WalletRecipientLookupRequest request) {
        UserEntity recipient = findTransferRecipient(userId, request.getRecipientPhoneNumber());
        return WalletRecipientLookupResponse.builder()
                .fullName(recipient.getFullName())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WalletTransactionHistoryResponse getTransactions(
            UUID userId,
            String type,
            String status,
            LocalDate updatedAtFrom,
            LocalDate updatedAtTo,
            Pageable pageable
    ) {
        ZoneId zoneId = ZoneId.systemDefault();
        OffsetDateTime updatedAtFromTime = updatedAtFrom == null ? null : updatedAtFrom.atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime updatedAtToTime = updatedAtTo == null ? null : updatedAtTo.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime();
        Pageable sortedPageable = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "updatedAt"));
        return WalletTransactionHistoryResponse.fromPage(
                walletTransactionRepository.findAll(
                        walletTransactionHistorySpecification(
                        userId,
                        trimToUpperOrNull(type),
                        trimToUpperOrNull(status),
                        updatedAtFromTime,
                        updatedAtToTime
                        ),
                        sortedPageable
                ),
                entity -> toWalletTransactionHistoryItem(walletTransactionMapper.toDto(entity))
        );
    }

    private Specification<WalletTransactionEntity> walletTransactionHistorySpecification(
            UUID userId,
            String type,
            String status,
            OffsetDateTime updatedAtFrom,
            OffsetDateTime updatedAtTo
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (updatedAtFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("updatedAt"), updatedAtFrom));
            }
            if (updatedAtTo != null) {
                predicates.add(criteriaBuilder.lessThan(root.get("updatedAt"), updatedAtTo));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
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
            ensureSameBankTransferPayload(transaction, request, transactionType);
            return toWalletTransactionResponse(walletTransactionMapper.toDto(transaction));
        }

        WalletEntity wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_WALLET_NOT_FOUND, Constant.ERROR_WALLET_NOT_FOUND));
        LinkedBankAccountEntity bankAccount = linkedBankAccountRepository.findByIdAndUserIdForUpdate(request.getLinkedBankAccountId(), userId)
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_BANK_ACCOUNT_NOT_FOUND, Constant.ERROR_BANK_ACCOUNT_NOT_FOUND));
        BankEntity bank = bankRepository.findByIdForUpdate(bankAccount.getBank().getId())
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_BANK_NOT_FOUND, Constant.ERROR_BANK_NOT_FOUND));

        ensureActiveUser(wallet.getUser());
        ensureActiveWallet(wallet);
        ensureActiveBankAccount(bankAccount);
        ensureActiveBank(bank);

        BigDecimal amount = request.getAmount();
        ensureWalletTransactionLimit(userId, amount);
        String referenceCode = UUID.randomUUID().toString();
        WalletTransactionDTO transactionDto = WalletTransactionDTO.builder()
                .walletId(wallet.getId())
                .userId(userId)
                .linkedBankAccountId(bankAccount.getId())
                .type(transactionType)
                .amount(amount)
                .status(Constant.STATUS_PENDING)
                .idempotencyKey(idempotencyKey)
                .referenceCode(referenceCode)
                .description(buildBankTransferDescription(transactionType, bankAccount))
                .build();
        WalletTransactionEntity transaction = walletTransactionMapper.toEntity(transactionDto);
        transaction.setWallet(wallet);
        transaction.setUser(userRepository.getReferenceById(userId));
        transaction.setLinkedBankAccount(bankAccount);
        WalletTransactionEntity savedTransaction = walletTransactionRepository.save(transaction);

        WalletProcessingResult processingResult = processTransaction(
                userId,
                transactionType,
                amount,
                idempotencyKey,
                referenceCode
        );
        if (Constant.STATUS_PENDING.equals(processingResult.getStatus())) {
            return toWalletTransactionResponse(walletTransactionMapper.toDto(savedTransaction));
        }
        if (!Constant.STATUS_SUCCESS.equals(processingResult.getStatus())) {
            savedTransaction.setStatus(Constant.STATUS_FAILED);
            return toWalletTransactionResponse(walletTransactionMapper.toDto(walletTransactionRepository.save(savedTransaction)));
        }

        if (Constant.WALLET_TRANSACTION_TYPE_BANK_TO_WALLET.equals(transactionType)) {
            ensureEnoughBalance(
                    bank.getBalance(),
                    amount,
                    Constant.MESSAGE_INSUFFICIENT_BANK_BALANCE,
                    Constant.ERROR_INSUFFICIENT_BANK_BALANCE
            );
            bank.setBalance(bank.getBalance().subtract(amount));
            wallet.setAvailableBalance(wallet.getAvailableBalance().add(amount));
            createLedgerEntry(
                    savedTransaction,
                    wallet,
                    Constant.LEDGER_ENTRY_TYPE_CREDIT,
                    amount,
                    wallet.getAvailableBalance(),
                    buildBankTransferDescription(transactionType, bankAccount)
            );
        } else {
            ensureEnoughBalance(
                    wallet.getAvailableBalance(),
                    amount,
                    Constant.MESSAGE_INSUFFICIENT_WALLET_BALANCE,
                    Constant.ERROR_INSUFFICIENT_WALLET_BALANCE
            );
            wallet.setAvailableBalance(wallet.getAvailableBalance().subtract(amount));
            bank.setBalance(bank.getBalance().add(amount));
            createLedgerEntry(
                    savedTransaction,
                    wallet,
                    Constant.LEDGER_ENTRY_TYPE_DEBIT,
                    amount,
                    wallet.getAvailableBalance(),
                    buildBankTransferDescription(transactionType, bankAccount)
            );
        }

        savedTransaction.setStatus(Constant.STATUS_SUCCESS);
        return toWalletTransactionResponse(walletTransactionMapper.toDto(walletTransactionRepository.save(savedTransaction)));
    }

    private WalletEntity findWallet(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_WALLET_NOT_FOUND, Constant.ERROR_WALLET_NOT_FOUND));
    }

    private UserEntity findTransferRecipient(UUID userId, String recipientPhoneNumber) {
        UserEntity recipient = userRepository.findByPhoneNumber(recipientPhoneNumber.trim())
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_RECIPIENT_NOT_FOUND, Constant.ERROR_RECIPIENT_NOT_FOUND));
        if (recipient.getId().equals(userId)) {
            throw new AuthException(Constant.MESSAGE_SELF_WALLET_TRANSFER, Constant.ERROR_SELF_WALLET_TRANSFER);
        }
        return recipient;
    }

    private void ensureSameWalletTransferPayload(WalletTransactionEntity transaction, WalletTransferRequest request) {
        String recipientPhoneNumber = request.getRecipientPhoneNumber().trim();
        boolean sameTransferType = Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_OUT.equals(transaction.getType());
        boolean sameAmount = transaction.getAmount().compareTo(request.getAmount()) == 0;
        boolean sameRecipient = transaction.getCounterpartyWallet() != null
                && transaction.getCounterpartyWallet().getUser() != null
                && recipientPhoneNumber.equals(transaction.getCounterpartyWallet().getUser().getPhoneNumber());
        if (!sameTransferType || !sameAmount || !sameRecipient) {
            throw new AuthException(
                    Constant.MESSAGE_IDEMPOTENCY_PAYLOAD_MISMATCH,
                    Constant.ERROR_IDEMPOTENCY_PAYLOAD_MISMATCH
            );
        }
    }

    private void ensureSameBankTransferPayload(
            WalletTransactionEntity transaction,
            WalletBankTransferRequest request,
            String transactionType
    ) {
        boolean sameTransferType = transactionType.equals(transaction.getType());
        boolean sameAmount = transaction.getAmount().compareTo(request.getAmount()) == 0;
        boolean sameLinkedBankAccount = transaction.getLinkedBankAccount() != null
                && request.getLinkedBankAccountId().equals(transaction.getLinkedBankAccount().getId());
        if (!sameTransferType || !sameAmount || !sameLinkedBankAccount) {
            throw new AuthException(
                    Constant.MESSAGE_IDEMPOTENCY_PAYLOAD_MISMATCH,
                    Constant.ERROR_IDEMPOTENCY_PAYLOAD_MISMATCH
            );
        }
    }

    private void ensureActiveUser(UserEntity user) {
        if (user == null || !Constant.STATUS_ACTIVE.equalsIgnoreCase(user.getStatus())) {
            throw new AuthException(Constant.MESSAGE_USER_NOT_ACTIVE, Constant.ERROR_USER_NOT_ACTIVE);
        }
    }

    private void ensureActiveWallet(WalletEntity wallet) {
        if (wallet == null || !Constant.STATUS_ACTIVE.equalsIgnoreCase(wallet.getStatus())) {
            throw new AuthException(Constant.MESSAGE_WALLET_NOT_ACTIVE, Constant.ERROR_WALLET_NOT_ACTIVE);
        }
    }

    private void ensureActiveBankAccount(LinkedBankAccountEntity bankAccount) {
        if (bankAccount == null || !Constant.STATUS_ACTIVE.equalsIgnoreCase(bankAccount.getStatus())) {
            throw new AuthException(Constant.MESSAGE_BANK_ACCOUNT_NOT_ACTIVE, Constant.ERROR_BANK_ACCOUNT_NOT_ACTIVE);
        }
    }

    private void ensureActiveBank(BankEntity bank) {
        if (bank == null || !Constant.STATUS_ACTIVE.equalsIgnoreCase(bank.getStatus())) {
            throw new AuthException(Constant.MESSAGE_BANK_NOT_ACTIVE, Constant.ERROR_BANK_NOT_ACTIVE);
        }
    }

    private void ensureEnoughBalance(BigDecimal balance, BigDecimal amount, String message, String error) {
        if (balance == null || balance.compareTo(amount) < 0) {
            throw new AuthException(message, error);
        }
    }

    private void ensureWalletTransactionLimit(UUID userId, BigDecimal amount) {
        BigDecimal dailyLimit = new BigDecimal(Constant.WALLET_TRANSACTION_DAILY_LIMIT);
        BigDecimal monthlyLimit = new BigDecimal(Constant.WALLET_TRANSACTION_MONTHLY_LIMIT);
        ZoneId zoneId = ZoneId.systemDefault();
        OffsetDateTime now = OffsetDateTime.now(zoneId);
        OffsetDateTime startOfDay = now.toLocalDate().atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime startOfNextDay = startOfDay.plusDays(1);
        OffsetDateTime startOfMonth = now.toLocalDate()
                .withDayOfMonth(1)
                .atStartOfDay(zoneId)
                .toOffsetDateTime();
        OffsetDateTime startOfNextMonth = startOfMonth.plusMonths(1);
        List<String> limitedTypes = List.of(
                Constant.WALLET_TRANSACTION_TYPE_BANK_TO_WALLET,
                Constant.WALLET_TRANSACTION_TYPE_WALLET_TO_BANK,
                Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_OUT
        );
        List<String> countedStatuses = List.of(Constant.STATUS_PENDING, Constant.STATUS_SUCCESS);

        BigDecimal dailyTotal = walletTransactionRepository.sumAmountByUserIdAndTypeInAndStatusInAndCreatedAtBetween(
                userId,
                limitedTypes,
                countedStatuses,
                startOfDay,
                startOfNextDay
        );
        if (zeroIfNull(dailyTotal).add(amount).compareTo(dailyLimit) > 0) {
            throw new AuthException(
                    Constant.MESSAGE_WALLET_TRANSACTION_DAILY_LIMIT_EXCEEDED,
                    Constant.ERROR_WALLET_TRANSACTION_AMOUNT_LIMIT_EXCEEDED
            );
        }

        BigDecimal monthlyTotal = walletTransactionRepository.sumAmountByUserIdAndTypeInAndStatusInAndCreatedAtBetween(
                userId,
                limitedTypes,
                countedStatuses,
                startOfMonth,
                startOfNextMonth
        );
        if (zeroIfNull(monthlyTotal).add(amount).compareTo(monthlyLimit) > 0) {
            throw new AuthException(
                    Constant.MESSAGE_WALLET_TRANSACTION_MONTHLY_LIMIT_EXCEEDED,
                    Constant.ERROR_WALLET_TRANSACTION_AMOUNT_LIMIT_EXCEEDED
            );
        }
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private WalletTransactionEntity buildWalletTransferTransaction(
            WalletEntity wallet,
            WalletEntity counterpartyWallet,
            UUID userId,
            String type,
            BigDecimal amount,
            String idempotencyKey,
            String referenceCode,
            String status
    ) {
        WalletTransactionDTO transactionDto = WalletTransactionDTO.builder()
                .walletId(wallet.getId())
                .userId(userId)
                .counterpartyWalletId(counterpartyWallet.getId())
                .type(type)
                .amount(amount)
                .status(status)
                .idempotencyKey(idempotencyKey)
                .referenceCode(referenceCode)
                .description(buildWalletTransferDescription(type, counterpartyWallet.getUser()))
                .build();
        WalletTransactionEntity transaction = walletTransactionMapper.toEntity(transactionDto);
        transaction.setWallet(wallet);
        transaction.setUser(userRepository.getReferenceById(userId));
        transaction.setCounterpartyWallet(counterpartyWallet);
        return transaction;
    }

    private WalletProcessingResult processTransaction(
            UUID userId,
            String transactionType,
            BigDecimal amount,
            String idempotencyKey,
            String referenceCode
    ) {
        WalletProcessingResult result = walletTransactionProcessor.process(
                WalletProcessingRequest.builder()
                        .userId(userId)
                        .transactionType(transactionType)
                        .amount(amount)
                        .idempotencyKey(idempotencyKey)
                        .referenceCode(referenceCode)
                        .build()
        );
        if (result == null || result.getStatus() == null) {
            return WalletProcessingResult.builder()
                    .status(Constant.STATUS_FAILED)
                    .message("External processor returned no result.")
                    .build();
        }
        return result;
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

    private WalletResponse toWalletResponse(com.university.fintech.domain.dto.WalletDTO wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .availableBalance(wallet.getAvailableBalance())
                .holdBalance(wallet.getHoldBalance())
                .status(wallet.getStatus())
                .build();
    }

    private BankResponse toBankResponse(BankEntity bank) {
        return BankResponse.builder()
                .id(bank.getId())
                .bankCode(bank.getBankCode())
                .bankName(bank.getBankName())
                .accountNumber(bank.getAccountNumber())
                .accountHolderName(bank.getAccountHolderName())
                .status(bank.getStatus())
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
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .idempotencyKey(transaction.getIdempotencyKey())
                .referenceCode(transaction.getReferenceCode())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .description(transaction.getDescription())
                .build();
    }

    private WalletTransactionDetailResponse toWalletTransactionDetailResponse(WalletTransactionEntity transaction) {
        UserEntity user = transaction.getUser();
        UserEntity counterpartyUser = transaction.getCounterpartyWallet() == null
                ? null
                : transaction.getCounterpartyWallet().getUser();
        return WalletTransactionDetailResponse.builder()
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .updatedAt(transaction.getUpdatedAt())
                .counterpartyFullName(counterpartyUser == null ? null : counterpartyUser.getFullName())
                .counterpartyPhoneNumber(counterpartyUser == null ? null : counterpartyUser.getPhoneNumber())
                .build();
    }

    private WalletTransactionHistoryResponse.Item toWalletTransactionHistoryItem(WalletTransactionDTO transaction) {
        return WalletTransactionHistoryResponse.Item.builder()
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .updatedAt(transaction.getUpdatedAt())
                .description(transaction.getDescription())
                .build();
    }

    private String buildBankTransferDescription(String transactionType, LinkedBankAccountEntity bankAccount) {
        String bankCode = bankAccount.getBank().getBankCode();
        String accountNumber = bankAccount.getBank().getAccountNumber();
        if (Constant.WALLET_TRANSACTION_TYPE_BANK_TO_WALLET.equals(transactionType)) {
            return "Nap tien tu tai khoan ngan hang " + bankCode + " " + accountNumber;
        }
        return "Rut tien ve tai khoan ngan hang " + bankCode + " " + accountNumber;
    }

    private String buildWalletTransferDescription(String transactionType, UserEntity counterpartyUser) {
        if (Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_OUT.equals(transactionType)) {
            return "Chuyen tien den " + counterpartyUser.getPhoneNumber();
        }
        return "Nhan tien tu " + counterpartyUser.getPhoneNumber();
    }

    private String trimToUpperOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim().toUpperCase();
    }

}
