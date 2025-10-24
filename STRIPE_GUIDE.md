# 💳 Hướng dẫn sử dụng Stripe Payment

## 1. Setup Stripe Account

1. Đăng ký tài khoản tại: https://dashboard.stripe.com/register
2. Vào **Developers > API Keys**
3. Copy **Secret key** (bắt đầu với `sk_test_...`)
4. Paste vào `application.yaml`:

```yaml
stripe:
  api-key: sk_test_YOUR_KEY_HERE
```

## 2. Flow thanh toán

### Bước 1: User chọn khóa học và bấm "Mua"

```bash
POST /courses/{courseId}/purchase
Authorization: Bearer {token}
```

**Response:**
```json
{
  "result": {
    "orderId": "order-uuid",
    "sessionId": "cs_test_...",
    "checkoutUrl": "https://checkout.stripe.com/c/pay/cs_test_...",
    "status": "PENDING"
  }
}
```

### Bước 2: Redirect user đến Stripe Checkout

Frontend nhận `checkoutUrl` và redirect user:

```javascript
window.location.href = response.result.checkoutUrl;
```

### Bước 3: User thanh toán trên Stripe

- User nhập thông tin card trên trang Stripe
- **Test cards:**
  - Success: `4242 4242 4242 4242`
  - Decline: `4000 0000 0000 0002`
  - Any future date, any CVC, any ZIP

### Bước 4: Stripe redirect về frontend

**Success:** `http://localhost:5500/payment/success?session_id={CHECKOUT_SESSION_ID}`

**Cancel:** `http://localhost:5500/payment/cancel`

### Bước 5: Frontend gọi API để xác nhận thanh toán

```bash
POST /courses/payment/capture
Authorization: Bearer {token}
Content-Type: application/json

{
  "sessionId": "cs_test_..." 
}
```

**Response khi thành công:**
```json
{
  "result": {
    "id": "enrollment-uuid",
    "courseId": "course-uuid",
    "courseTitle": "TOEIC 900+",
    "status": "ACTIVE",
    "enrolledAt": "2025-10-24T10:00:00",
    "expiresAt": "2026-10-24T10:00:00"
  }
}
```

## 3. Kiểm tra khóa học đã mua

```bash
GET /courses/my-courses
Authorization: Bearer {token}
```

## 4. Admin: Tạo khóa học

```bash
POST /courses
Authorization: Bearer {admin-token}
Content-Type: application/json

{
  "title": "TOEIC 900+ Complete Course",
  "description": "Khóa học TOEIC từ 0 đến 900+",
  "price": 99.99,
  "imageUrl": "https://example.com/image.jpg",
  "duration": 365,
  "testIds": ["test-id-1", "test-id-2"]
}
```

## 5. Admin: Publish khóa học

```bash
PUT /courses/{courseId}/publish
Authorization: Bearer {admin-token}
```

## 6. Database Schema

### Table: courses
- id, title, description, price, duration, is_published

### Table: orders
- id, user_id, course_id, amount, status
- stripe_session_id, stripe_payment_intent_id, stripe_customer_id

### Table: enrollments
- id, user_id, course_id, status
- enrolled_at, expires_at

## 7. Payment Status

- **PENDING**: Đang chờ thanh toán
- **COMPLETED**: Thanh toán thành công
- **FAILED**: Thanh toán thất bại
- **CANCELLED**: User hủy thanh toán
- **REFUNDED**: Đã hoàn tiền

## 8. Enrollment Status

- **ACTIVE**: Đang học (chưa hết hạn)
- **EXPIRED**: Hết hạn
- **CANCELLED**: Đã hủy

## 9. Lưu ý

- Stripe tự động expire session sau 24 giờ nếu chưa thanh toán
- Enrollment tự động expire sau `duration` days (check hàng ngày lúc 00:00)
- Test mode miễn phí, không tính phí thật
- Production cần switch sang `sk_live_...` key

## 10. Testing với Postman

1. Login để lấy token
2. Create course (admin)
3. Publish course (admin)
4. Purchase course → Lấy `checkoutUrl`
5. Mở `checkoutUrl` trong browser
6. Dùng test card `4242 4242 4242 4242`
7. Sau khi thanh toán xong, copy `session_id` từ URL
8. Gọi `/payment/capture` với `session_id`
9. Check `/my-courses` để xem enrollment

---

**Happy coding!** 🚀
