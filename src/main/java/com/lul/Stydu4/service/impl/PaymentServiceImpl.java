package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.response.Course.PaymentResponse;
import com.lul.Stydu4.entity.CartEntity;
import com.lul.Stydu4.entity.CourseEntity;
import com.lul.Stydu4.entity.EnrollmentEntity;
import com.lul.Stydu4.entity.OrderEntity;
import com.lul.Stydu4.entity.UserEntity;
import com.lul.Stydu4.enums.EnrollmentStatus;
import com.lul.Stydu4.enums.PaymentStatus;
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
    @Transactional
    public PaymentResponse createPayment(UserEntity user, CourseEntity course) throws Exception {
        try {
            // Kiểm tra xem user đã mua khóa học này chưa
            if (enrollmentRepository.existsByUserIdAndCourseId(user.getId(), course.getId())) {
                log.warn("User {} already enrolled in course {}", user.getId(), course.getId());
                throw new Exception("Bạn đã mua khóa học này rồi!");
            }
            
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
    @Transactional
    public boolean verifyAndProcessPayment(String sessionId) throws StripeException {
        try {
            // Retrieve session from Stripe
            Session session = Session.retrieve(sessionId);
            
            // Get metadata
            String userId = session.getMetadata().get("userId");
            String type = session.getMetadata().get("type");
            
            log.info("Verifying payment for session: {}, type: {}, user: {}", sessionId, type, userId);
            
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
                return processSinglePurchase(sessionId, session);
            }
            
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
    
    private boolean processSinglePurchase(String sessionId, Session session) {
        try {
            // Find order by session ID
            OrderEntity order = orderRepository.findByStripeSessionId(sessionId)
                    .orElse(null);
            
            if (order == null) {
                log.warn("No order found for session: {}", sessionId);
                return false;
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
            
            log.info("Completed single purchase for order: {}", order.getId());
            
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
