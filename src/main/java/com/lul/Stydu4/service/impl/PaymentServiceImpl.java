package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.response.Course.PaymentResponse;
import com.lul.Stydu4.entity.CourseEntity;
import com.lul.Stydu4.entity.OrderEntity;
import com.lul.Stydu4.entity.UserEntity;
import com.lul.Stydu4.enums.PaymentStatus;
import com.lul.Stydu4.repository.IOrderRepository;
import com.lul.Stydu4.service.IPaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements IPaymentService {

    private final IOrderRepository orderRepository;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @Override
    @Transactional
    public PaymentResponse createPayment(UserEntity user, CourseEntity course) throws Exception {
        try {
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
}
