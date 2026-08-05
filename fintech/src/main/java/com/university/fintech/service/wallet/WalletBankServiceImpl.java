package com.university.fintech.service.wallet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
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
import com.university.fintech.domain.port.primary.AuthMailService;
import com.university.fintech.domain.port.primary.TransactionOtpService;
import com.university.fintech.domain.port.primary.WalletNotificationMailService;
import com.university.fintech.domain.port.primary.WalletBankService;
import com.university.fintech.domain.port.primary.WalletPinService;
import com.university.fintech.domain.port.secondary.WalletTransactionProcessor;
import com.university.fintech.domain.port.secondary.model.WalletProcessingRequest;
import com.university.fintech.domain.port.secondary.model.WalletProcessingResult;
import com.university.fintech.exception.domain.AuthException;
import com.university.fintech.infrastructure.primary.restful.request.wallet.LinkBankAccountRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletBankTransferRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletQrDecodeRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletRecipientLookupRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletTransferRequest;
import com.university.fintech.infrastructure.primary.restful.response.wallet.BankResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.LinkedBankAccountResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletQrDecodeResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletQrResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletRecipientLookupResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletRetryContextResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletTransactionDetailResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletTransactionHistoryResponse;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletTransactionResponse;
import com.university.fintech.infrastructure.secondary.jpa.repository.BankRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.LinkedBankAccountRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.UserRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.WalletLedgerEntryRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.WalletRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.WalletTransactionRepository;
import com.university.fintech.infrastructure.primary.restful.security.ClientSecurityContext;
import com.university.fintech.shared.constant.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.OffsetDateTime;

