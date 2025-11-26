package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.response.Order.OrderResponse;
import com.lul.Stydu4.entity.CourseEntity;
import com.lul.Stydu4.entity.OrderEntity;
import com.lul.Stydu4.entity.UserEntity;
import com.lul.Stydu4.enums.PaymentStatus;
import com.lul.Stydu4.repository.IOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl Tests")
class OrderServiceImplTest {

    @Mock
    private IOrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private UserEntity user;
    private CourseEntity course;
    private OrderEntity order;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .id("user-123")
                .username("john_doe")
                .email("john@example.com")
                .build();

        course = CourseEntity.builder()
                .id("course-123")
                .title("TOEIC Complete Course")
                .description("Complete TOEIC preparation")
                .price(new BigDecimal("99.99"))
                .imageUrl("https://example.com/image.jpg")
                .duration(30)
                .build();

        order = OrderEntity.builder()
                .id("order-123")
                .user(user)
                .course(course)
                .amount(new BigDecimal("99.99"))
                .status(PaymentStatus.COMPLETED)
                .stripeSessionId("cs_test_123")
                .build();

        // Set audit fields manually since we can't use @CreatedDate in tests
        order.setCreatedDate(LocalDateTime.now());
        order.setModifiedDate(LocalDateTime.now());
    }

    @Nested
    @DisplayName("getUserOrders Tests")
    class GetUserOrdersTests {

        @Test
        @DisplayName("Should return all orders for user")
        void getUserOrders_Success() {
            // GIVEN
            CourseEntity course2 = CourseEntity.builder()
                    .id("course-456")
                    .title("Advanced TOEIC")
                    .description("Advanced course")
                    .price(new BigDecimal("149.99"))
                    .build();

            OrderEntity order2 = OrderEntity.builder()
                    .id("order-456")
                    .user(user)
                    .course(course2)
                    .amount(new BigDecimal("149.99"))
                    .status(PaymentStatus.COMPLETED)
                    .stripeSessionId("cs_test_456")
                    .build();
            order2.setCreatedDate(LocalDateTime.now());
            order2.setModifiedDate(LocalDateTime.now());

            when(orderRepository.findByUserId("user-123")).thenReturn(List.of(order, order2));

            // WHEN
            List<OrderResponse> result = orderService.getUserOrders("user-123");

            // THEN
            assertThat(result).hasSize(2);
            
            OrderResponse firstOrder = result.get(0);
            assertThat(firstOrder.getId()).isEqualTo("order-123");
            assertThat(firstOrder.getCourseTitle()).isEqualTo("TOEIC Complete Course");
            assertThat(firstOrder.getAmount()).isEqualByComparingTo(new BigDecimal("99.99"));
            assertThat(firstOrder.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(firstOrder.getStripeSessionId()).isEqualTo("cs_test_123");

            OrderResponse secondOrder = result.get(1);
            assertThat(secondOrder.getId()).isEqualTo("order-456");
            assertThat(secondOrder.getCourseTitle()).isEqualTo("Advanced TOEIC");
            assertThat(secondOrder.getAmount()).isEqualByComparingTo(new BigDecimal("149.99"));

            verify(orderRepository).findByUserId("user-123");
        }

        @Test
        @DisplayName("Should return empty list when user has no orders")
        void getUserOrders_EmptyList() {
            // GIVEN
            when(orderRepository.findByUserId("user-123")).thenReturn(List.of());

            // WHEN
            List<OrderResponse> result = orderService.getUserOrders("user-123");

            // THEN
            assertThat(result).isEmpty();
            verify(orderRepository).findByUserId("user-123");
        }

        @Test
        @DisplayName("Should map all order fields correctly")
        void getUserOrders_CorrectMapping() {
            // GIVEN
            order.setStatus(PaymentStatus.PENDING);
            when(orderRepository.findByUserId("user-123")).thenReturn(List.of(order));

            // WHEN
            List<OrderResponse> result = orderService.getUserOrders("user-123");

            // THEN
            assertThat(result).hasSize(1);
            
            OrderResponse orderResponse = result.get(0);
            assertThat(orderResponse.getId()).isEqualTo(order.getId());
            assertThat(orderResponse.getCourseId()).isEqualTo(course.getId());
            assertThat(orderResponse.getCourseTitle()).isEqualTo(course.getTitle());
            assertThat(orderResponse.getCourseDescription()).isEqualTo(course.getDescription());
            assertThat(orderResponse.getAmount()).isEqualByComparingTo(order.getAmount());
            assertThat(orderResponse.getStatus()).isEqualTo(order.getStatus());
            assertThat(orderResponse.getStripeSessionId()).isEqualTo(order.getStripeSessionId());
            assertThat(orderResponse.getCreatedAt()).isEqualTo(order.getCreatedDate());
            assertThat(orderResponse.getUpdatedAt()).isEqualTo(order.getModifiedDate());
        }

        @Test
        @DisplayName("Should handle different order statuses")
        void getUserOrders_DifferentStatuses() {
            // GIVEN
            OrderEntity pendingOrder = OrderEntity.builder()
                    .id("order-pending")
                    .user(user)
                    .course(course)
                    .amount(new BigDecimal("99.99"))
                    .status(PaymentStatus.PENDING)
                    .build();
            pendingOrder.setCreatedDate(LocalDateTime.now());
            pendingOrder.setModifiedDate(LocalDateTime.now());

            OrderEntity failedOrder = OrderEntity.builder()
                    .id("order-failed")
                    .user(user)
                    .course(course)
                    .amount(new BigDecimal("99.99"))
                    .status(PaymentStatus.FAILED)
                    .build();
            failedOrder.setCreatedDate(LocalDateTime.now());
            failedOrder.setModifiedDate(LocalDateTime.now());

            when(orderRepository.findByUserId("user-123"))
                    .thenReturn(List.of(order, pendingOrder, failedOrder));

            // WHEN
            List<OrderResponse> result = orderService.getUserOrders("user-123");

            // THEN
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(result.get(1).getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(result.get(2).getStatus()).isEqualTo(PaymentStatus.FAILED);
        }
    }
}
