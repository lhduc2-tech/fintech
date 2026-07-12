# Kiểm Thử API Giao Dịch Ví

Tài liệu này mô tả các test thủ công đơn giản cho 2 API:

- `POST /private/api/v1/wallet/transactions/search`
- `GET /private/api/v1/wallet/transactions/{id}`

Giả sử ứng dụng đang chạy tại:

```text
http://localhost:8082/fintech-service
```

Thay các giá trị sau bằng dữ liệu thật khi test:

```text
<JWT_TOKEN>          Token của user đang đăng nhập
<TRANSACTION_ID>    Id giao dịch thuộc user đang đăng nhập
<OTHER_USER_TX_ID>  Id giao dịch thuộc user khác
```

## 1. Test getTransactions có filter

API `getTransactions` nhận filter và pagination trong request body. Các field pagination kế thừa từ `BaseRequest`:

- `pageIndex`
- `pageSize`
- `sort`
- `direction`

Request:

```bash
curl --location 'http://localhost:8082/fintech-service/private/api/v1/wallet/transactions/search' \
  --header 'Authorization: Bearer <JWT_TOKEN>' \
  --header 'Content-Type: application/json' \
  --data '{
    "type": "WALLET_TRANSFER_OUT",
    "status": "SUCCESS",
    "from": "2026-06-01",
    "to": "2026-06-30",
    "pageIndex": 0,
    "pageSize": 10,
    "sort": "updatedAt",
    "direction": "DESC"
  }'
```

Kết quả mong đợi:

- HTTP `200 OK`.
- Response có field `content`.
- Mỗi item trong `content` chỉ là giao dịch của user đang đăng nhập.
- Mỗi item gồm các field chính:

```json
{
  "amount": 100000,
  "type": "WALLET_TRANSFER_OUT",
  "status": "SUCCESS",
  "updatedAt": "2026-06-20T08:00:00+07:00",
  "description": "Chuyen tien den 0912345678"
}
```

## 2. Test getTransactions không có giao dịch

Dùng khoảng ngày không có dữ liệu.

Request:

```bash
curl --location 'http://localhost:8082/fintech-service/private/api/v1/wallet/transactions/search' \
  --header 'Authorization: Bearer <JWT_TOKEN>' \
  --header 'Content-Type: application/json' \
  --data '{
    "from": "2099-01-01",
    "to": "2099-01-31",
    "pageIndex": 0,
    "pageSize": 10,
    "sort": "updatedAt",
    "direction": "DESC"
  }'
```

Kết quả mong đợi:

- HTTP `200 OK`.
- Response là page rỗng:

```json
{
  "size": 10,
  "totalElements": 0,
  "totalPages": 0,
  "content": []
}
```

## 3. Test getTransactions không truyền filter

Request:

```bash
curl --location 'http://localhost:8082/fintech-service/private/api/v1/wallet/transactions/search' \
  --header 'Authorization: Bearer <JWT_TOKEN>' \
  --header 'Content-Type: application/json' \
  --data '{
    "pageIndex": 0,
    "pageSize": 10,
    "sort": "updatedAt",
    "direction": "DESC"
  }'
```

Kết quả mong đợi:

- HTTP `200 OK`.
- Trả về danh sách giao dịch của user đang đăng nhập.
- Nếu user chưa có giao dịch thì `content` là mảng rỗng.

## 4. Test getTransactions với body rỗng

Request:

```bash
curl --location 'http://localhost:8082/fintech-service/private/api/v1/wallet/transactions/search' \
  --header 'Authorization: Bearer <JWT_TOKEN>' \
  --header 'Content-Type: application/json' \
  --data '{}'
```

Kết quả mong đợi:

- HTTP `200 OK`.
- API dùng giá trị mặc định từ `BaseRequest`:
  - `pageIndex = 0`
  - `pageSize = 10`
- Nếu không truyền `sort`, service mặc định sort theo `updatedAt DESC`.

## 5. Test getTransactionDetail thành công

Request:

```bash
curl --location 'http://localhost:8082/fintech-service/private/api/v1/wallet/transactions/<TRANSACTION_ID>' \
  --header 'Authorization: Bearer <JWT_TOKEN>'
```

Kết quả mong đợi:

- HTTP `200 OK`.
- Response trả về chi tiết giao dịch:

```json
{
  "fullName": "Nguyen Van A",
  "phoneNumber": "0901234567",
  "type": "WALLET_TRANSFER_OUT",
  "amount": 100000,
  "status": "SUCCESS",
  "updatedAt": "2026-06-20T08:00:00+07:00",
  "counterpartyFullName": "Tran Thi B",
  "counterpartyPhoneNumber": "0912345678"
}
```

Với giao dịch nạp tiền hoặc rút tiền qua ngân hàng, `counterpartyFullName` và `counterpartyPhoneNumber` có thể là `null` vì không có `counterparty_wallet_id`.

## 6. Test getTransactionDetail không tồn tại

Request:

```bash
curl --location 'http://localhost:8082/fintech-service/private/api/v1/wallet/transactions/00000000-0000-0000-0000-000000000000' \
  --header 'Authorization: Bearer <JWT_TOKEN>'
```

Kết quả mong đợi:

- HTTP `400 Bad Request`.
- Response:

```json
{
  "error": "TRANSACTION_NOT_FOUND",
  "message": "Transaction was not found.",
  "details": {}
}
```

## 7. Test không xem được giao dịch của user khác

Đăng nhập bằng user A, sau đó dùng id giao dịch của user B.

Request:

```bash
curl --location 'http://localhost:8082/fintech-service/private/api/v1/wallet/transactions/<OTHER_USER_TX_ID>' \
  --header 'Authorization: Bearer <JWT_TOKEN>'
```

Kết quả mong đợi:

- HTTP `400 Bad Request`.
- Response:

```json
{
  "error": "TRANSACTION_NOT_FOUND",
  "message": "Transaction was not found.",
  "details": {}
}
```

Lý do: API chi tiết giao dịch query theo cả `wallet_transactions.id` và `user_id` của user đang đăng nhập.
