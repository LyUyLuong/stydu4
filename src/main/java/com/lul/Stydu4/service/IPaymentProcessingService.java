package com.lul.Stydu4.service;

/**
 * Service xử lý phần ghi DB sau khi Stripe đã xác nhận thanh toán.
 * Tách riêng khỏi PaymentServiceImpl để:
 * - Tránh self-invocation: @Transactional không hoạt động khi gọi method
 *   private/public trong cùng class qua "this" (Spring AOP proxy bypass).
 * - Cô lập transaction boundary: Stripe HTTP call nằm ngoài @Transactional.
 */
public interface IPaymentProcessingService {

    /**
     * Tạo orders + enrollments cho toàn bộ giỏ hàng của user.
     * @return true nếu xử lý thành công ít nhất 1 item, false nếu cart rỗng
     */
    boolean processCartCheckout(String userId, String sessionId, String paymentIntentId);

    /**
     * Cập nhật order PENDING → COMPLETED và tạo enrollment cho 1 khóa học.
     */
    boolean processSinglePurchase(String sessionId, String paymentIntentId, String userId);
}