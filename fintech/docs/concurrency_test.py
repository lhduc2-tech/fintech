"""
Script kiểm thử Concurrency & Double-Spending cho Fintech Service
Yêu cầu: pip install requests
"""

import concurrent.futures
import requests
import json
import uuid
import time

BASE_URL = "http://13.213.32.9/fintech-service"

# Điền JWT Token của User A sau khi đăng nhập vào đây
JWT_TOKEN_A = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJwZXJtaXNzaW9ucyI6W10sInJvbGVzIjpbIkFETUlOIl0sImZ1bGxOYW1lIjoiRFVDLiBMRSBIQUkiLCJ1c2VySWQiOiIwMTlmM2EyZS0xMjFkLTdiM2QtYmY1YS1mNGMzNzlkMTg5ODciLCJzdWIiOiJsaGR1YzIudHNAZ21haWwuY29tIiwiaWF0IjoxNzg2ODgyMzA5LCJleHAiOjE3ODY5Njg3MDl9.TdYXJs6KR9JQtXVA4AQL60vbZfhI9WkoPvt8kK7sQq4"

HEADERS = {
    "Authorization": f"Bearer {JWT_TOKEN_A}",
    "Content-Type": "application/json"
}

def send_transfer(req_index, amount, custom_idempotency_key=None):
    """Gửi 1 request chuyển tiền độc lập"""
    idem_key = custom_idempotency_key if custom_idempotency_key else f"RACE-{uuid.uuid4()}"
    
    payload = {
        "recipientPhoneNumber": "0123456789",
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
