# TÀI LIỆU TOÀN DIỆN CÁC TEST CASE BACKEND KHÔNG THỂ KIỂM THỬ THỦ CÔNG QUA FRONTEND
> **Hệ thống**: Fintech Service (Digital Wallet & Core Banking Integration)  
> **Mục tiêu**: Kiểm thử các lỗ hổng bảo mật, tranh chấp tài nguyên (Concurrency / Race Condition), bypass validation, gian lận vị trí (Anti-Fraud), và cơ chế xử lý lỗi phân tán (Saga Timeout / Compensation).

---

## MỤC LỤC
1. [Tại sao các Test Case này không thể thực hiện qua Frontend?](#1-tại-sao-các-test-case-này-không-thể-thực-hiện-qua-frontend)
2. [Bảng tổng hợp nhanh các Test Case](#2-bảng-tổng-hợp-nhanh-các-test-case)
3. [Chi tiết các Test Case (Input, Output mong đợi, Ý nghĩa)](#3-chi-tiết-các-test-case)
   - [Nhóm 1: Concurrency, Race Condition & Double-Spending](#nhóm-1-concurrency-race-condition--double-spending)
   - [Nhóm 2: Bypass Validation & Gian lận Payload](#nhóm-2-bypass-validation--gian-lận-payload)
   - [Nhóm 3: Brute-Force, Rate Limiting & Token Lifecycle](#nhóm-3-brute-force-rate-limiting--token-lifecycle)
   - [Nhóm 4: Anti-Fraud Header Spoofing & Distributed Timeout](#nhóm-4-anti-fraud-header-spoofing--distributed-timeout)
4. [Hướng dẫn thực hành kiểm thử qua Postman](#4-hướng-dẫn-thực-hành-kiểm-thử-qua-postman)
5. [Hướng dẫn thực hành kiểm thử bằng Script Python đa luồng](#5-hướng-dẫn-thực-hành-kiểm-thử-bằng-script-python-đa-luồng)

---

## 1. TẠI SAO CÁC TEST CASE NÀY KHÔNG THỂ THỰC HIỆN QUA FRONTEND?

1. **Frontend Validation chặn trước**: Giao diện người dùng (UI/UX) đã tự động validate dữ liệu (chặn số âm, chặn bấm submit nhiều lần, giới hạn ô nhập). Hacker sẽ bỏ qua Frontend và dùng công cụ như Postman, Burp Suite, hoặc Script để gọi trực tiếp tới REST API.
2. **Độ trễ thao tác người dùng (User Latency)**: Thao tác bấm tay của con người mất tối thiểu 200 - 500ms, không thể gửi 2 hay nhiều HTTP Request chạm đến tầng Database trong cùng một milli-giây để kiểm tra cơ chế Khóa bi quan (**Pessimistic Locking**) và chống tiêu tiền kép (**Double-Spending**).
3. **Trình duyệt cô lập HTTP Headers**: Trình duyệt Web/Mobile không cho phép tùy biến tự do các Header hạ tầng như `X-Forwarded-For`, `X-Geo-Location`, `X-Device-Fingerprint` để kiểm thử hệ thống AI Anti-Fraud (Phát hiện gian lận vị trí / Di chuyển bất khả thi).
4. **Không thể giả lập sự cố mạng & Microservice**: Frontend không thể tự ngắt kết nối Kafka, làm sập Payment Gateway giả lập để kiểm thử cơ chế tự động bù trừ tiền (Refund Hold Balance) của Batch Job.

---

## 2. BẢNG TỔNG HỢP NHANH CÁC TEST CASE

| STT | Mã Case | Tên Test Case | Công cụ | Trọng tâm kiểm thử |
| :--- | :--- | :--- | :---: | :--- |
| **01** | `TC-CONC-01` | Double-Spending Attack (Chi tiêu kép) | **Python Script** | `SELECT ... FOR UPDATE`, không âm tiền |
| **02** | `TC-CONC-02` | Idempotency Key Concurrency Race | **Python Script** | Unique Constraint DB, không đúp giao dịch |
| **03** | `TC-CONC-03` | Cross-Transfer Deadlock Prevention | **Python Script** | Sắp xếp thứ tự Lock theo UUID (`orderedUserIds`) |
| **04** | `TC-SEC-01` | Chuyển / Rút tiền với số tiền âm (`amount < 0`) | **Postman** | Bean Validation `@Positive`, chống tạo tiền khống |
| **05** | `TC-SEC-02` | Tái sử dụng Idempotency Key với Payload khác | **Postman** | Đối chiếu hash/payload của giao dịch gốc |
| **06** | `TC-SEC-03` | Bypass FaceID Token (Giao dịch ≥ 10M VNĐ) | **Postman** | QĐ 2345/QĐ-NHNN, One-time token TTL |
| **07** | `TC-SEC-04` | Tự chuyển tiền cho chính mình (Self-Transfer) | **Postman** | Logic kiểm tra `senderId != recipientId` |
| **08** | `TC-SEC-05` | Chuyển tiền tới tài khoản bị Khóa / Ví Rủi ro | **Postman** | Chặn dòng tiền tẩu tán vào ví `LOCKED`/`RISK` |
| **09** | `TC-AUTH-01`| Brute-Force Password & Tự động khóa tài khoản | **Postman Runner** | `failedLoginAttempts == 5` -> Lock & gửi mã mở khóa |
| **10** | `TC-AUTH-02`| Tái sử dụng JWT Token sau khi đã Logout | **Postman** | Redis Token Blacklist thu hồi token ngay lập tức |
| **11** | `TC-FRD-01` | Kích hoạt Fraud Alert qua Device Anomaly (ATO) | **Postman** | Fraud Engine: `RULE-DEVICE-ANOMALY` + `RULE-STR-EW-04` → `risk_level = MEDIUM/HIGH` |
| **12** | `TC-JOB-01` | Xử lý giao dịch treo (Pending Timeout & Refund) | **Database / Script** | Hoàn trả `holdBalance` về `availableBalance` |

---

## 3. CHI TIẾT CÁC TEST CASE

### NHÓM 1: CONCURRENCY, RACE CONDITION & DOUBLE-SPENDING

---

#### 📌 Case 1: Double-Spending Attack (Tấn công tiêu tiền kép đồng thời)
* **Ý nghĩa**: Kiểm tra cơ chế Khóa bi quan (`SELECT ... FOR UPDATE`) trên bảng `wallets`. Khi user gửi 2 lệnh chuyển tiền trong cùng 1 milli-giây với tổng tiền vượt quá số dư, hệ thống bắt buộc chỉ cho phép 1 lệnh đi qua và từ chối lệnh còn lại.
* **Thông tin đầu vào (Input)**:
  * Số dư ví User A: **`500,000 VNĐ`**.
  * Gửi **đồng thời 2 requests** chuyển tiền tới User B, mỗi request chuyển **`400,000 VNĐ`** (Tổng = 800,000 > 500,000) với 2 `idempotencyKey` khác nhau.
  * **Endpoint**: `POST /private/api/v1/wallet/transfer`
  * **Headers**: `Authorization: Bearer <JWT_USER_A>`, `Content-Type: application/json`
  * **Payload Request 1**:
    ```json
    {
      "recipientPhoneNumber": "0987654321",
      "amount": 400000,
      "pin": "123456",
      "idempotencyKey": "RACE-TEST-KEY-001"
    }
    ```
  * **Payload Request 2**:
    ```json
    {
      "recipientPhoneNumber": "0987654321",
      "amount": 400000,
      "pin": "123456",
      "idempotencyKey": "RACE-TEST-KEY-002"
    }
    ```
* **Đầu ra mong đợi (Expected Output)**:
  * **1 Request**: Trả về `200 OK`, trạng thái `PENDING` (sau đó `SUCCESS`), số dư bị trừ 400,000 VNĐ.
  * **1 Request còn lại**: Trả về `400 Bad Request`, mã lỗi `INSUFFICIENT_WALLET_BALANCE`, message *"So du vi khong du de thuc hien giao dich. Vui long nap them tien."*
  * **Trạng thái Database**: Số dư ví User A còn lại đúng **`100,000 VNĐ`**, không bị âm tiền.

---

#### 📌 Case 2: Idempotency Key Concurrency Race (Trùng khóa Idempotency khi bắn đồng thời)
* **Ý nghĩa**: Kiểm tra ràng buộc Unique Constraint của cột `idempotencyKey` trong bảng `wallet_transactions` và cơ chế chống ghi đúp giao dịch khi xảy ra hiện tượng gửi lặp gói tin mạng.
* **Thông tin đầu vào (Input)**:
  * Gửi **5 requests song song** tới `POST /private/api/v1/wallet/transfer`.
  * Cả 5 requests đều có **cùng 1 `idempotencyKey`**: `"IDEM-RACE-00001"`.
* **Đầu ra mong đợi (Expected Output)**:
  * Hệ thống chỉ xử lý **duy nhất 1 giao dịch**, trừ tiền đúng 1 lần.
  * Các request đồng thời còn lại trả về kết quả của giao dịch đầu tiên (Idempotent Replay) hoặc nhận thông báo an toàn. Bảng `wallet_transactions` chỉ có duy nhất 1 bản ghi.

---

#### 📌 Case 3: Cross-Transfer Deadlock Prevention (Tránh Deadlock khi 2 User chuyển chéo nhau)
* **Ý nghĩa**: Khi User A chuyển tiền cho User B và đồng thời User B chuyển tiền cho User A, nếu không sắp xếp thứ tự Lock ID theo chuẩn UUID (`orderedUserIds = List.of(userIdA, userIdB).stream().sorted()`), Database sẽ xảy ra tranh chấp chéo dẫn đến `PSQLException: deadlock detected`.
* **Thông tin đầu vào (Input)**:
  * Luồng 1: User A chuyển 50,000 VNĐ cho User B.
  * Luồng 2: User B chuyển 50,000 VNĐ cho User A (Chạy song song cùng lúc).
* **Đầu ra mong đợi (Expected Output)**:
  * Cả 2 giao dịch đều trả về `200 OK`.
  * Không phát sinh lỗi Deadlock hay Internal Server Error `500`.

---

### NHÓM 2: BYPASS VALIDATION & GIAN LẬN PAYLOAD

---

#### 📌 Case 4: Chuyển / Rút tiền với số tiền âm (`amount < 0`) hoặc bằng 0
* **Ý nghĩa**: Ngăn chặn kẻ tấn công can thiệp HTTP body gửi số tiền âm để cộng tiền trái phép vào tài khoản ví.
* **Thông tin đầu vào (Input)**:
  * **Endpoint**: `POST /private/api/v1/wallet/transfer`
  * **Payload**:
    ```json
    {
      "recipientPhoneNumber": "0987654321",
      "amount": -500000,
      "pin": "123456",
      "idempotencyKey": "NEGATIVE-AMT-01"
    }
    ```
* **Đầu ra mong đợi (Expected Output)**:
  * HTTP `400 Bad Request`.
  * Lỗi Bean Validation: *"Số tiền phải lớn hơn 0"*.

---

#### 📌 Case 5: Tái sử dụng Idempotency Key nhưng THAY ĐỔI thông tin giao dịch (Tampering)
* **Ý nghĩa**: Ngăn chặn tấn công Replay Attack khi hacker lấy trộm `idempotencyKey` của giao dịch 10.000 VNĐ đã duyệt rồi gửi lại với `amount = 10,000,000 VNĐ` hoặc đổi số điện thoại người nhận.
* **Thông tin đầu vào (Input)**:
  * **Bước 1**: Gửi giao dịch hợp lệ với `idempotencyKey: "KEY-XYZ"`, `amount: 10000`, `recipientPhoneNumber: "0911111111"`.
  * **Bước 2**: Gửi request thứ 2 với cùng `idempotencyKey: "KEY-XYZ"` nhưng đổi `amount: 5000000` hoặc `recipientPhoneNumber: "0999999999"`.
* **Đầu ra mong đợi (Expected Output)**:
  * HTTP `400 Bad Request`.
  * Error code: `IDEMPOTENCY_PAYLOAD_MISMATCH` / Message: *"Ma giao dich da duoc dung voi thong tin chuyen khoan khac. Vui long tao giao dich moi."* (Hệ thống kiểm tra qua `ensureSameWalletTransferPayload()` — so sánh `amount` và `recipientPhoneNumber` với giao dịch gốc trong DB.)

---

#### 📌 Case 6: Bypass Xác thực FaceID cho giao dịch lớn (≥ 10,000,000 VNĐ - QĐ 2345/QĐ-NHNN)
* **Ý nghĩa**: Đảm bảo tuân thủ quy định Ngân hàng Nhà nước. Hệ thống `verifyTransactionAuth()` kiểm tra ngưỡng `FACEID_HIGH_VALUE_THRESHOLD = 10,000,000 VNĐ`. Mọi giao dịch từ 10 triệu trở lên bắt buộc có FaceID token sinh trắc học hợp lệ, lưu trong Redis TTL **5 phút** (`FACEID_TOKEN_TTL_SECONDS = 300`), chỉ dùng **1 lần** (`consumeToken()`). Lưu ý: giới hạn tối đa **50,000,000 VNĐ/giao dịch**.
* **Các kịch bản Input**:
  * **Case 6.1**: Chuyển `15,000,000 VNĐ` nhưng không gửi `faceIdToken` (hoặc gửi `null`/rỗng).
  * **Case 6.2**: Chuyển `15,000,000 VNĐ` với `faceIdToken` giả mạo (`"fake-token-abc"`).
  * **Case 6.3 (Token Replay)**: Dùng lại `faceIdToken` đã được sử dụng ở giao dịch trước (đã bị `consumeToken()` xóa khỏi Redis).
  * **Case 6.4**: User chưa hoàn tất đăng ký eKYC 5 góc mặt (`ekycStatus != "VERIFIED"`) gọi chuyển `10,000,000 VNĐ`.
* **Đầu ra mong đợi (Expected Output)**:
  * Case 6.1: `400 Bad Request` — `FACEID_REQUIRED` (*"Giao dich nay yeu cau xac thuc FaceID. Vui long xac thuc khuon mat truoc."*)
  * Case 6.2 & 6.3: `400 Bad Request` — `FACEID_TOKEN_INVALID` (*"Ma xac thuc FaceID khong hop le hoac da het han (5 phut). Vui long xac thuc lai."*)
  * Case 6.4: `400 Bad Request` — `FACEID_NOT_REGISTERED` (*"Ban chua dang ky sinh trac hoc (eKYC). Vui long hoan tat eKYC truoc khi thuc hien giao dich cao."*)

---

#### 📌 Case 7: Tự chuyển tiền cho chính mình (Self-Transfer)
* **Ý nghĩa**: Ngăn chặn lỗi logic nghiệp vụ khi user tự chuyển cho chính mình gây tốn tài nguyên và kẹt số dư.
* **Thông tin đầu vào (Input)**:
  * User đang đăng nhập có số điện thoại `0912345678`.
  * Gọi `POST /private/api/v1/wallet/transfer` với `recipientPhoneNumber: "0912345678"`.
* **Đầu ra mong đợi (Expected Output)**:
  * HTTP `400 Bad Request`.
  * Error code: `SELF_WALLET_TRANSFER` — *"Ban khong the chuyen tien cho chinh minh."* (Hệ thống kiểm tra trong `findTransferRecipient()`: nếu `recipient.getId().equals(userId)` thì ném lỗi.)

---

#### 📌 Case 8: Chuyển tiền tới tài khoản bị KHÓA (LOCKED) hoặc VÍ RỦI RO (RISK)
* **Ý nghĩa**: Đảm bảo tài khoản gian lận hoặc ví bị đóng băng không thể tiếp nhận dòng tiền tẩu tán.
* **Thông tin đầu vào (Input)**:
  * **Case 8a**: Gửi lệnh chuyển tiền tới số điện thoại của user đang có `status = LOCKED`.
  * **Case 8b**: Gửi lệnh chuyển tiền tới số điện thoại của user có ví `status = RISK`.
* **Đầu ra mong đợi (Expected Output)**:
  * Case 8a: `400 Bad Request` — `USER_NOT_ACTIVE` (*"Tai khoan cua ban chua duoc kich hoat hoac da bi khoa."*) — kiểm tra qua `ensureActiveUser()`.
  * Case 8b: `400 Bad Request` — `RECIPIENT_WALLET_RISK` (*"Khong the chuyen tien den tai khoan nay do tai khoan nguoi nhan da bi vi pham quy dinh."*) — kiểm tra qua `ensureRecipientWalletNotRisk()`.

---

### NHÓM 3: BRUTE-FORCE, RATE LIMITING & TOKEN LIFECYCLE

---

#### 📌 Case 9: Brute-Force Password & Tự động khóa tài khoản sau 5 lần thất bại
* **Ý nghĩa**: Kiểm tra cơ chế chống tấn công từ điển mật khẩu và quy trình tự động gửi email kèm mã mở khóa (`unlockCode`).
* **Thông tin đầu vào (Input)**:
  * Gửi liên tiếp 5 lần request `POST /public/api/v1/auth/login` với mật khẩu sai.
* **Đầu ra mong đợi (Expected Output)**:
  * Lần 1–4: HTTP `400 Bad Request`, `INVALID_CREDENTIALS`, trường `remainingLoginAttempts` giảm dần (4 → 3 → 2 → 1). Constant: `MAX_FAILED_LOGIN_ATTEMPTS = 5`.
  * Lần 5: HTTP `400 Bad Request`, `ACCOUNT_LOCKED` (*"Tai khoan bi khoa do nhap sai mat khau qua nhieu lan. Ma mo khoa da duoc gui toi email cua ban."*), `remainingLoginAttempts: 0`. Hệ thống gọi `accountUnlockCodeService.createCode()` + `authMailService.sendAccountUnlockCode()`.
  * Lần 6 (nhập đúng mật khẩu nhưng thiếu `verificationCode`): Vẫn bị từ chối `ACCOUNT_LOCKED` cho đến khi nhập đúng `unlockCode` từ email.

---

#### 📌 Case 10: Tái sử dụng Token sau khi đã Logout (JWT Blacklist Replay)
* **Ý nghĩa**: Đảm bảo cơ chế Token Blacklist lưu trên Redis hoạt động chuẩn xác, vô hiệu hóa session ngay khi user đăng xuất dù JWT chưa hết hạn TTL.
* **Thông tin đầu vào (Input)**:
  * **Bước 1**: Đăng nhập lấy `jwt_token`.
  * **Bước 2**: Gọi `POST /private/api/v1/auth/logout` với header `Authorization: Bearer <jwt_token>`.
  * **Bước 3**: Tiếp tục dùng lại `<jwt_token>` đó để gọi `GET /private/api/v1/wallet`.
* **Đầu ra mong đợi (Expected Output)**:
  * Bước 2: HTTP `200 OK`.
  * Bước 3: HTTP `401 Unauthorized`. Hệ thống lưu key `auth:blacklist:<token>` vào Redis; bộ lọc JWT từ chối mọi request dùng token đã blacklist.

---

### NHÓM 4: ANTI-FRAUD HEADER SPOOFING & DISTRIBUTED TIMEOUT

---

#### 📌 Case 11: Kích hoạt Fraud Alert qua Device Anomaly — Mô phỏng tấn công chiếm đoạt tài khoản (ATO)
* **Ý nghĩa**: Kiểm tra **Fraud Detection Engine** (`fraud_detection/app_server.py`) phát hiện hành vi gian lận qua **6 Rule nghiệp vụ** và **3-Tier AI Models** (LightGBM + AutoEncoder + GraphSAGE). Test case kích hoạt `RULE-DEVICE-ANOMALY` (+25) kết hợp `RULE-STR-EW-04` (+35) để đẩy `final_risk_score >= 50` (MEDIUM) hoặc `>= 80` (HIGH).

  > **Lý do thay đổi so với thiết kế ban đầu (Impossible Travel)**:  
  > Fraud Engine hiện tại xử lý **stateless** — không truy vấn lịch sử vị trí GPS từ DB, chưa có rule `RULE-IMPOSSIBLE-TRAVEL`. Gửi 2 request từ 2 IP khác nhau với số tiền nhỏ **không đủ** kích hoạt cảnh báo HIGH (điểm chỉ đạt LOW ≈18/100). Test đúng đắn cần kết hợp nhiều rule theo cơ chế thực tế.

* **Bảng 6 Rule nghiệp vụ hiện có trong Fraud Engine**:

  | Rule Code | Điều kiện kích hoạt | Penalty |
  | :--- | :--- | :---: |
  | `RULE-DEVICE-ANOMALY` | `deviceFingerprint` chứa chuỗi `"unknown"` | +25 |
  | `RULE-STR-EW-04` | `amount / oldBalance >= 80%` + type TRANSFER/CASHOUT/WALLET_TRANSFER_OUT | +35 |
  | `RULE-RAPID-INOUT` | `amount / oldBalance >= 80%` + type CASHOUT/WALLET_TO_BANK | +30 |
  | `RULE-NIGHT-HIGH-AMT` | Giờ giao dịch 02:00–05:00 sáng AND `amount > 50,000,000` | +15 |
  | `RULE-STR-EW-05` | `fanIn24hCount >= 5` (nhận tiền từ >= 5 ví khác trong 24h) | +25 |
  | `RULE-STR-EW-06` | `fanOut24hCount >= 5` (chuyển tiền đến >= 5 ví khác trong 24h) | +25 |

* **Kịch bản A — HIGH Risk (Giả lập ATO: rút sạch tài khoản từ thiết bị lạ)**:
  * **Endpoint**: `POST /private/api/v1/wallet/bank-withdraw`
  * **Headers**:
    ```
    Authorization: Bearer <JWT_USER>
    X-Device-Fingerprint: unknown-device-xyz-hacker
    X-Forwarded-For: 1.2.3.4
    X-Geo-Location: 10.762622,106.660172
    X-Device-Id: device-unknown-hacker
    ```
  * **Payload** (số dư ví cần ≈ 50,000,000 VNĐ):
    ```json
    {
      "linkedBankAccountId": "<LINKED_BANK_ACCOUNT_ID>",
      "amount": 48000000,
      "pin": "123456",
      "idempotencyKey": "ATO-TEST-001"
    }
    ```
  * **Cơ chế kích hoạt**: `48M / 50M = 96% >= 80%` → `RULE-STR-EW-04 (+35)` + `RULE-DEVICE-ANOMALY (+25)` = **Rule Score 60**.
  * **Ước tính điểm**: `final_risk = 0.4 × 60 + 0.6 × AI_Score` → **≈ 42+ (MEDIUM)**; nếu AI đánh giá cao → **HIGH**.

* **Kịch bản B — MEDIUM Risk (Fan-Out Network)**:
  * Thực hiện >= 5 giao dịch `POST /private/api/v1/wallet/transfer` tới 5 `recipientPhoneNumber` khác nhau trong 24h.
  * Giao dịch thứ 6 → `fanOut24hCount >= 5` → Fraud Engine kích hoạt `RULE-STR-EW-06 (+25)`.
  * **Kết quả mong đợi trong `fraud_alerts`**: `risk_level = MEDIUM`, `triggered_rules` chứa `RULE-STR-EW-06`.

* **Đầu ra mong đợi (Expected Output)**:
  * Giao dịch vẫn được xử lý bình thường (Fraud Detection là **async, non-blocking**).
  * Bảng `fraud_alerts` có bản ghi mới: `risk_level = MEDIUM/HIGH`, `triggered_rules != []`, `threat_warnings` chứa cảnh báo `⚠️`/`🚨`, `final_risk_score > 30`.
  * Xác nhận bằng SQL:
    ```sql
    SELECT risk_level, final_risk_score, triggered_rules, threat_warnings, created_at
    FROM fraud_alerts
    ORDER BY created_at DESC
    LIMIT 5;
    ```

---

#### 📌 Case 12: Xử lý Giao dịch treo và Hoàn tiền tự động (Pending Transaction Timeout Job)
* **Ý nghĩa**: Khi PaymentGateway hoặc CoreService không gửi lại callback Kafka, tiền bị kẹt ở `holdBalance`. `PendingTransactionTimeoutJob` chạy theo `timeout-check-interval-ms` (mặc định 5 phút/lần), quét giao dịch `PENDING` quá `pending-timeout-minutes` (mặc định 30 phút), rồi `PendingTransactionTimeoutProcessor.expireSingleTransaction()` xử lý:
  * `WALLET_TRANSFER_OUT`: Hoàn `holdBalance` → `availableBalance` cho sender + đánh `TIMED_OUT` cả leg `TRANSFER_IN` cùng `referenceCode`.
  * `WALLET_TO_BANK`: Hoàn `holdBalance` → `availableBalance`.
  * `BANK_TO_WALLET`: Chỉ đánh `TIMED_OUT`, không có hold.
  * Gửi email thông báo timeout tới user.
* **Thông tin đầu vào (Input)**:
  * Tạo giao dịch chuyển tiền `200,000 VNĐ` (`status = PENDING`, `holdBalance = 200,000 VNĐ`).
  * Cập nhật thủ công `created_at` về > 30 phút trước để giả lập timeout:
    ```sql
    UPDATE wallet_transactions
    SET created_at = NOW() - INTERVAL '35 minutes'
    WHERE idempotency_key = 'TEST-TIMEOUT-001';
    ```
  * Đợi Batch Job chạy (tối đa 5 phút).
* **Đầu ra mong đợi (Expected Output)**:
  * Giao dịch đổi trạng thái sang `TIMED_OUT`.
  * `holdBalance` giảm 200,000 VNĐ, `availableBalance` tăng hoàn lại 200,000 VNĐ.
  * User nhận được email thông báo giao dịch timeout và đã hoàn tiền.

---

## 4. HƯỚNG DẪN THỰC HÀNH KIỂM THỬ QUA POSTMAN

### Bước 1: Thiết lập Postman Environment
1. Vào **Environments** > Tạo environment mới tên `Fintech-Local`.
2. Thêm các biến:
   * `base_url`: `http://localhost:8082/fintech-service`
   * `token_user_a`: (Token JWT sau khi login User A)
   * `token_user_b`: (Token JWT sau khi login User B)

### Bước 2: Tự động sinh Idempotency Key ngẫu nhiên trong Pre-request Script
Trong tab **Pre-request Script** của request `POST /transfer`, dán đoạn code sau để mỗi lần nhấn **Send** sẽ tự sinh 1 UUID mới:
```javascript
pm.environment.set("current_idempotency_key", "IDEM-" + pm.variables.replaceIn('{{$guid}}'));
```

Trong body request:
```json
{
  "recipientPhoneNumber": "0987654321",
  "amount": 50000,
  "pin": "123456",
  "idempotencyKey": "{{current_idempotency_key}}"
}
```

### Bước 3: Viết Test Script tự động kiểm tra Assertion
Dán vào tab **Tests** của request trong Postman:

```javascript
// Test status code
pm.test("Status code hợp lệ", function () {
    pm.expect(pm.response.code).to.be.oneOf([200, 400, 401]);
});

// Test chặn số tiền âm hoặc lỗi nghiệp vụ
if (pm.response.code === 400) {
    pm.test("Trả về đúng cấu trúc lỗi nghiệp vụ", function () {
        var jsonData = pm.response.json();
        pm.expect(jsonData).to.have.property("message");
    });
}
```

### Bước 4: Chạy kiểm thử Brute-Force bằng Postman Collection Runner
1. Chọn Folder chứa request `POST /public/api/v1/auth/login`.
2. Nhấn **Run Collection**.
3. Cài đặt **Iterations = 6**, **Delay = 200ms**.
4. Quan sát kết quả: Iteration 1–4 trả về sai mật khẩu + `remainingLoginAttempts` giảm, Iteration 5 trả về `ACCOUNT_LOCKED`, Iteration 6 bị từ chối truy cập.

### Bước 5: Kiểm thử Fraud Alert (Case 11)
1. Thiết lập header `X-Device-Fingerprint: unknown-device-xyz-hacker` trong Postman (tab Headers).
2. Gọi `POST /private/api/v1/wallet/bank-withdraw` với số tiền chiếm >= 80% số dư ví (ví dụ: số dư 50M, chuyển 48M).
3. Sau khi nhận `200 OK`, truy vấn DB kiểm tra:
```sql
SELECT risk_level, final_risk_score, triggered_rules, threat_warnings, created_at
FROM fraud_alerts
ORDER BY created_at DESC
LIMIT 5;
```

---

## 5. HƯỚNG DẪN THỰC HÀNH KIỂM THỬ BẰNG SCRIPT PYTHON ĐA LUỒNG

Để kiểm thử các ca khó như **Double-Spending** và **Concurrency Race**, chạy script Python sau để đảm bảo các request được gửi **hoàn toàn đồng thời**:

```python
"""
Script kiểm thử Concurrency & Double-Spending cho Fintech Service
Yêu cầu: pip install requests
"""

import concurrent.futures
import requests
import json
import uuid
import time

BASE_URL = "http://localhost:8082/fintech-service"

# Điền JWT Token của User A vào đây
JWT_TOKEN_A = "YOUR_JWT_TOKEN_HERE"

HEADERS = {
    "Authorization": f"Bearer {JWT_TOKEN_A}",
    "Content-Type": "application/json"
}

def send_transfer(req_index, amount, custom_idempotency_key=None):
    """Gửi 1 request chuyển tiền độc lập"""
    idem_key = custom_idempotency_key if custom_idempotency_key else f"RACE-{uuid.uuid4()}"
    
    payload = {
        "recipientPhoneNumber": "0987654321",
        "amount": amount,
        "pin": "123456",
        "idempotencyKey": idem_key
    }
    
    start = time.time()
    try:
        res = requests.post(
            f"{BASE_URL}/private/api/v1/wallet/transfer",
            headers=HEADERS,
            json=payload,
            timeout=10
        )
        elapsed = time.time() - start
        return {
            "req_index": req_index,
            "status_code": res.status_code,
            "response": res.json() if res.text else res.status_code,
            "elapsed": f"{elapsed:.3f}s"
        }
    except Exception as e:
        return {"req_index": req_index, "error": str(e)}

def test_double_spending():
    print("\n" + "="*60)
    print("🚀 BẮT ĐẦU TEST CASE 1: DOUBLE-SPENDING CONCURRENCY RACE")
    print("Mô phỏng 2 luồng bắn đồng thời mỗi lệnh 400,000 VNĐ (Số dư ví: 500,000 VNĐ)...")
    print("="*60)
    
    # Bắn song song 2 request cùng lúc
    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as executor:
        f1 = executor.submit(send_transfer, 1, 400000)
        f2 = executor.submit(send_transfer, 2, 400000)
        results = [f1.result(), f2.result()]

    for r in results:
        print(f"Request #{r.get('req_index')}: Status = {r.get('status_code')} | Thời gian: {r.get('elapsed')}")
        print(f"  Body: {json.dumps(r.get('response'), ensure_ascii=False)}")
        print("-" * 60)
        
    codes = [r.get("status_code") for r in results]
    if 200 in codes and 400 in codes:
        print("✅ KẾT QUẢ: PASS! Hệ thống chặn Double-Spending thành công (1 giao dịch duyệt, 1 bị từ chối).")
    elif codes.count(200) == 2:
        print("❌ KẾT QUẢ: FAILED! LỖI NGHIÊM TRỌNG! Cả 2 giao dịch đều thành công (Âm tiền ví).")
    else:
        print(f"⚠️ Trạng thái phản hồi: {codes}")

def test_duplicate_idempotency_race():
    print("\n" + "="*60)
    print("🚀 BẮT ĐẦU TEST CASE 2: DUPLICATE IDEMPOTENCY KEY CONCURRENCY")
    print("Mô phỏng 5 luồng bắn đồng thời với CÙNG 1 Key...")
    print("="*60)
    
    same_key = f"SAME-KEY-{uuid.uuid4()}"
    
    with concurrent.futures.ThreadPoolExecutor(max_workers=5) as executor:
        futures = [executor.submit(send_transfer, i, 50000, same_key) for i in range(1, 6)]
        results = [f.result() for f in futures]

    for r in results:
        print(f"Request #{r.get('req_index')}: Status = {r.get('status_code')}")
        
    print("✅ PASS: Hệ thống đảm bảo tính duy nhất, chỉ có 1 giao dịch được tạo ra trong Database.")

if __name__ == "__main__":
    test_double_spending()
    test_duplicate_idempotency_race()
```
