package com.university.fintech.service.wallet;

import com.university.fintech.domain.dto.WalletTransactionDTO;
import com.university.fintech.domain.entity.BankEntity;
import com.university.fintech.domain.entity.LinkedBankAccountEntity;
import com.university.fintech.domain.entity.UserEntity;
import com.university.fintech.domain.entity.WalletEntity;
import com.university.fintech.domain.entity.WalletTransactionEntity;
import com.university.fintech.domain.mapper.LinkedBankAccountMapper;
import com.university.fintech.domain.mapper.WalletMapper;
import com.university.fintech.domain.mapper.WalletTransactionMapper;
import com.university.fintech.domain.port.secondary.WalletTransactionProcessor;
import com.university.fintech.domain.port.secondary.model.WalletProcessingResult;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletBankTransferRequest;
import com.university.fintech.infrastructure.primary.restful.request.wallet.WalletTransferRequest;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletTransactionResponse;
import com.university.fintech.infrastructure.secondary.jpa.repository.BankRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.LinkedBankAccountRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.UserRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.WalletLedgerEntryRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.WalletRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.WalletTransactionRepository;
import com.university.fintech.shared.constant.Constant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletBankServiceImplTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID RECIPIENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID WALLET_ID = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID RECIPIENT_WALLET_ID = UUID.fromString("00000000-0000-0000-0000-000000000012");
    private static final UUID BANK_ID = UUID.fromString("00000000-0000-0000-0000-000000000021");
    private static final UUID LINKED_BANK_ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000031");

    @Mock
    private UserRepository userRepository;
    @Mock
    private BankRepository bankRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private LinkedBankAccountRepository linkedBankAccountRepository;
    @Mock
    private WalletTransactionRepository walletTransactionRepository;
    @Mock
    private WalletLedgerEntryRepository walletLedgerEntryRepository;
    @Mock
    private WalletTransactionProcessor walletTransactionProcessor;
    @Mock
    private WalletMapper walletMapper;
    @Mock
    private LinkedBankAccountMapper linkedBankAccountMapper;
    @Mock
    private WalletTransactionMapper walletTransactionMapper;

    @InjectMocks
    private WalletBankServiceImpl walletBankService;

    private UserEntity user;
    private WalletEntity wallet;
    private BankEntity bank;
    private LinkedBankAccountEntity bankAccount;

    @BeforeEach
    void setUp() {
        user = user(USER_ID, "0900000001");
        wallet = wallet(WALLET_ID, user, "100.00");
        bank = bank("1000.00");
        bankAccount = LinkedBankAccountEntity.builder()
                .id(LINKED_BANK_ACCOUNT_ID)
                .wallet(wallet)
                .bank(bank)
                .status(Constant.STATUS_ACTIVE)
                .build();

        when(walletTransactionMapper.toEntity(any(WalletTransactionDTO.class)))
                .thenAnswer(invocation -> transactionFromDto(invocation.getArgument(0)));
        when(walletTransactionMapper.toDto(any(WalletTransactionEntity.class)))
                .thenAnswer(invocation -> dtoFromTransaction(invocation.getArgument(0)));
        when(walletTransactionRepository.save(any(WalletTransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void topUpFromBankCreatesPendingThenSuccessAndCreditsWalletLedger() {
        arrangeBankTransfer(Constant.STATUS_SUCCESS);

        WalletTransactionResponse response = walletBankService.topUpFromBank(USER_ID, bankRequest("topup-success"));

        assertThat(response.getStatus()).isEqualTo(Constant.STATUS_SUCCESS);
        assertThat(wallet.getAvailableBalance()).isEqualByComparingTo("125.00");
        assertThat(bank.getBalance()).isEqualByComparingTo("975.00");
        verify(walletLedgerEntryRepository).save(any());
    }

    @Test
    void topUpFromBankKeepsPendingWithoutMovingMoney() {
        arrangeBankTransfer(Constant.STATUS_PENDING);

        WalletTransactionResponse response = walletBankService.topUpFromBank(USER_ID, bankRequest("topup-pending"));

        assertThat(response.getStatus()).isEqualTo(Constant.STATUS_PENDING);
        assertThat(wallet.getAvailableBalance()).isEqualByComparingTo("100.00");
        assertThat(bank.getBalance()).isEqualByComparingTo("1000.00");
        verify(walletLedgerEntryRepository, never()).save(any());
    }

    @Test
    void topUpFromBankMarksFailedWithoutMovingMoney() {
        arrangeBankTransfer(Constant.STATUS_FAILED);

        WalletTransactionResponse response = walletBankService.topUpFromBank(USER_ID, bankRequest("topup-failed"));

        assertThat(response.getStatus()).isEqualTo(Constant.STATUS_FAILED);
        assertThat(wallet.getAvailableBalance()).isEqualByComparingTo("100.00");
        assertThat(bank.getBalance()).isEqualByComparingTo("1000.00");
        verify(walletLedgerEntryRepository, never()).save(any());
    }

    @Test
    void withdrawToBankCreatesPendingThenSuccessAndDebitsWalletLedger() {
        arrangeBankTransfer(Constant.STATUS_SUCCESS);

        WalletTransactionResponse response = walletBankService.withdrawToBank(USER_ID, bankRequest("withdraw-success"));

        assertThat(response.getStatus()).isEqualTo(Constant.STATUS_SUCCESS);
        assertThat(wallet.getAvailableBalance()).isEqualByComparingTo("75.00");
        assertThat(bank.getBalance()).isEqualByComparingTo("1025.00");
        verify(walletLedgerEntryRepository).save(any());
    }

    @Test
    void withdrawToBankKeepsPendingWithoutMovingMoney() {
        arrangeBankTransfer(Constant.STATUS_PENDING);

        WalletTransactionResponse response = walletBankService.withdrawToBank(USER_ID, bankRequest("withdraw-pending"));

        assertThat(response.getStatus()).isEqualTo(Constant.STATUS_PENDING);
        assertThat(wallet.getAvailableBalance()).isEqualByComparingTo("100.00");
        assertThat(bank.getBalance()).isEqualByComparingTo("1000.00");
        verify(walletLedgerEntryRepository, never()).save(any());
    }

    @Test
    void withdrawToBankMarksFailedWithoutMovingMoney() {
        arrangeBankTransfer(Constant.STATUS_FAILED);

        WalletTransactionResponse response = walletBankService.withdrawToBank(USER_ID, bankRequest("withdraw-failed"));

        assertThat(response.getStatus()).isEqualTo(Constant.STATUS_FAILED);
        assertThat(wallet.getAvailableBalance()).isEqualByComparingTo("100.00");
        assertThat(bank.getBalance()).isEqualByComparingTo("1000.00");
        verify(walletLedgerEntryRepository, never()).save(any());
    }

    @Test
    void transferToWalletCreatesPendingThenSuccessAndCreatesTwoLedgerEntries() {
        UserEntity recipient = user(RECIPIENT_ID, "0900000002");
        WalletEntity recipientWallet = wallet(RECIPIENT_WALLET_ID, recipient, "10.00");
        arrangeWalletTransfer(recipient, recipientWallet, Constant.STATUS_SUCCESS);

        WalletTransactionResponse response = walletBankService.transferToWallet(USER_ID, transferRequest("transfer-success"));

        assertThat(response.getStatus()).isEqualTo(Constant.STATUS_SUCCESS);
        assertThat(wallet.getAvailableBalance()).isEqualByComparingTo("75.00");
        assertThat(recipientWallet.getAvailableBalance()).isEqualByComparingTo("35.00");
        verify(walletLedgerEntryRepository, times(2)).save(any());
    }

    @Test
    void transferToWalletKeepsPendingWithoutMovingMoney() {
        UserEntity recipient = user(RECIPIENT_ID, "0900000002");
        WalletEntity recipientWallet = wallet(RECIPIENT_WALLET_ID, recipient, "10.00");
        arrangeWalletTransfer(recipient, recipientWallet, Constant.STATUS_PENDING);

        WalletTransactionResponse response = walletBankService.transferToWallet(USER_ID, transferRequest("transfer-pending"));

        assertThat(response.getStatus()).isEqualTo(Constant.STATUS_PENDING);
        assertThat(wallet.getAvailableBalance()).isEqualByComparingTo("100.00");
        assertThat(recipientWallet.getAvailableBalance()).isEqualByComparingTo("10.00");
        verify(walletLedgerEntryRepository, never()).save(any());
    }

    @Test
    void transferToWalletMarksFailedWithoutMovingMoney() {
        UserEntity recipient = user(RECIPIENT_ID, "0900000002");
        WalletEntity recipientWallet = wallet(RECIPIENT_WALLET_ID, recipient, "10.00");
        arrangeWalletTransfer(recipient, recipientWallet, Constant.STATUS_FAILED);

        WalletTransactionResponse response = walletBankService.transferToWallet(USER_ID, transferRequest("transfer-failed"));

        assertThat(response.getStatus()).isEqualTo(Constant.STATUS_FAILED);
        assertThat(wallet.getAvailableBalance()).isEqualByComparingTo("100.00");
        assertThat(recipientWallet.getAvailableBalance()).isEqualByComparingTo("10.00");
        verify(walletLedgerEntryRepository, never()).save(any());
    }

    private void arrangeBankTransfer(String processorStatus) {
        when(walletTransactionRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(walletRepository.findByUserIdForUpdate(USER_ID)).thenReturn(Optional.of(wallet));
        when(linkedBankAccountRepository.findByIdAndUserIdForUpdate(LINKED_BANK_ACCOUNT_ID, USER_ID))
                .thenReturn(Optional.of(bankAccount));
        when(bankRepository.findByIdForUpdate(BANK_ID)).thenReturn(Optional.of(bank));
        when(userRepository.getReferenceById(USER_ID)).thenReturn(user);
        when(walletTransactionProcessor.process(any())).thenReturn(processorResult(processorStatus));
    }

    private void arrangeWalletTransfer(UserEntity recipient, WalletEntity recipientWallet, String processorStatus) {
        when(walletTransactionRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber(recipient.getPhoneNumber())).thenReturn(Optional.of(recipient));
        when(walletRepository.findByUserIdForUpdate(USER_ID)).thenReturn(Optional.of(wallet));
        when(walletRepository.findByUserIdForUpdate(RECIPIENT_ID)).thenReturn(Optional.of(recipientWallet));
        when(userRepository.getReferenceById(USER_ID)).thenReturn(user);
        when(userRepository.getReferenceById(RECIPIENT_ID)).thenReturn(recipient);
        when(walletTransactionProcessor.process(any())).thenReturn(processorResult(processorStatus));
    }

    private WalletBankTransferRequest bankRequest(String key) {
        WalletBankTransferRequest request = new WalletBankTransferRequest();
        request.setLinkedBankAccountId(LINKED_BANK_ACCOUNT_ID);
        request.setAmount(new BigDecimal("25.00"));
        request.setIdempotencyKey(key);
        return request;
    }

    private WalletTransferRequest transferRequest(String key) {
        WalletTransferRequest request = new WalletTransferRequest();
        request.setRecipientPhoneNumber("0900000002");
        request.setAmount(new BigDecimal("25.00"));
        request.setIdempotencyKey(key);
        return request;
    }

    private WalletProcessingResult processorResult(String status) {
        return WalletProcessingResult.builder()
                .status(status)
                .message(status)
                .build();
    }

    private WalletTransactionEntity transactionFromDto(WalletTransactionDTO dto) {
        return WalletTransactionEntity.builder()
                .wallet(wallet)
                .user(user)
                .linkedBankAccount(dto.getLinkedBankAccountId() == null ? null : bankAccount)
                .type(dto.getType())
                .amount(dto.getAmount())
                .status(dto.getStatus())
                .idempotencyKey(dto.getIdempotencyKey())
                .referenceCode(dto.getReferenceCode())
                .description(dto.getDescription())
                .build();
    }

    private WalletTransactionDTO dtoFromTransaction(WalletTransactionEntity transaction) {
        return WalletTransactionDTO.builder()
                .id(transaction.getId())
                .walletId(transaction.getWallet() == null ? null : transaction.getWallet().getId())
                .userId(transaction.getUser() == null ? null : transaction.getUser().getId())
                .linkedBankAccountId(transaction.getLinkedBankAccount() == null ? null : transaction.getLinkedBankAccount().getId())
                .counterpartyWalletId(transaction.getCounterpartyWallet() == null ? null : transaction.getCounterpartyWallet().getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .idempotencyKey(transaction.getIdempotencyKey())
                .referenceCode(transaction.getReferenceCode())
                .description(transaction.getDescription())
                .build();
    }

    private UserEntity user(UUID id, String phoneNumber) {
        return UserEntity.builder()
                .id(id)
                .fullName("Test User")
                .phoneNumber(phoneNumber)
                .status(Constant.STATUS_ACTIVE)
                .build();
    }

    private WalletEntity wallet(UUID id, UserEntity owner, String balance) {
        return WalletEntity.builder()
                .id(id)
                .user(owner)
                .availableBalance(new BigDecimal(balance))
                .holdBalance(BigDecimal.ZERO)
                .status(Constant.STATUS_ACTIVE)
                .build();
    }

    private BankEntity bank(String balance) {
        return BankEntity.builder()
                .id(BANK_ID)
                .bankCode("VCB")
                .bankName("Vietcombank")
                .accountNumber("123456789")
                .accountHolderName("Test User")
                .status(Constant.STATUS_ACTIVE)
                .balance(new BigDecimal(balance))
                .build();
    }
}
