"""
Script kiểm thử Concurrency, Double-Spending & Deadlock cho Fintech Service
Yêu cầu: pip install requests
"""

import concurrent.futures
import requests
import json
import uuid
import time

BASE_URL = "http://13.213.32.9/fintech-service"

# ==============================================================================
# CẤU HÌNH THÔNG TIN 2 USER ĐỂ TEST
# ==============================================================================
USER_A = {
    "name": "DUC. LE HAI",
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJwZXJtaXNzaW9ucyI6W10sInJvbGVzIjpbIkFETUlOIl0sImZ1bGxOYW1lIjoiRFVDLiBMRSBIQUkiLCJ1c2VySWQiOiIwMTlmM2EyZS0xMjFkLTdiM2QtYmY1YS1mNGMzNzlkMTg5ODciLCJzdWIiOiJsaGR1YzIudHNAZ21haWwuY29tIiwiaWF0IjoxNzg2ODgyMzA5LCJleHAiOjE3ODY5Njg3MDl9.TdYXJs6KR9JQtXVA4AQL60vbZfhI9WkoPvt8kK7sQq4",
    "phone": "0971319316",
    "pin": "123456"
}

USER_B = {
    "name": "LE HAI DUC",
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiVVNFUiIsInBlcm1pc3Npb25zIjpbXSwicm9sZXMiOlsiVVNFUiJdLCJmdWxsTmFtZSI6IkxFIEhBSSBEVUMiLCJ1c2VySWQiOiIwMTllYWYzNi1kNjA3LTdjODAtODM0OS0xMjg3Yzk5YzViZGYiLCJzdWIiOiJ1bWJyZWxsYTJrNUBnbWFpbC5jb20iLCJpYXQiOjE3ODY4ODI1NTYsImV4cCI6MTc4Njk2ODk1Nn0.noijSCLNpzbdLjbxbZG9n7JYwee5mmWHm7cmEPaVUBE",
    "phone": "0123456789",
    "pin": "123456"
}

def send_transfer_custom(sender, recipient_phone, amount):
    """Gửi lệnh chuyển tiền từ sender tới recipient"""
    idem_key = f"DEADLOCK-TEST-{uuid.uuid4()}"
    headers = {
        "Authorization": f"Bearer {sender['token']}",
        "Content-Type": "application/json"
    }
    payload = {
        "recipientPhoneNumber": recipient_phone,
        "amount": amount,
        "pin": sender["pin"],
        "idempotencyKey": idem_key
    }
    
    start = time.time()
    try:
        res = requests.post(
            f"{BASE_URL}/private/api/v1/wallet/transfer",
            headers=headers,
            json=payload,
            timeout=15
        )
        elapsed = time.time() - start
        return {
            "sender": sender["name"],
            "recipient_phone": recipient_phone,
            "status_code": res.status_code,
            "response": res.json() if res.text else res.status_code,
            "elapsed": f"{elapsed:.3f}s"
        }
    except Exception as e:
        return {"sender": sender["name"], "error": str(e)}

def test_cross_transfer_deadlock():
    print("\n" + "="*70)
    print("🚀 BẮT ĐẦU TEST CASE 3: CROSS-TRANSFER DEADLOCK PREVENTION")
    print(f"Mô phỏng: User A chuyển cho User B VÀ User B chuyển cho User A cùng lúc...")
    print("="*70)
    
    amount_to_send = 50000  # Mỗi bên chuyển 50,000 VNĐ
    
    # Kích hoạt 2 luồng đồng thời
    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as executor:
        # Luồng 1: A chuyển cho B
        future_a_to_b = executor.submit(send_transfer_custom, USER_A, USER_B["phone"], amount_to_send)
        # Luồng 2: B chuyển cho A
        future_b_to_a = executor.submit(send_transfer_custom, USER_B, USER_A["phone"], amount_to_send)
        
        results = [future_a_to_b.result(), future_b_to_a.result()]

    print("\n📊 KẾT QUẢ PHẢN HỒI:")
    for r in results:
        print(f"👉 [{r.get('sender')} -> {r.get('recipient_phone')}]: Status = {r.get('status_code')} | Thời gian: {r.get('elapsed')}")
        print(f"   Body: {json.dumps(r.get('response'), ensure_ascii=False)}")
        print("-" * 70)
        
    status_codes = [r.get("status_code") for r in results]
    
    # Đánh giá
    if status_codes == [200, 200]:
        print("✅ PASS: Cả 2 giao dịch đều thành công (200 OK)! Không xảy ra Deadlock.")
    elif 500 in status_codes:
        print("❌ FAILED: Xảy ra lỗi 500 (Khả năng cao bị Deadlock detected ở DB).")
    else:
        print(f"⚠️ Trạng thái nhận được: {status_codes}")

if __name__ == "__main__":
    # Chạy test Deadlock
    test_cross_transfer_deadlock()
