# 🔥 Stripe Payment - cURL Commands for Postman

## Prerequisites
**Lấy JWT Token trước:**
```bash
POST http://localhost:8080/auth/token
Body: { "username": "your_username", "password": "your_password" }
```

Copy `token` từ response và thay vào `{{TOKEN}}` ở các lệnh dưới.

---

## 1️⃣ Admin: Tạo khóa học mới

```bash
curl -X POST http://localhost:8080/courses \
  -H "Authorization: Bearer {{TOKEN}}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "TOEIC 900+ Complete Course",
    "description": "Khóa học TOEIC từ cơ bản đến nâng cao, đạt 900+ điểm",
    "price": 99.99,
    "imageUrl": "https://example.com/toeic-course.jpg",
    "duration": 365,
    "testIds": []
  }'
```

**Response:**
```json
{
  "code": 1000,
  "result": {
    "id": "course-uuid-here",
    "title": "TOEIC 900+ Complete Course",
    "description": "...",
    "price": 99.99,
    "duration": 365,
    "isPublished": false,
    "testCount": 0,
    "studentCount": 0
  }
}
```

**Copy `id` của course để dùng cho bước tiếp theo!**

---

## 2️⃣ Admin: Publish khóa học

```bash
curl -X PUT http://localhost:8080/courses/{{COURSE_ID}}/publish \
  -H "Authorization: Bearer {{TOKEN}}"
```

**Response:**
```json
{
  "code": 1000,
  "result": "Course published"
}
```

---

## 3️⃣ User: Xem danh sách khóa học

```bash
curl -X GET http://localhost:8080/courses \
  -H "Authorization: Bearer {{TOKEN}}"
```

---

## 4️⃣ User: Xem chi tiết khóa học

```bash
curl -X GET http://localhost:8080/courses/{{COURSE_ID}} \
  -H "Authorization: Bearer {{TOKEN}}"
```

---

## 5️⃣ User: Mua khóa học (Tạo Stripe Checkout Session)

```bash
curl -X POST http://localhost:8080/courses/{{COURSE_ID}}/purchase \
  -H "Authorization: Bearer {{TOKEN}}"
```

**Response:**
```json
{
  "code": 1000,
  "result": {
    "orderId": "f3e7a8b9-1234-5678-9abc-def012345678",
    "sessionId": "cs_test_a1XO0pSgteC57O1pSQcW44qHG3WmoXOTxO4oZ0q2W5iODRsAtv1XpS3Bus",
    "checkoutUrl": "https://checkout.stripe.com/c/pay/cs_test_...",
    "status": "PENDING"
  }
}
```

### 📌 Bước tiếp theo:
1. **Copy `checkoutUrl`** và mở trong browser
2. Dùng test card: `4242 4242 4242 4242`
3. Any future date, any CVC, any ZIP
4. Click "Pay"
5. Stripe sẽ redirect về: `http://localhost:5500/payment/success?session_id=cs_test_...`
6. **Copy `session_id`** từ URL để dùng ở bước 6

---

## 6️⃣ User: Xác nhận thanh toán và nhận enrollment

```bash
curl -X POST http://localhost:8080/courses/payment/capture \
  -H "Authorization: Bearer {{TOKEN}}" \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "cs_test_a1XO0pSgteC57O1pSQcW44qHG3WmoXOTxO4oZ0q2W5iODRsAtv1XpS3Bus"
  }'
```

**Response khi thành công:**
```json
{
  "code": 1000,
  "result": {
    "id": "enrollment-uuid",
    "userId": "user-uuid",
    "courseId": "course-uuid",
    "courseTitle": "TOEIC 900+ Complete Course",
    "status": "ACTIVE",
    "enrolledAt": "2025-10-24T14:30:00",
    "expiresAt": "2026-10-24T14:30:00"
  }
}
```

---

## 7️⃣ User: Xem khóa học đã mua

```bash
curl -X GET http://localhost:8080/courses/my-courses \
  -H "Authorization: Bearer {{TOKEN}}"
```

**Response:**
```json
{
  "code": 1000,
  "result": [
    {
      "id": "enrollment-uuid",
      "userId": "user-uuid",
      "courseId": "course-uuid",
      "courseTitle": "TOEIC 900+ Complete Course",
      "status": "ACTIVE",
      "enrolledAt": "2025-10-24T14:30:00",
      "expiresAt": "2026-10-24T14:30:00"
    }
  ]
}
```

---

## 8️⃣ Admin: Unpublish khóa học

```bash
curl -X PUT http://localhost:8080/courses/{{COURSE_ID}}/unpublish \
  -H "Authorization: Bearer {{TOKEN}}"
```

---

## 🧪 Stripe Test Cards

| Card Number | Scenario |
|-------------|----------|
| `4242 4242 4242 4242` | ✅ Payment succeeds |
| `4000 0000 0000 0002` | ❌ Payment declined |
| `4000 0000 0000 9995` | ❌ Insufficient funds |
| `4000 0000 0000 0069` | ❌ Card expired |

**Expiry:** Any future date (e.g., `12/34`)  
**CVC:** Any 3 digits (e.g., `123`)  
**ZIP:** Any 5 digits (e.g., `12345`)

---

## 📝 Import vào Postman

1. Mở Postman
2. Click **Import** > **Raw text**
3. Paste các cURL commands trên
4. Tạo Environment variable:
   - `TOKEN` = JWT token của bạn
   - `COURSE_ID` = ID của course vừa tạo

---

## 🔥 Complete Flow Testing

```
1. Login → Lấy token
2. Create course → Lấy courseId
3. Publish course
4. Purchase course → Lấy checkoutUrl
5. Mở browser, thanh toán với test card
6. Copy session_id từ URL redirect
7. Capture payment với session_id
8. Check my-courses → Thấy enrollment
```

---

**✅ Done! Happy testing!** 🚀