import jakarta.persistence.criteria.Predicate;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletBankServiceImpl implements WalletBankService {

    private final UserRepository userRepository;
    private final BankRepository bankRepository;
    private final WalletRepository walletRepository;
    private final LinkedBankAccountRepository linkedBankAccountRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletLedgerEntryRepository walletLedgerEntryRepository;
    private final WalletTransactionProcessor walletTransactionProcessor;
    private final WalletNotificationMailService walletNotificationMailService;
    private final WalletMapper walletMapper;
    private final LinkedBankAccountMapper linkedBankAccountMapper;
    private final WalletTransactionMapper walletTransactionMapper;
    private final WalletPinService walletPinService;
    private final TransactionOtpService transactionOtpService;
    private final AuthMailService authMailService;
    private final ObjectMapper objectMapper;

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

        if (linkedBankAccountRepository.existsByBankIdAndStatus(bank.getId(), Constant.STATUS_ACTIVE)) {
            throw new AuthException(
                    Constant.MESSAGE_BANK_ACCOUNT_ALREADY_LINKED,
                    Constant.ERROR_BANK_ACCOUNT_ALREADY_LINKED
            );
        }

        // Nếu đã từng link rồi unlink → restore record cũ, tránh vi phạm unique constraint DB
        Optional<LinkedBankAccountEntity> existing = linkedBankAccountRepository
                .findByBankIdAndWalletUserId(bank.getId(), userId);
        if (existing.isPresent()) {
            LinkedBankAccountEntity bankAccount = existing.get();
            bankAccount.setStatus(Constant.STATUS_ACTIVE);
            return toLinkedBankAccountResponse(linkedBankAccountMapper.toDto(linkedBankAccountRepository.save(bankAccount)));
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
        return linkedBankAccountRepository.findAllByUserIdAndStatusOrderByCreatedAtDesc(userId, Constant.STATUS_ACTIVE).stream()
                .map(linkedBankAccountMapper::toDto)
                .map(this::toLinkedBankAccountResponse)
                .toList();
    }

    @Override
    @Transactional
    public void unlinkBankAccount(UUID userId, UUID bankAccountId) {
        LinkedBankAccountEntity bankAccount = linkedBankAccountRepository.findByIdAndUserId(bankAccountId, userId)
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_BANK_ACCOUNT_NOT_FOUND, Constant.ERROR_BANK_ACCOUNT_NOT_FOUND));
        if (!Constant.STATUS_ACTIVE.equalsIgnoreCase(bankAccount.getStatus())) {
            throw new AuthException(Constant.MESSAGE_BANK_ACCOUNT_NOT_FOUND, Constant.ERROR_BANK_ACCOUNT_NOT_FOUND);
        }
        boolean hasPendingTransaction = walletTransactionRepository.existsByLinkedBankAccountIdAndStatus(
                bankAccount.getId(), Constant.STATUS_PENDING
        );
        if (hasPendingTransaction) {
            throw new AuthException(
                    Constant.MESSAGE_BANK_ACCOUNT_HAS_PENDING_TRANSACTION,
                    Constant.ERROR_BANK_ACCOUNT_HAS_PENDING_TRANSACTION
            );
        }
        bankAccount.setStatus(Constant.STATUS_UNLINKED);
        linkedBankAccountRepository.save(bankAccount);
    }

    @Override
    @Transactional
    public WalletTransactionResponse topUpFromBank(UUID userId, WalletBankTransferRequest request, ClientSecurityContext clientContext) {
        verifyTransactionAuth(userId, request.getPin(), request.getOtpCode());
        try {
            return executeTransfer(
                    userId,
                    request,
                    Constant.WALLET_TRANSACTION_TYPE_BANK_TO_WALLET,
                    clientContext
            );
        } catch (RuntimeException e) {
            notifyTransactionError(userId, Constant.WALLET_TRANSACTION_TYPE_BANK_TO_WALLET, request.getAmount(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public WalletTransactionResponse withdrawToBank(UUID userId, WalletBankTransferRequest request, ClientSecurityContext clientContext) {
        verifyTransactionAuth(userId, request.getPin(), request.getOtpCode());
        try {
            return executeTransfer(
                    userId,
                    request,
                    Constant.WALLET_TRANSACTION_TYPE_WALLET_TO_BANK,
                    clientContext
            );
        } catch (RuntimeException e) {
            notifyTransactionError(userId, Constant.WALLET_TRANSACTION_TYPE_WALLET_TO_BANK, request.getAmount(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public WalletTransactionResponse transferToWallet(UUID userId, WalletTransferRequest request, ClientSecurityContext clientContext) {
        verifyTransactionAuth(userId, request.getPin(), request.getOtpCode());
        try {
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

            // Hold sender balance ngay để tránh double-spend trong khi chờ PG xử lý
            senderWallet.setAvailableBalance(senderWallet.getAvailableBalance().subtract(amount));
            senderWallet.setHoldBalance(senderWallet.getHoldBalance().add(amount));

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
            walletTransactionRepository.save(creditTransaction);

            // Submit lên PaymentGateway qua Kafka (async)
            WalletProcessingRequest.WalletProcessingRequestBuilder requestBuilder = WalletProcessingRequest.builder()
                    .userId(userId)
                    .transactionType(Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_OUT)
                    .amount(amount)
                    .idempotencyKey(idempotencyKey)
                    .referenceCode(referenceCode)
                    .userEmail(senderWallet.getUser().getEmail())
                    .userFullName(senderWallet.getUser().getFullName())
                    .description(debitTransaction.getDescription())
                    .recipientFullName(recipientWallet.getUser().getFullName())
                    .recipientPhoneNumber(recipientWallet.getUser().getPhoneNumber());

            enrichAntiFraudData(
                    requestBuilder,
                    userId,
                    senderWallet.getAvailableBalance(),
                    senderWallet.getAvailableBalance().subtract(amount),
                    userId.toString(),
                    senderWallet.getId().toString(),
                    clientContext
            );

            walletTransactionProcessor.process(requestBuilder.build());

            // Trả PENDING ngay — email sẽ gửi sau khi nhận kết quả từ PaymentGateway (SUCCESS / FAILED)
            return toWalletTransactionResponse(walletTransactionMapper.toDto(savedDebitTransaction));
        } catch (RuntimeException e) {
            notifyTransactionError(
                    userId,
                    Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_OUT,
                    request.getAmount(),
                    e
            );
            throw e;
        }
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
    public WalletRetryContextResponse getRetryContext(UUID userId, UUID transactionId) {
        WalletTransactionEntity tx = walletTransactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new AuthException(
                        Constant.MESSAGE_TRANSACTION_NOT_FOUND,
                        Constant.ERROR_TRANSACTION_NOT_FOUND
                ));

        // Chỉ DEBIT leg (TRANSFER_OUT, WALLET_TO_BANK, BANK_TO_WALLET) mới có retry context.
        // TRANSFER_IN là credit leg nội bộ — client không tương tác trực tiếp.
        String type = tx.getType();
        if (Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_IN.equals(type)) {
            return WalletRetryContextResponse.builder()
                    .transactionId(tx.getId())
                    .type(type)
                    .status(tx.getStatus())
                    .retryable(false)
                    .amount(tx.getAmount())
                    .description(tx.getDescription())
                    .originalCreatedAt(tx.getCreatedAt())
                    .nonRetryableReason("Giao dich nhan tien noi bo, khong the thu lai truc tiep.")
                    .build();
        }

        String status = tx.getStatus();
        boolean retryable = Constant.STATUS_FAILED.equals(status) || Constant.STATUS_TIMED_OUT.equals(status);

        String nonRetryableReason = null;
        if (!retryable) {
            nonRetryableReason = switch (status) {
                case Constant.STATUS_PENDING  -> "Giao dich dang duoc xu ly. Vui long cho ket qua.";
                case Constant.STATUS_SUCCESS  -> "Giao dich da thanh cong. Khong the thu lai.";
                default -> "Giao dich o trang thai " + status + ", khong the thu lai.";
            };
        }

        WalletRetryContextResponse.WalletRetryContextResponseBuilder builder = WalletRetryContextResponse.builder()
                .transactionId(tx.getId())
                .type(type)
                .status(status)
                .retryable(retryable)
                .amount(tx.getAmount())
                .description(tx.getDescription())
                .originalCreatedAt(tx.getCreatedAt())
                .nonRetryableReason(nonRetryableReason);

        // Điền thông tin người nhận cho giao dịch chuyển ví
        if (Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_OUT.equals(type)
                && tx.getCounterpartyWallet() != null
                && tx.getCounterpartyWallet().getUser() != null) {
            UserEntity recipient = tx.getCounterpartyWallet().getUser();
            builder.recipientPhoneNumber(recipient.getPhoneNumber())
                   .recipientFullName(recipient.getFullName());
        }

        // Điền thông tin tài khoản ngân hàng cho giao dịch bank
        if ((Constant.WALLET_TRANSACTION_TYPE_WALLET_TO_BANK.equals(type)
                || Constant.WALLET_TRANSACTION_TYPE_BANK_TO_WALLET.equals(type))
                && tx.getLinkedBankAccount() != null) {
            builder.linkedBankAccountId(tx.getLinkedBankAccount().getId());
        }

        return builder.build();
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
            String transactionType,
            ClientSecurityContext clientContext
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
        BankEntity bank = bankRepository.findById(bankAccount.getBank().getId())
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_BANK_NOT_FOUND, Constant.ERROR_BANK_NOT_FOUND));

        ensureActiveUser(wallet.getUser());
        ensureActiveWallet(wallet);
        ensureActiveBankAccount(bankAccount);
        ensureActiveBank(bank);

        BigDecimal amount = request.getAmount();
        ensureWalletTransactionLimit(userId, amount);

        // Với WALLET_TO_BANK: hold balance ngay để tránh double-spend
        if (Constant.WALLET_TRANSACTION_TYPE_WALLET_TO_BANK.equals(transactionType)) {
            ensureEnoughBalance(
                    wallet.getAvailableBalance(),
                    amount,
                    Constant.MESSAGE_INSUFFICIENT_WALLET_BALANCE,
                    Constant.ERROR_INSUFFICIENT_WALLET_BALANCE
            );
            wallet.setAvailableBalance(wallet.getAvailableBalance().subtract(amount));
            wallet.setHoldBalance(wallet.getHoldBalance().add(amount));
        }

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

        // Submit lên PaymentGateway qua Kafka (async) - kết quả sẽ được xử lý bởi PaymentResultConsumer
        BigDecimal oldBal = wallet.getAvailableBalance();
        BigDecimal newBal = Constant.WALLET_TRANSACTION_TYPE_BANK_TO_WALLET.equals(transactionType)
                ? oldBal.add(amount)
                : oldBal.subtract(amount);

        String senderId = userId.toString();
        String senderAcc = Constant.WALLET_TRANSACTION_TYPE_BANK_TO_WALLET.equals(transactionType)
                ? bank.getAccountNumber()
                : bankAccount.getId().toString();

        WalletProcessingRequest.WalletProcessingRequestBuilder requestBuilder = WalletProcessingRequest.builder()
                .userId(userId)
                .transactionType(transactionType)
                .amount(amount)
                .idempotencyKey(idempotencyKey)
                .referenceCode(referenceCode)
                .bankCode(bank.getBankCode())
                .accountNumber(bank.getAccountNumber())
                .userEmail(wallet.getUser().getEmail())
                .userFullName(wallet.getUser().getFullName())
                .description(transaction.getDescription());

        enrichAntiFraudData(requestBuilder, userId, oldBal, newBal, senderId, senderAcc, clientContext);

        walletTransactionProcessor.process(requestBuilder.build());

        // Trả PENDING ngay — email sẽ gửi sau khi nhận kết quả từ PaymentGateway (SUCCESS / FAILED)
        return toWalletTransactionResponse(walletTransactionMapper.toDto(savedTransaction));
    }

    private void notifyTransaction(UserEntity user, WalletTransactionEntity transaction) {
        notifyTransaction(user, transaction, null);
    }

    private void notifyTransaction(UserEntity user, WalletTransactionEntity transaction, UserEntity recipient) {
        if (!hasEmail(user)) {
            return;
        }
        runAfterCommit(() -> sendTransactionNotification(user, transaction, recipient));
    }

    private void sendTransactionNotification(UserEntity user, WalletTransactionEntity transaction, UserEntity recipient) {
        try {
            walletNotificationMailService.sendTransactionNotification(
                    user.getEmail(),
                    user.getFullName(),
                    transaction.getType(),
                    transaction.getAmount(),
                    transaction.getStatus(),
                    transaction.getReferenceCode(),
                    transaction.getDescription(),
                    recipient == null ? null : recipient.getFullName(),
                    recipient == null ? null : recipient.getPhoneNumber()
            );
        } catch (RuntimeException e) {
            log.warn("Could not send wallet transaction notification to user {}", user.getId(), e);
        }
    }

    private void notifyTransactionError(UUID userId, String transactionType, BigDecimal amount, RuntimeException exception) {
        try {
            userRepository.findById(userId)
                    .filter(this::hasEmail)
                    .ifPresent(user -> walletNotificationMailService.sendTransactionErrorNotification(
                            user.getEmail(),
                            user.getFullName(),
                            transactionType,
                            amount,
                            exception.getMessage()
                    ));
        } catch (RuntimeException e) {
            log.warn("Could not send wallet transaction error notification to user {}", userId, e);
        }
    }

    private boolean hasEmail(UserEntity user) {
        return user != null && user.getEmail() != null && !user.getEmail().isBlank();
    }

    private void runAfterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }

    @Override
    @Transactional
    public void requestTransactionOtp(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_USER_NOT_FOUND, Constant.ERROR_USER_NOT_FOUND));
        String otp = transactionOtpService.createOtp(userId.toString());
        authMailService.sendTransactionOtpCode(user.getEmail(), user.getFullName(), otp);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPin(UUID userId) {
        return walletPinService.hasPin(userId);
    }

    private void verifyTransactionAuth(UUID userId, String pin, String otpCode) {
        boolean hasPinProvided = pin != null && !pin.isBlank();
        boolean hasOtpProvided = otpCode != null && !otpCode.isBlank();

        if (hasPinProvided) {
            if (!walletPinService.hasPin(userId)) {
                throw new AuthException(Constant.MESSAGE_INVALID_PIN, Constant.ERROR_INVALID_PIN);
            }
            boolean pinValid = walletPinService.verifyPin(userId, pin.trim());
            if (!pinValid) {
                throw new AuthException(Constant.MESSAGE_INVALID_PIN, Constant.ERROR_INVALID_PIN);
            }
            return;
        }

        if (hasOtpProvided) {
            if (!transactionOtpService.isOtpValid(userId.toString(), otpCode.trim())) {
                throw new AuthException(Constant.MESSAGE_INVALID_TRANSACTION_OTP, Constant.ERROR_INVALID_TRANSACTION_OTP);
            }
            transactionOtpService.consumeOtp(userId.toString());
            return;
        }

        // Cả 2 đều không được truyền
        if (walletPinService.hasPin(userId)) {
            throw new AuthException(Constant.MESSAGE_PIN_REQUIRED, Constant.ERROR_PIN_REQUIRED);
        } else {
            throw new AuthException(Constant.MESSAGE_TRANSACTION_OTP_REQUIRED, Constant.ERROR_TRANSACTION_OTP_REQUIRED);
        }
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
            return "Nạp tiền từ tài khoản ngân hàng " + bankCode + " " + accountNumber;
        }
        return "Rút tiền về tài khoản ngân hàng " + bankCode + " " + accountNumber;
    }

    private String buildWalletTransferDescription(String transactionType, UserEntity counterpartyUser) {
        if (Constant.WALLET_TRANSACTION_TYPE_WALLET_TRANSFER_OUT.equals(transactionType)) {
            return "Chuyển tiền đến " + counterpartyUser.getPhoneNumber();
        }
        return "Nhận tiền từ " + counterpartyUser.getPhoneNumber();
    }

    private String trimToUpperOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim().toUpperCase();
    }

    // -------------------------------------------------------------------------
    // QR Code
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public WalletQrResponse generateWalletQr(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_USER_NOT_FOUND, Constant.ERROR_USER_NOT_FOUND));

        String phoneNumber = user.getPhoneNumber();
        String fullName = user.getFullName() != null ? user.getFullName() : "";

        // Build JSON content to encode into QR
        String qrContent;
        try {
            qrContent = objectMapper.writeValueAsString(
                    Map.of("phoneNumber", phoneNumber, "fullName", fullName)
            );
        } catch (Exception e) {
            log.error("Failed to serialize QR content for user {}", userId, e);
            throw new AuthException(Constant.MESSAGE_QR_GENERATE_FAILED, Constant.ERROR_QR_GENERATE_FAILED);
        }

        // Generate QR PNG -> Base64
        String qrBase64;
        try {
            int size = Constant.WALLET_QR_SIZE_PX;
            EnumMap<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 2);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, size, size, hints);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            qrBase64 = Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            log.error("Failed to generate QR code image for user {}", userId, e);
            throw new AuthException(Constant.MESSAGE_QR_GENERATE_FAILED, Constant.ERROR_QR_GENERATE_FAILED);
        }

        return WalletQrResponse.builder()
                .qrBase64(qrBase64)
                .phoneNumber(phoneNumber)
                .fullName(fullName)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WalletQrDecodeResponse decodeWalletQr(WalletQrDecodeRequest request) {
        String qrContent = request.getQrContent().trim();

        // Parse JSON content from QR string
        String phoneNumber;
        try {
            JsonNode node = objectMapper.readTree(qrContent);
            JsonNode phoneNode = node.get("phoneNumber");
            if (phoneNode == null || phoneNode.isNull() || phoneNode.asText().isBlank()) {
                throw new AuthException(Constant.MESSAGE_QR_INVALID, Constant.ERROR_QR_INVALID);
            }
            phoneNumber = phoneNode.asText().trim();
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.debug("Invalid QR content format: {}", qrContent, e);
            throw new AuthException(Constant.MESSAGE_QR_INVALID, Constant.ERROR_QR_INVALID);
        }

        // Validate phone number format (10 digits)
        if (!phoneNumber.matches(Constant.PHONE_NUMBER_PATTERN)) {
            throw new AuthException(Constant.MESSAGE_QR_INVALID, Constant.ERROR_QR_INVALID);
        }

        // Lookup recipient — must exist and be active
        UserEntity recipient = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new AuthException(Constant.MESSAGE_RECIPIENT_NOT_FOUND, Constant.ERROR_RECIPIENT_NOT_FOUND));
        ensureActiveUser(recipient);

        return WalletQrDecodeResponse.builder()
                .phoneNumber(recipient.getPhoneNumber())
                .fullName(recipient.getFullName())
                .build();
    }

    private void enrichAntiFraudData(
            WalletProcessingRequest.WalletProcessingRequestBuilder builder,
            UUID userId,
            BigDecimal oldBalance,
            BigDecimal newBalance,
            String senderUserId,
            String senderAccountNumber,
            ClientSecurityContext clientContext
    ) {
        String deviceId = clientContext != null ? clientContext.getDeviceId() : null;
        String deviceFingerprint = clientContext != null ? clientContext.getDeviceFingerprint() : null;
        String geoLocation = clientContext != null ? clientContext.getGeoLocation() : null;
        String clientIp = clientContext != null ? clientContext.getClientIp() : null;

        OffsetDateTime since24h = OffsetDateTime.now().minusHours(24);
        long vCount = walletTransactionRepository.countByUserIdAndCreatedAtAfter(userId, since24h);
        BigDecimal vAmount = walletTransactionRepository.sumAmountByUserIdAndCreatedAtAfter(userId, since24h);
        long fanIn = walletTransactionRepository.countDistinctSendersToUserAfter(userId, since24h);
        long fanOut = walletTransactionRepository.countDistinctRecipientsFromUserAfter(userId, since24h);

        builder.senderUserId(senderUserId)
                .senderAccountNumber(senderAccountNumber)
                .oldBalance(oldBalance)
                .newBalance(newBalance)
                .deviceId(deviceId)
                .deviceFingerprint(deviceFingerprint)
                .geoLocation(geoLocation)
                .clientIp(clientIp)
                .velocity24hCount((int) vCount)
                .velocity24hAmount(vAmount)
                .fanIn24hCount((int) fanIn)
                .fanOut24hCount((int) fanOut);
    }

}
