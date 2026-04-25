package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.response.Course.PaymentResponse;
import com.lul.Stydu4.entity.CartEntity;
import com.lul.Stydu4.entity.CourseEntity;
import com.lul.Stydu4.entity.EnrollmentEntity;
import com.lul.Stydu4.entity.OrderEntity;
import com.lul.Stydu4.entity.UserEntity;
import com.lul.Stydu4.enums.EnrollmentStatus;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.enums.PaymentStatus;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.repository.ICartRepository;
import com.lul.Stydu4.repository.IEnrollmentRepository;
import com.lul.Stydu4.repository.IOrderRepository;
import com.lul.Stydu4.service.IEmailService;
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
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements IPaymentService {

    private final IOrderRepository orderRepository;
    private final IEnrollmentRepository enrollmentRepository;
    private final ICartRepository cartRepository;
    private final IEmailService emailService;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResponse createPayment(UserEntity user, CourseEntity course) throws Exception {
        try {
            // ===== BUSINESS VALIDATION =====
            
            // 1. Validate user is not banned
            if (user.getIsBanned() != null && user.getIsBanned()) {
                log.warn("Banned user {} attempted to purchase course {}", user.getId(), course.getId());
                throw new AppException(ErrorCode.USER_BANNED);
            }
            
            // 2. Validate course is published
            if (course.getIsPublished() == null || !course.getIsPublished()) {
                log.warn("User {} attempted to purchase unpublished course {}", user.getId(), course.getId());
                throw new AppException(ErrorCode.COURSE_NOT_PUBLISHED);
            }

            // 3. Validate course price is valid
            if (course.getPrice() == null || course.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                log.error("Course {} has invalid price: {}", course.getId(), course.getPrice());
                throw new AppException(ErrorCode.INVALID_COURSE_PRICE);
            }

            // 4. Check if user already enrolled in this course
            if (enrollmentRepository.existsByUserIdAndCourseId(user.getId(), course.getId())) {
                log.warn("User {} already enrolled in course {}", user.getId(), course.getId());
                throw new AppException(ErrorCode.ENROLLMENT_ALREADY_EXISTS);
            }
            
            // ===== CREATE ORDER AND PAYMENT =====
            
            // Tạo order trong DB
            OrderEntity order = OrderEntity.builder()
                    .user(user)
                    .course(course)
                    .amount(course.getPrice())
                    .status(PaymentStatus.PENDING)
                    .build();
            order = orderRepository.save(order);

            // Tạo Stripe Checkout Session
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
                                                    .setUnitAmount((long) (course.getPrice().doubleValue() * 100)) // Convert to cents
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

            // Lưu Stripe Session ID
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

    @Override
    @Transactional
    public PaymentResponse capturePayment(String sessionId) throws Exception {
        try {
            // Lấy session từ Stripe
            Session session = Session.retrieve(sessionId);

            // Tìm order trong DB
            OrderEntity order = orderRepository.findByStripeSessionId(sessionId)
                    .orElseThrow(() -> new Exception("Không tìm thấy order"));

            // IDEMPOTENT CHECK: If already completed, return success immediately
            if (order.getStatus() == PaymentStatus.COMPLETED) {
                log.info("Order {} already completed. Idempotent request.", order.getId());
                return PaymentResponse.builder()
                        .orderId(order.getId())
                        .sessionId(session.getId())
                        .checkoutUrl(null)
                        .status("COMPLETED")
                        .build();
            }

            // Kiểm tra payment status
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean verifyAndProcessPayment(String sessionId, String userId) throws StripeException {
        try {
            // Retrieve session from Stripe
            Session session = Session.retrieve(sessionId);
            
            // Get metadata
            String metadataUserId = session.getMetadata().get("userId");
            String type = session.getMetadata().get("type");
            
            log.info("Verifying payment for session: {}, type: {}, requestUserId: {}, metadataUserId: {}", 
                    sessionId, type, userId, metadataUserId);
            
            // Security check: verify the user requesting verification owns this payment session
            if (metadataUserId != null && !metadataUserId.equals(userId)) {
                log.error("Security violation: User {} attempted to verify payment for user {}", 
                        userId, metadataUserId);
                throw new SecurityException("Unauthorized: You cannot verify payments for other users");
            }
            
            // Check if payment is successful
            if (!"paid".equals(session.getPaymentStatus())) {
                log.warn("Session {} payment not completed. Status: {}", sessionId, session.getPaymentStatus());
                return false;
            }
            
            if ("cart_checkout".equals(type)) {
                // Process cart checkout
                return processCartCheckout(userId, session);
            } else {
                // Process single course purchase
                return processSinglePurchase(sessionId, session, userId);
            }
            
        } catch (SecurityException e) {
            log.error("Security error during verification: {}", e.getMessage());
            throw new StripeException(e.getMessage(), null, null, 403, null) {};
        } catch (StripeException e) {
            log.error("Stripe error during verification: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error during payment verification: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean processCartCheckout(String userId, Session session) {
        try {
            // Get all cart items for user
            List<CartEntity> cartItems = cartRepository.findByUserId(userId);

            if (cartItems.isEmpty()) {
                log.warn("No cart items found for user: {}", userId);
                return false;
            }

            // Create orders and enrollments for each cart item
            for (CartEntity cartItem : cartItems) {
                // IDEMPOTENT CHECK: Skip if user already enrolled in this course
                if (enrollmentRepository.existsByUserIdAndCourseId(userId, cartItem.getCourse().getId())) {
                    log.info("User {} already enrolled in course {}. Skipping.", userId, cartItem.getCourse().getId());
                    continue;
                }

                // Create order
                OrderEntity order = OrderEntity.builder()
                        .user(cartItem.getUser())
                        .course(cartItem.getCourse())
                        .amount(cartItem.getCourse().getPrice())
                        .status(PaymentStatus.COMPLETED)
                        .stripeSessionId(session.getId())
                        .stripePaymentIntentId(session.getPaymentIntent())
                        .build();
                order = orderRepository.save(order);

                // Create enrollment
                EnrollmentEntity enrollment = EnrollmentEntity.builder()
                        .user(cartItem.getUser())
                        .course(cartItem.getCourse())
                        .status(EnrollmentStatus.ACTIVE)
                        .build();
                enrollmentRepository.save(enrollment);
                
                log.info("Created order and enrollment for course: {} - user: {}", 
                        cartItem.getCourse().getId(), userId);
                
                // Send confirmation email
                try {
                    emailService.sendPaymentConfirmationEmail(order);
                    emailService.sendEnrollmentWelcomeEmail(
                        cartItem.getUser().getEmail(), 
                        cartItem.getCourse().getTitle()
                    );
                } catch (Exception emailEx) {
                    log.error("Failed to send email for order {}: {}", order.getId(), emailEx.getMessage());
                }
            }
            
            // Clear cart
            cartRepository.deleteByUserId(userId);
            log.info("Cleared cart for user: {}", userId);
            
            return true;
            
        } catch (Exception e) {
            log.error("Error processing cart checkout: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean processSinglePurchase(String sessionId, Session session, String userId) {
        try {
            // Find order by session ID
            OrderEntity order = orderRepository.findByStripeSessionId(sessionId)
                    .orElse(null);

            if (order == null) {
                log.warn("No order found for session: {}", sessionId);
                return false;
            }

            // Security check: verify the user owns this order
            if (!order.getUser().getId().equals(userId)) {
                log.error("Security violation: User {} attempted to process order for user {}",
                        userId, order.getUser().getId());
                return false;
            }

            // IDEMPOTENT CHECK: If order already completed, return success
            if (order.getStatus() == PaymentStatus.COMPLETED) {
                log.info("Order {} already completed. Idempotent request.", order.getId());
                return true;
            }

            // IDEMPOTENT CHECK: If enrollment already exists, just update order status
            if (enrollmentRepository.existsByUserIdAndCourseId(userId, order.getCourse().getId())) {
                log.warn("Enrollment already exists for user {} and course {}. Updating order status only.",
                        userId, order.getCourse().getId());
                order.setStatus(PaymentStatus.COMPLETED);
                order.setStripePaymentIntentId(session.getPaymentIntent());
                orderRepository.save(order);
                return true;
            }

            // Update order status
            order.setStatus(PaymentStatus.COMPLETED);
            order.setStripePaymentIntentId(session.getPaymentIntent());
            order = orderRepository.save(order);

            // Create enrollment
            EnrollmentEntity enrollment = EnrollmentEntity.builder()
                    .user(order.getUser())
                    .course(order.getCourse())
                    .status(EnrollmentStatus.ACTIVE)
                    .build();
            enrollmentRepository.save(enrollment);
            
            log.info("Completed single purchase for order: {}, user: {}", order.getId(), userId);
            
            // Send confirmation email
            try {
                emailService.sendPaymentConfirmationEmail(order);
                emailService.sendEnrollmentWelcomeEmail(
                    order.getUser().getEmail(), 
                    order.getCourse().getTitle()
                );
            } catch (Exception emailEx) {
                log.error("Failed to send email for order {}: {}", order.getId(), emailEx.getMessage());
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("Error processing single purchase: {}", e.getMessage());
            return false;
        }
    }
}
