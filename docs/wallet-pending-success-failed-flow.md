# Wallet transaction PENDING/SUCCESS/FAILED flow

## Runtime behavior

The wallet APIs now create transaction records with `status = PENDING` before the external processor result is applied.

- `SUCCESS`: balances are updated, ledger entries are created, and transaction status becomes `SUCCESS`.
- `PENDING`: balances are not updated yet, no ledger entry is created, and transaction status remains `PENDING`.
- `FAILED`: balances are not updated, no ledger entry is created, and transaction status becomes `FAILED`.

Validation failures such as inactive wallet/account, receiver not found, self-transfer, or insufficient balance still return an API error and roll back the database transaction. They are not stored as `FAILED` transaction records because the business transaction was never accepted for processing.

## Test processor behavior

The current `SimulatedWalletTransactionProcessor` decides the result from `idempotencyKey`:

- Contains `PENDING`, case-insensitive: returns `PENDING`.
- Contains `FAILED`, case-insensitive: returns `FAILED`.
- Any other key: returns `SUCCESS`.

Example keys:

```text
topup-success-001
topup-pending-001
topup-failed-001
withdraw-success-001
transfer-pending-001
```

## Manual API examples

Top up success:

```bash
curl --location 'http://localhost:8082/fintech-service/private/api/v1/wallet/bank-topup' \
  --header 'Authorization: Bearer <JWT_TOKEN>' \
  --header 'Content-Type: application/json' \
  --data '{
    "linkedBankAccountId": "<LINKED_BANK_ACCOUNT_ID>",
    "amount": 25000,
    "idempotencyKey": "topup-success-001"
  }'
```

Top up pending:

```bash
curl --location 'http://localhost:8082/fintech-service/private/api/v1/wallet/bank-topup' \
  --header 'Authorization: Bearer <JWT_TOKEN>' \
  --header 'Content-Type: application/json' \
  --data '{
    "linkedBankAccountId": "<LINKED_BANK_ACCOUNT_ID>",
    "amount": 25000,
    "idempotencyKey": "topup-pending-001"
  }'
```

Withdraw failed:

```bash
curl --location 'http://localhost:8082/fintech-service/private/api/v1/wallet/bank-withdraw' \
  --header 'Authorization: Bearer <JWT_TOKEN>' \
  --header 'Content-Type: application/json' \
  --data '{
    "linkedBankAccountId": "<LINKED_BANK_ACCOUNT_ID>",
    "amount": 25000,
    "idempotencyKey": "withdraw-failed-001"
  }'
```

Wallet transfer pending:

```bash
curl --location 'http://localhost:8082/fintech-service/private/api/v1/wallet/transfer' \
  --header 'Authorization: Bearer <JWT_TOKEN>' \
  --header 'Content-Type: application/json' \
  --data '{
    "recipientPhoneNumber": "<RECEIVER_PHONE_NUMBER>",
    "amount": 25000,
    "idempotencyKey": "transfer-pending-001"
  }'
```

After each call, use transaction search with the matching status:

```bash
curl --location 'http://localhost:8082/fintech-service/private/api/v1/wallet/transactions/search' \
  --header 'Authorization: Bearer <JWT_TOKEN>' \
  --header 'Content-Type: application/json' \
  --data '{
    "status": "PENDING",
    "pageIndex": 0,
    "pageSize": 10
  }'
```

## Database table required for ledger

Because the application uses `spring.jpa.hibernate.ddl-auto=none`, create this table in the target database before testing successful wallet operations:

```sql
create table if not exists wallet_ledger_entries (
    id uuid primary key,
    transaction_id uuid not null references wallet_transactions(id),
    wallet_id uuid not null references wallets(id),
    type varchar(50) not null,
    amount numeric(19, 2) not null,
    balance_after numeric(19, 2) not null,
    description text,
    created_at timestamptz,
    created_by varchar(255),
    updated_at timestamptz,
    updated_by varchar(255)
);

comment on table wallet_ledger_entries is
    'Sổ cái ví: lưu từng bút toán làm thay đổi số dư ví sau khi giao dịch thành công.';

comment on column wallet_ledger_entries.id is
    'Khóa chính của bút toán ledger.';
comment on column wallet_ledger_entries.transaction_id is
    'Giao dịch ví sinh ra bút toán này. Tham chiếu wallet_transactions.id.';
comment on column wallet_ledger_entries.wallet_id is
    'Ví bị tác động bởi bút toán này. Tham chiếu wallets.id.';
comment on column wallet_ledger_entries.type is
    'Loại bút toán: CREDIT là cộng tiền vào ví, DEBIT là trừ tiền khỏi ví.';
comment on column wallet_ledger_entries.amount is
    'Số tiền được ghi nhận trong bút toán.';
comment on column wallet_ledger_entries.balance_after is
    'Số dư khả dụng của ví ngay sau khi áp dụng bút toán.';
comment on column wallet_ledger_entries.description is
    'Mô tả nghiệp vụ của bút toán, ví dụ nạp tiền, rút tiền, chuyển tiền.';
comment on column wallet_ledger_entries.created_at is
    'Thời điểm tạo bút toán.';
comment on column wallet_ledger_entries.created_by is
    'Người hoặc hệ thống tạo bút toán.';
comment on column wallet_ledger_entries.updated_at is
    'Thời điểm cập nhật bút toán gần nhất.';
comment on column wallet_ledger_entries.updated_by is
    'Người hoặc hệ thống cập nhật bút toán gần nhất.';
```

If the table already exists, only run the `comment on ...` statements to add descriptions.
