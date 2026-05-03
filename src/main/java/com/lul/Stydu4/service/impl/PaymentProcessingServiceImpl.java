package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.entity.CartEntity;
import com.lul.Stydu4.entity.EnrollmentEntity;
import com.lul.Stydu4.entity.OrderEntity;
import com.lul.Stydu4.enums.EnrollmentStatus;
import com.lul.Stydu4.enums.PaymentStatus;
import com.lul.Stydu4.repository.ICartRepository;
import com.lul.Stydu4.repository.IEnrollmentRepository;
import com.lul.Stydu4.repository.IOrderRepository;
import com.lul.Stydu4.service.IEmailService;
import com.lul.Stydu4.service.IPaymentProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessingServiceImpl implements IPaymentProcessingService {

    private final IOrderRepository orderRepository;
    private final IEnrollmentRepository enrollmentRepository;
    private final ICartRepository cartRepository;
    private final IEmailService emailService;

    // ============================================================
    //  CART CHECKOUT
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processCartCheckout(String userId, String sessionId, String paymentIntentId) {
        List<CartEntity> cartItems = cartRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            log.warn("No cart items found for user: {}", userId);
            return false;
        }

        // [FIX #5] Loại N+1 query: lấy 1 phát toàn bộ enrollment hiện có,
        // build Set<courseId> để check O(1) trong vòng lặp.
        Set<String> alreadyEnrolledCourseIds = enrollmentRepository.findByUserId(userId).stream()
                .map(e -> e.getCourse().getId())
                .collect(Collectors.toSet());

        List<OrderEntity> ordersToNotify = new ArrayList<>();

        for (CartEntity cartItem : cartItems) {
            String courseId = cartItem.getCourse().getId();

            if (alreadyEnrolledCourseIds.contains(courseId)) {
                log.info("User {} already enrolled in course {}. Skipping.", userId, courseId);
                continue;
            }

            OrderEntity order = OrderEntity.builder()
                    .user(cartItem.getUser())
                    .course(cartItem.getCourse())
                    .amount(cartItem.getCourse().getPrice())
                    .status(PaymentStatus.COMPLETED)
                    .stripeSessionId(sessionId)
                    .stripePaymentIntentId(paymentIntentId)
                    .build();
            order = orderRepository.save(order);

            EnrollmentEntity enrollment = EnrollmentEntity.builder()
                    .user(cartItem.getUser())
                    .course(cartItem.getCourse())
                    .status(EnrollmentStatus.ACTIVE)
                    .build();
            enrollmentRepository.save(enrollment);

            log.info("Created order and enrollment for course: {} - user: {}", courseId, userId);
            ordersToNotify.add(order);
        }

        cartRepository.deleteByUserId(userId);
        log.info("Cleared cart for user: {}", userId);

        // [FIX #1] Bắn email SAU khi commit, async — không giữ DB connection
        registerEmailNotifications(ordersToNotify);

        // [FIX #4] KHÔNG try/catch nuốt Exception. Mọi lỗi sẽ bubble up
        // để Spring rollback toàn bộ transaction → tránh data nửa-vời.
        return true;
    }

    // ============================================================
    //  SINGLE PURCHASE
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processSinglePurchase(String sessionId, String paymentIntentId, String userId) {
        OrderEntity order = orderRepository.findByStripeSessionId(sessionId).orElse(null);
        if (order == null) {
            log.warn("No order found for session: {}", sessionId);
            return false;
        }

        if (!order.getUser().getId().equals(userId)) {
            log.error("Security violation: User {} attempted to process order for user {}",
                    userId, order.getUser().getId());
            return false;
        }

        // Idempotent: order đã hoàn tất rồi
        if (order.getStatus() == PaymentStatus.COMPLETED) {
            log.info("Order {} already completed. Idempotent request.", order.getId());
            return true;
        }

        // Idempotent: enrollment đã tồn tại — chỉ cập nhật order status
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, order.getCourse().getId())) {
            log.warn("Enrollment already exists for user {} and course {}. Updating order status only.",
                    userId, order.getCourse().getId());
            order.setStatus(PaymentStatus.COMPLETED);
            order.setStripePaymentIntentId(paymentIntentId);
            orderRepository.save(order);
            return true;
        }

        order.setStatus(PaymentStatus.COMPLETED);
        order.setStripePaymentIntentId(paymentIntentId);
        OrderEntity savedOrder = orderRepository.save(order);

        enrollmentRepository.save(EnrollmentEntity.builder()
                .user(savedOrder.getUser())
                .course(savedOrder.getCourse())
                .status(EnrollmentStatus.ACTIVE)
                .build());

        log.info("Completed single purchase for order: {}, user: {}", savedOrder.getId(), userId);

        registerEmailNotifications(List.of(savedOrder));
        return true;
    }

    // ============================================================
    //  HELPER: gửi email SAU khi transaction commit thành công
    // ============================================================
    private void registerEmailNotifications(List<OrderEntity> orders) {
        if (orders.isEmpty()) return;

        // Pre-touch các field có thể lazy-loaded — đảm bảo đã nằm trong
        // persistence context trước khi session đóng.
        // Sau commit, entity ở trạng thái detached; field đã load vẫn truy cập được.
        orders.forEach(o -> {
            o.getUser().getEmail();
            o.getUser().getUsername();
            o.getCourse().getTitle();
            o.getCourse().getId();
            o.getAmount();
        });

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (OrderEntity o : orders) {
                    try {
                        emailService.sendPaymentConfirmationEmailAsync(o);
                        emailService.sendEnrollmentWelcomeEmailAsync(
                                o.getUser().getEmail(),
                                o.getCourse().getTitle());
                    } catch (Exception ex) {
                        // Lỗi async được xử lý bởi AsyncUncaughtExceptionHandler
                        // trong AsyncConfig. Catch ở đây chỉ để 1 email lỗi
                        // không kéo theo các email kế tiếp bị bỏ qua.
                        log.error("Failed to dispatch email for order {}: {}",
                                o.getId(), ex.getMessage());
                    }
                }
            }
        });
    }
}
