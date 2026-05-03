package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.response.Course.PaymentResponse;
import com.lul.Stydu4.entity.CourseEntity;
import com.lul.Stydu4.entity.OrderEntity;
import com.lul.Stydu4.entity.UserEntity;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.enums.PaymentStatus;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.repository.IEnrollmentRepository;
import com.lul.Stydu4.repository.IOrderRepository;
import com.lul.Stydu4.service.IPaymentProcessingService;            // CHANGED: thêm
import com.lul.Stydu4.service.IPaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
// REMOVED: import com.lul.Stydu4.entity.CartEntity;
// REMOVED: import com.lul.Stydu4.entity.EnrollmentEntity;
// REMOVED: import com.lul.Stydu4.enums.EnrollmentStatus;
// REMOVED: import com.lul.Stydu4.repository.ICartRepository;
// REMOVED: import com.lul.Stydu4.service.IEmailService;
// REMOVED: import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements IPaymentService {

    private final IOrderRepository orderRepository;
    private final IEnrollmentRepository enrollmentRepository;
    private final IPaymentProcessingService paymentProcessingService;   // CHANGED: thêm
    // REMOVED: private final ICartRepository cartRepository;
    // REMOVED: private final IEmailService emailService;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    // ─────────────────────────────────────────────────────────────
    //  createPayment — GIỮ NGUYÊN, không thay đổi
    // ─────────────────────────────────────────────────────────────
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResponse createPayment(UserEntity user, CourseEntity course) throws Exception {
        try {
            if (user.getIsBanned() != null && user.getIsBanned()) {
                log.warn("Banned user {} attempted to purchase course {}", user.getId(), course.getId());
                throw new AppException(ErrorCode.USER_BANNED);
            }
            if (course.getIsPublished() == null || !course.getIsPublished()) {
                log.warn("User {} attempted to purchase unpublished course {}", user.getId(), course.getId());
                throw new AppException(ErrorCode.COURSE_NOT_PUBLISHED);
            }
            if (course.getPrice() == null || course.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                log.error("Course {} has invalid price: {}", course.getId(), course.getPrice());
                throw new AppException(ErrorCode.INVALID_COURSE_PRICE);
            }
            if (enrollmentRepository.existsByUserIdAndCourseId(user.getId(), course.getId())) {
                log.warn("User {} already enrolled in course {}", user.getId(), course.getId());
                throw new AppException(ErrorCode.ENROLLMENT_ALREADY_EXISTS);
            }

            OrderEntity order = OrderEntity.builder()
                    .user(user)
                    .course(course)
                    .amount(course.getPrice())
                    .status(PaymentStatus.PENDING)
                    .build();
            order = orderRepository.save(order);

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("usd")
                                                    .setUnitAmount((long) (course.getPrice().doubleValue() * 100))
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(course.getTitle())
                                                                    .setDescription(course.getDescription())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .putMetadata("orderId", order.getId())
                    .putMetadata("userId", user.getId())
                    .putMetadata("courseId", course.getId())
                    .build();

            Session session = Session.create(params);

            order.setStripeSessionId(session.getId());
            orderRepository.save(order);

            log.info("Created Stripe payment session for order: {}", order.getId());

            return PaymentResponse.builder()
                    .orderId(order.getId())
                    .sessionId(session.getId())
                    .checkoutUrl(session.getUrl())
                    .status("PENDING")
                    .build();

        } catch (StripeException e) {
            log.error("Stripe error: {}", e.getMessage());
            throw new Exception("Lỗi khi tạo thanh toán Stripe: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  capturePayment — GIỮ NGUYÊN (không nằm trong scope báo cáo)
    // ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public PaymentResponse capturePayment(String sessionId) throws Exception {
        try {
            Session session = Session.retrieve(sessionId);
            OrderEntity order = orderRepository.findByStripeSessionId(sessionId)
                    .orElseThrow(() -> new Exception("Không tìm thấy order"));

            if (order.getStatus() == PaymentStatus.COMPLETED) {
                log.info("Order {} already completed. Idempotent request.", order.getId());
                return PaymentResponse.builder()
                        .orderId(order.getId())
                        .sessionId(session.getId())
                        .checkoutUrl(null)
                        .status("COMPLETED")
                        .build();
            }

            if ("paid".equals(session.getPaymentStatus())) {
                order.setStatus(PaymentStatus.COMPLETED);
                order.setStripePaymentIntentId(session.getPaymentIntent());
                orderRepository.save(order);
                log.info("Payment completed for order: {}", order.getId());
                return PaymentResponse.builder()
                        .orderId(order.getId())
                        .sessionId(session.getId())
                        .checkoutUrl(null)
                        .status("COMPLETED")
                        .build();
            } else {
                order.setStatus(PaymentStatus.FAILED);
                orderRepository.save(order);
                throw new Exception("Thanh toán chưa hoàn tất");
            }
        } catch (StripeException e) {
            log.error("Stripe capture error: {}", e.getMessage());
            throw new Exception("Lỗi khi xác nhận thanh toán: " + e.getMessage());
        }
    }

    @Override
    public CourseEntity getCourseFromOrder(String orderId) throws Exception {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new Exception("Không tìm thấy order"));
        return order.getCourse();
    }

    // ─────────────────────────────────────────────────────────────
    //  verifyAndProcessPayment — REFACTORED
    // ─────────────────────────────────────────────────────────────
    @Override
    // CHANGED: BỎ @Transactional ở đây — Stripe call không được nằm trong transaction.
    //          Transaction sẽ được mở bởi paymentProcessingService.processX() bên trong.
    public boolean verifyAndProcessPayment(String sessionId, String userId) throws StripeException {
        // [STEP 1] Stripe HTTP call — NGOÀI transaction để không giữ DB connection
        Session session;
        try {
            session = Session.retrieve(sessionId);
        } catch (StripeException e) {
            log.error("Stripe error during verification: {}", e.getMessage());
            throw e;
        }

        String metadataUserId = session.getMetadata().get("userId");
        String type = session.getMetadata().get("type");
        String paymentIntentId = session.getPaymentIntent();

        log.info("Verifying payment for session: {}, type: {}, requestUserId: {}, metadataUserId: {}",
                sessionId, type, userId, metadataUserId);

        // [STEP 2] Security check — chặn user verify thay user khác
        if (metadataUserId != null && !metadataUserId.equals(userId)) {
            log.error("Security violation: User {} attempted to verify payment for user {}",
                    userId, metadataUserId);
            // Giữ behavior cũ: throw StripeException 403 để controller cũ vẫn xử lý đúng
            throw new StripeException(
                    "Unauthorized: You cannot verify payments for other users",
                    null, null, 403, null) {};
        }

        // [STEP 3] Status check — Stripe phải xác nhận đã trả tiền
        if (!"paid".equals(session.getPaymentStatus())) {
            log.warn("Session {} payment not completed. Status: {}",
                    sessionId, session.getPaymentStatus());
            return false;
        }

        // [STEP 4] Delegate sang processing service — transaction được mở tại đây
        if ("cart_checkout".equals(type)) {
            return paymentProcessingService.processCartCheckout(userId, sessionId, paymentIntentId);
        } else {
            return paymentProcessingService.processSinglePurchase(sessionId, paymentIntentId, userId);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  REMOVED: processCartCheckout(...)  — chuyển sang PaymentProcessingServiceImpl
    //  REMOVED: processSinglePurchase(...) — chuyển sang PaymentProcessingServiceImpl
    // ─────────────────────────────────────────────────────────────
}
