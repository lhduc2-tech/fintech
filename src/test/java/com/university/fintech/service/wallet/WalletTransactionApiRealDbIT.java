package com.university.fintech.service.wallet;

import com.university.fintech.domain.dto.UserDTO;
import com.university.fintech.domain.entity.BankEntity;
import com.university.fintech.domain.entity.LinkedBankAccountEntity;
import com.university.fintech.domain.entity.UserEntity;
import com.university.fintech.domain.entity.WalletEntity;
import com.university.fintech.domain.entity.WalletTransactionEntity;
import com.university.fintech.infrastructure.primary.restful.response.wallet.WalletTransactionResponse;
import com.university.fintech.infrastructure.secondary.jpa.repository.BankRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.LinkedBankAccountRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.UserRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.WalletRepository;
import com.university.fintech.infrastructure.secondary.jpa.repository.WalletTransactionRepository;
import com.university.fintech.shared.constant.Constant;
import com.university.fintech.wire.security.JwtProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.Console;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.jpa.show-sql=false",
                "spring.jpa.properties.hibernate.show_sql=false",
                "spring.jpa.properties.hibernate.format_sql=false",
                "logging.level.org.hibernate.SQL=OFF",
                "logging.level.org.hibernate.orm.jdbc.bind=OFF",
                "logging.level.org.springframework=warn",
                "logging.level.com.zaxxer.hikari=warn",
                "logging.level.com.university.fintech=warn"
        }
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WalletTransactionApiRealDbIT {

    private static final BigDecimal AMOUNT = new BigDecimal("25.00");

    @Value("${server.port}")
    private int serverPort;
    
    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${application.path.base.private}")
    private String privatePath;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private LinkedBankAccountRepository linkedBankAccountRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private final RestTemplate restTemplate = new RestTemplate();

    private final List<String> createdIdempotencyKeys = new ArrayList<>();
    private final List<UUID> createdLinkedBankAccountIds = new ArrayList<>();
    private final List<UUID> createdWalletIds = new ArrayList<>();
    private final List<UUID> createdBankIds = new ArrayList<>();
    private final List<UUID> createdUserIds = new ArrayList<>();

    @AfterAll
    void askBeforeCleaningUpRealDatabaseRows() {
        System.out.printf("%n=== KẾT THÚC TEST API + DB THẬT ===%n");
        System.out.printf("Test đã thực thi xong. Các idempotency key test vừa tạo:%n");
        createdIdempotencyKeys.forEach(key -> System.out.printf("- %s%n", key));

        String cleanupMode = System.getProperty("wallet.it.cleanup", "prompt").trim();
        if ("always".equalsIgnoreCase(cleanupMode)
                || "true".equalsIgnoreCase(cleanupMode)
                || "c".equalsIgnoreCase(cleanupMode)) {
            cleanUpRealDatabaseRows();
            System.out.printf("Đã cleanup dữ liệu test trong DB vì wallet.it.cleanup=%s.%n", cleanupMode);
            return;
        }
        if ("keep".equalsIgnoreCase(cleanupMode) || "false".equalsIgnoreCase(cleanupMode)) {
            System.out.printf("Giữ nguyên dữ liệu test trong DB vì wallet.it.cleanup=%s.%n", cleanupMode);
            return;
        }

        Console console = System.console();
        if (console == null) {
            System.out.printf("%nMaven/Surefire hiện không cung cấp console tương tác cho test JVM, nên không đọc được phím bạn nhập.%n");
            System.out.printf("Dữ liệu test sẽ được giữ nguyên trong DB.%n");
            System.out.printf("Muốn cleanup tự động, chạy lại test với: -Dwallet.it.cleanup=always%n");
            System.out.printf("Muốn luôn giữ dữ liệu, chạy với: -Dwallet.it.cleanup=keep%n");
            return;
        }

        System.out.printf("%nNhập c rồi Enter để cleanup dữ liệu test trong DB.%n");
        String answer = console.readLine("Nhập phím bất kỳ rồi Enter để giữ nguyên dữ liệu và kết thúc tiến trình: ");
        if (!"c".equalsIgnoreCase(answer.trim())) {
            System.out.printf("Bạn đã chọn giữ nguyên dữ liệu test trong DB.%n");
            return;
        }

        cleanUpRealDatabaseRows();
        System.out.printf("Đã cleanup dữ liệu test trong DB.%n");
    }

    private void cleanUpRealDatabaseRows() {
        transactionTemplate.executeWithoutResult(status -> {
            for (String idempotencyKey : createdIdempotencyKeys) {
                jdbcTemplate.update(
                        "delete from wallet_ledger_entries where transaction_id in " +
                                "(select id from wallet_transactions where idempotency_key = ? or idempotency_key = ?)",
                        idempotencyKey,
                        idempotencyKey + Constant.WALLET_TRANSFER_CREDIT_IDEMPOTENCY_SUFFIX
                );
                jdbcTemplate.update(
                        "delete from wallet_transactions where idempotency_key = ? or idempotency_key = ?",
                        idempotencyKey,
                        idempotencyKey + Constant.WALLET_TRANSFER_CREDIT_IDEMPOTENCY_SUFFIX
                );
            }
            createdLinkedBankAccountIds.forEach(id -> jdbcTemplate.update("delete from linked_bank_accounts where id = ?", id));
            createdWalletIds.forEach(id -> jdbcTemplate.update("delete from wallets where id = ?", id));
            createdBankIds.forEach(id -> jdbcTemplate.update("delete from banks where id = ?", id));
            createdUserIds.forEach(id -> jdbcTemplate.update("delete from users where id = ?", id));
        });
    }

    @Test
    @DisplayName("Gọi API thật và DB thật: topup PENDING/SUCCESS/FAILED")
    void topUpFromBankApiRealDbFlow() {
        runBankApiScenario("Nạp tiền SUCCESS", "/bank-topup", "topup-success-realdb", Constant.STATUS_SUCCESS);
        runBankApiScenario("Nạp tiền PENDING", "/bank-topup", "topup-pending-realdb", Constant.STATUS_PENDING);
        runBankApiScenario("Nạp tiền FAILED", "/bank-topup", "topup-failed-realdb", Constant.STATUS_FAILED);
    }

    @Test
    @DisplayName("Gọi API thật và DB thật: withdraw PENDING/SUCCESS/FAILED")
    void withdrawToBankApiRealDbFlow() {
        runBankApiScenario("Rút tiền SUCCESS", "/bank-withdraw", "withdraw-success-realdb", Constant.STATUS_SUCCESS);
        runBankApiScenario("Rút tiền PENDING", "/bank-withdraw", "withdraw-pending-realdb", Constant.STATUS_PENDING);
        runBankApiScenario("Rút tiền FAILED", "/bank-withdraw", "withdraw-failed-realdb", Constant.STATUS_FAILED);
    }

    @Test
    @DisplayName("Gọi API thật và DB thật: transfer PENDING/SUCCESS/FAILED")
    void transferToWalletApiRealDbFlow() {
        runTransferApiScenario("Chuyển tiền SUCCESS", "transfer-success-realdb", Constant.STATUS_SUCCESS);
        runTransferApiScenario("Chuyển tiền PENDING", "transfer-pending-realdb", Constant.STATUS_PENDING);
        runTransferApiScenario("Chuyển tiền FAILED", "transfer-failed-realdb", Constant.STATUS_FAILED);
    }

    private void runBankApiScenario(String title, String apiPath, String idempotencyPrefix, String expectedStatus) {
        TestData data = createTestData(idempotencyPrefix);
        BigDecimal walletBefore = data.senderWallet.getAvailableBalance();
        BigDecimal bankBefore = data.bank.getBalance();

        Map<String, Object> request = Map.of(
                "linkedBankAccountId", data.linkedBankAccount.getId(),
                "amount", AMOUNT,
                "idempotencyKey", data.idempotencyKey
        );

        ResponseEntity<WalletTransactionResponse> response = exchange(
                apiPath,
                data.senderToken,
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(expectedStatus);

        WalletEntity walletAfter = walletRepository.findById(data.senderWallet.getId()).orElseThrow();
        BankEntity bankAfter = bankRepository.findById(data.bank.getId()).orElseThrow();
        WalletTransactionEntity transaction = walletTransactionRepository.findByIdempotencyKey(data.idempotencyKey)
                .orElseThrow();
        int ledgerCount = ledgerCount(transaction.getId());

        assertThat(transaction.getStatus()).isEqualTo(expectedStatus);
        if (Constant.STATUS_SUCCESS.equals(expectedStatus)) {
            assertThat(ledgerCount).isEqualTo(1);
        } else {
            assertThat(walletAfter.getAvailableBalance()).isEqualByComparingTo(walletBefore);
            assertThat(bankAfter.getBalance()).isEqualByComparingTo(bankBefore);
            assertThat(ledgerCount).isZero();
        }

        logScenario(
                title,
                apiPath,
                request,
                response.getBody(),
                data.idempotencyKey,
                expectedStatus,
                transaction.getStatus(),
                walletBefore,
                walletAfter.getAvailableBalance(),
                bankBefore,
                bankAfter.getBalance(),
                ledgerCount
        );
    }

    private void runTransferApiScenario(String title, String idempotencyPrefix, String expectedStatus) {
        TestData data = createTestData(idempotencyPrefix);
        BigDecimal senderBefore = data.senderWallet.getAvailableBalance();
        BigDecimal receiverBefore = data.receiverWallet.getAvailableBalance();

        Map<String, Object> request = Map.of(
                "recipientPhoneNumber", data.receiver.getPhoneNumber(),
                "amount", AMOUNT,
                "idempotencyKey", data.idempotencyKey
        );

        ResponseEntity<WalletTransactionResponse> response = exchange(
                "/transfer",
                data.senderToken,
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(expectedStatus);

        WalletEntity senderAfter = walletRepository.findById(data.senderWallet.getId()).orElseThrow();
        WalletEntity receiverAfter = walletRepository.findById(data.receiverWallet.getId()).orElseThrow();
        WalletTransactionEntity debitTransaction = walletTransactionRepository.findByIdempotencyKey(data.idempotencyKey)
                .orElseThrow();
        WalletTransactionEntity creditTransaction = walletTransactionRepository
                .findByIdempotencyKey(data.idempotencyKey + Constant.WALLET_TRANSFER_CREDIT_IDEMPOTENCY_SUFFIX)
                .orElseThrow();
        int ledgerCount = ledgerCount(debitTransaction.getId()) + ledgerCount(creditTransaction.getId());

        assertThat(debitTransaction.getStatus()).isEqualTo(expectedStatus);
        assertThat(creditTransaction.getStatus()).isEqualTo(expectedStatus);
        if (Constant.STATUS_SUCCESS.equals(expectedStatus)) {
            assertThat(ledgerCount).isEqualTo(2);
        } else {
            assertThat(senderAfter.getAvailableBalance()).isEqualByComparingTo(senderBefore);
            assertThat(receiverAfter.getAvailableBalance()).isEqualByComparingTo(receiverBefore);
            assertThat(ledgerCount).isZero();
        }

        logScenario(
                title,
                "/transfer",
                request,
                response.getBody(),
                data.idempotencyKey,
                expectedStatus,
                debitTransaction.getStatus(),
                senderBefore,
                senderAfter.getAvailableBalance(),
                receiverBefore,
                receiverAfter.getAvailableBalance(),
                ledgerCount
        );
    }

    private ResponseEntity<WalletTransactionResponse> exchange(
            String apiPath,
            String token,
            Map<String, Object> request
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            return restTemplate.exchange(
                    baseUrl() + apiPath,
                    HttpMethod.POST,
                    new HttpEntity<>(request, headers),
                    WalletTransactionResponse.class
            );
        } catch (ResourceAccessException exception) {
            fail(
                    "Không gọi được backend thật tại " + baseUrl() +
                            ". Hãy chạy backend trước rồi mới chạy test này.",
                    exception
            );
            throw exception;
        }
    }

    private TestData createTestData(String idempotencyPrefix) {
        return transactionTemplate.execute(status -> {
            String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
            String idempotencyKey = idempotencyPrefix + "-" + suffix;
            createdIdempotencyKeys.add(idempotencyKey);

            UserEntity sender = user("sender-" + suffix, randomPhone());
            UserEntity receiver = user("receiver-" + suffix, randomPhone());
            sender = userRepository.saveAndFlush(sender);
            receiver = userRepository.saveAndFlush(receiver);
            createdUserIds.add(receiver.getId());
            createdUserIds.add(sender.getId());

            WalletEntity senderWallet = wallet(sender, "100.00");
            WalletEntity receiverWallet = wallet(receiver, "10.00");
            senderWallet = walletRepository.saveAndFlush(senderWallet);
            receiverWallet = walletRepository.saveAndFlush(receiverWallet);
            createdWalletIds.add(receiverWallet.getId());
            createdWalletIds.add(senderWallet.getId());

            BankEntity bank = bank("1000.00", suffix);
            bank = bankRepository.saveAndFlush(bank);
            createdBankIds.add(bank.getId());

            LinkedBankAccountEntity linkedBankAccount = LinkedBankAccountEntity.builder()
                    .wallet(senderWallet)
                    .bank(bank)
                    .status(Constant.STATUS_ACTIVE)
                    .build();
            linkedBankAccount = linkedBankAccountRepository.saveAndFlush(linkedBankAccount);
            createdLinkedBankAccountIds.add(linkedBankAccount.getId());

            return new TestData(
                    sender,
                    receiver,
                    senderWallet,
                    receiverWallet,
                    bank,
                    linkedBankAccount,
                    idempotencyKey,
                    tokenFor(sender)
            );
        });
    }

    private String tokenFor(UserEntity user) {
        return jwtProvider.generateToken(
                UserDTO.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .phoneNumber(user.getPhoneNumber())
                        .status(user.getStatus())
                        .roleCodes(Set.of("USER"))
                        .permissionCodes(Set.of())
                        .build()
        );
    }

    private String baseUrl() {
        return "http://localhost:" + serverPort + contextPath + privatePath + "/wallet";
    }

    private int ledgerCount(UUID transactionId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from wallet_ledger_entries where transaction_id = ?",
                Integer.class,
                transactionId
        );
        return count == null ? 0 : count;
    }

    private void logScenario(
            String title,
            String apiPath,
            Map<String, Object> request,
            WalletTransactionResponse response,
            String idempotencyKey,
            String processorStatus,
            String transactionStatusInDb,
            BigDecimal firstBalanceBefore,
            BigDecimal firstBalanceAfter,
            BigDecimal secondBalanceBefore,
            BigDecimal secondBalanceAfter,
            int ledgerEntries
    ) {
        System.out.printf("%n============================================================%n");
        System.out.printf("TÌNH HUỐNG: %s%n", title);
        System.out.printf("MỤC TIÊU: processor trả %s, transaction trong DB phải là %s.%n", processorStatus, processorStatus);
        System.out.printf("API: POST %s%s%n", baseUrl(), apiPath);

        System.out.printf("%nDỮ LIỆU ĐẦU VÀO%n");
        System.out.printf("- idempotencyKey: %s%n", idempotencyKey);
        System.out.printf("- amount: %s%n", request.get("amount"));
        if (request.containsKey("linkedBankAccountId")) {
            System.out.printf("- linkedBankAccountId: %s%n", request.get("linkedBankAccountId"));
            System.out.printf("- số dư ví trước khi gọi API: %s%n", firstBalanceBefore);
            System.out.printf("- số dư ngân hàng trước khi gọi API: %s%n", secondBalanceBefore);
        } else {
            System.out.printf("- recipientPhoneNumber: %s%n", request.get("recipientPhoneNumber"));
            System.out.printf("- số dư ví người gửi trước khi gọi API: %s%n", firstBalanceBefore);
            System.out.printf("- số dư ví người nhận trước khi gọi API: %s%n", secondBalanceBefore);
        }

        System.out.printf("%nKẾT QUẢ API TRẢ VỀ%n");
        System.out.printf("- transactionId: %s%n", response.getId());
        System.out.printf("- type: %s%n", response.getType());
        System.out.printf("- status: %s%n", response.getStatus());
        System.out.printf("- referenceCode: %s%n", response.getReferenceCode());
        System.out.printf("- description: %s%n", response.getDescription());

        System.out.printf("%nKẾT QUẢ KIỂM TRA TRONG DB%n");
        System.out.printf("- trạng thái transaction trong DB: %s%n", transactionStatusInDb);
        if (request.containsKey("linkedBankAccountId")) {
            System.out.printf("- số dư ví sau khi gọi API: %s%n", firstBalanceAfter);
            System.out.printf("- số dư ngân hàng sau khi gọi API: %s%n", secondBalanceAfter);
        } else {
            System.out.printf("- số dư ví người gửi sau khi gọi API: %s%n", firstBalanceAfter);
            System.out.printf("- số dư ví người nhận sau khi gọi API: %s%n", secondBalanceAfter);
        }
        System.out.printf("- số ledger entry được tạo trong DB: %d%n", ledgerEntries);

        System.out.printf("%nKẾT LUẬN%n");
        if (Constant.STATUS_SUCCESS.equals(processorStatus)) {
            System.out.printf("- SUCCESS: balance đã thay đổi và ledger đã được tạo.%n");
        } else {
            System.out.printf("- %s: balance giữ nguyên và không tạo ledger.%n", processorStatus);
        }
    }

    private UserEntity user(String name, String phoneNumber) {
        return UserEntity.builder()
                .email(name + "@wallet-it.local")
                .passwordHash("integration-test-password-hash")
                .status(Constant.STATUS_ACTIVE)
                .fullName("API Real DB Test " + name)
                .phoneNumber(phoneNumber)
                .failedLoginAttempts(0)
                .build();
    }

    private WalletEntity wallet(UserEntity owner, String balance) {
        return WalletEntity.builder()
                .user(owner)
                .availableBalance(new BigDecimal(balance))
                .holdBalance(BigDecimal.ZERO)
                .status(Constant.STATUS_ACTIVE)
                .build();
    }

    private BankEntity bank(String balance, String suffix) {
        return BankEntity.builder()
                .bankCode("IT" + suffix.substring(0, 4).toUpperCase())
                .bankName("Integration Test Bank")
                .accountNumber("9" + suffix)
                .accountHolderName("API Real DB Test")
                .status(Constant.STATUS_ACTIVE)
                .balance(new BigDecimal(balance))
                .build();
    }

    private String randomPhone() {
        int value = ThreadLocalRandom.current().nextInt(10_000_000, 100_000_000);
        return "09" + value;
    }

    private record TestData(
            UserEntity sender,
            UserEntity receiver,
            WalletEntity senderWallet,
            WalletEntity receiverWallet,
            BankEntity bank,
            LinkedBankAccountEntity linkedBankAccount,
            String idempotencyKey,
            String senderToken
    ) {
    }
}
