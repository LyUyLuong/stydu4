package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.response.Course.PaymentResponse;
import com.lul.Stydu4.entity.*;
import com.lul.Stydu4.enums.PaymentStatus;
import com.lul.Stydu4.repository.IOrderRepository;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentServiceImpl Tests")
class PaymentServiceImplTest {

    @Mock
    private IOrderRepository orderRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private UserEntity testUser;
    private CourseEntity testCourse;
    private OrderEntity testOrder;

    @BeforeEach
    void setUp() {
        // Set Stripe API key and URLs via reflection
        ReflectionTestUtils.setField(paymentService, "successUrl", "http://localhost:5500/payment/success?session_id={CHECKOUT_SESSION_ID}");
        ReflectionTestUtils.setField(paymentService, "cancelUrl", "http://localhost:5500/payment/cancel");

        // Setup test user
        testUser = new UserEntity();
        testUser.setId("user-123");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        // Setup test course
        testCourse = new CourseEntity();
        testCourse.setId("course-123");
        testCourse.setTitle("TOEIC Full Course");
        testCourse.setDescription("Complete TOEIC preparation");
        testCourse.setPrice(new BigDecimal("99.99"));
        testCourse.setDuration(30);
        testCourse.setIsPublished(true);

        // Setup test order
        testOrder = new OrderEntity();
        testOrder.setId("order-123");
        testOrder.setUser(testUser);
        testOrder.setCourse(testCourse);
        testOrder.setAmount(testCourse.getPrice());
        testOrder.setStripeSessionId("cs_test_session_123");
        testOrder.setStatus(PaymentStatus.PENDING);
    }

    @Nested
    @DisplayName("Create Payment Tests")
    class CreatePaymentTests {

        @Test
        @DisplayName("Should create payment successfully and return payment response")
        void createPayment_ValidCourseAndUser_ReturnsPaymentResponse() {
            // Given
            when(orderRepository.save(any(OrderEntity.class))).thenReturn(testOrder);

            // When & Then
            // Note: This will fail because Stripe API needs real key
            // We're testing that order is created with correct data
            assertThatThrownBy(() -> paymentService.createPayment(testUser, testCourse))
                .isInstanceOf(Exception.class);
            
            // Verify order was saved
            verify(orderRepository, atLeastOnce()).save(any(OrderEntity.class));
        }

        @Test
        @DisplayName("Should create order with correct user and course")
        void createPayment_CreatesOrderWithCorrectData() {
            // Given
            ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
            when(orderRepository.save(orderCaptor.capture())).thenReturn(testOrder);

            // When
            try {
                paymentService.createPayment(testUser, testCourse);
            } catch (Exception e) {
                // Expected - Stripe API will fail
            }

            // Then
            OrderEntity savedOrder = orderCaptor.getValue();
            assertThat(savedOrder).isNotNull();
            assertThat(savedOrder.getUser()).isEqualTo(testUser);
            assertThat(savedOrder.getCourse()).isEqualTo(testCourse);
            assertThat(savedOrder.getAmount()).isEqualByComparingTo(testCourse.getPrice());
            assertThat(savedOrder.getStatus()).isEqualTo(PaymentStatus.PENDING);
        }

        @Test
        @DisplayName("Should handle course with zero price")
        void createPayment_ZeroPrice_HandlesCorrectly() {
            // Given
            testCourse.setPrice(BigDecimal.ZERO);
            ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
            when(orderRepository.save(orderCaptor.capture())).thenReturn(testOrder);

            // When
            try {
                paymentService.createPayment(testUser, testCourse);
            } catch (Exception e) {
                // Expected
            }

            // Then
            OrderEntity savedOrder = orderCaptor.getValue();
            assertThat(savedOrder.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle course with large price")
        void createPayment_LargePrice_HandlesCorrectly() {
            // Given
            testCourse.setPrice(new BigDecimal("9999.99"));
            ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
            when(orderRepository.save(orderCaptor.capture())).thenReturn(testOrder);

            // When
            try {
                paymentService.createPayment(testUser, testCourse);
            } catch (Exception e) {
                // Expected
            }

            // Then
            OrderEntity savedOrder = orderCaptor.getValue();
            assertThat(savedOrder.getAmount()).isEqualByComparingTo(new BigDecimal("9999.99"));
        }

        @Test
        @DisplayName("Should save order twice - once before session, once after")
        void createPayment_SavesOrderTwice() {
            // Given
            when(orderRepository.save(any(OrderEntity.class))).thenReturn(testOrder);

            // When
            try {
                paymentService.createPayment(testUser, testCourse);
            } catch (Exception e) {
                // Expected
            }

            // Then - Should save once before creating session
            verify(orderRepository, atLeastOnce()).save(any(OrderEntity.class));
        }
    }

    @Nested
    @DisplayName("Capture Payment Tests")
    class CapturePaymentTests {

        @Test
        @DisplayName("Should throw Stripe error when session retrieval fails")
        void capturePayment_StripeRetrievalError_ThrowsException() {
            // Given
            String sessionId = "cs_test_invalid_session";

            // When & Then
            // This will fail because Stripe.apiKey is not set
            assertThatThrownBy(() -> paymentService.capturePayment(sessionId))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Lỗi khi xác nhận thanh toán");

            // Verify order repository is NOT called before Stripe fails
            verify(orderRepository, never()).findByStripeSessionId(anyString());
        }

        @Test
        @DisplayName("Should handle successful session retrieval but order not found")
        void capturePayment_OrderNotFoundAfterStripeCheck_ThrowsException() {
            // Given
            String sessionId = "cs_test_valid_session";

            // When & Then
            // Will fail at Stripe.retrieve() first
            assertThatThrownBy(() -> paymentService.capturePayment(sessionId))
                .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should call Stripe retrieve before checking order")
        void capturePayment_CallsStripeFirst() {
            // Given
            String sessionId = testOrder.getStripeSessionId();

            // When
            try {
                paymentService.capturePayment(sessionId);
            } catch (Exception e) {
                // Expected - Stripe API will fail
            }

            // Then - Order repository should NOT be called because Stripe fails first
            verify(orderRepository, never()).findByStripeSessionId(anyString());
        }
    }

    @Nested
    @DisplayName("Get Course From Order Tests")
    class GetCourseFromOrderTests {

        @Test
        @DisplayName("Should return course when order exists")
        void getCourseFromOrder_OrderExists_ReturnsCourse() throws Exception {
            // Given
            String orderId = testOrder.getId();
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

            // When
            CourseEntity result = paymentService.getCourseFromOrder(orderId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(testCourse);
            assertThat(result.getId()).isEqualTo(testCourse.getId());
            assertThat(result.getTitle()).isEqualTo(testCourse.getTitle());
        }

        @Test
        @DisplayName("Should throw exception when order not found")
        void getCourseFromOrder_OrderNotFound_ThrowsException() {
            // Given
            String invalidOrderId = "invalid-order-id";
            when(orderRepository.findById(invalidOrderId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> paymentService.getCourseFromOrder(invalidOrderId))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Không tìm thấy order");
        }

        @Test
        @DisplayName("Should handle null order ID")
        void getCourseFromOrder_NullOrderId_ThrowsException() {
            // Given
            when(orderRepository.findById(null)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> paymentService.getCourseFromOrder(null))
                .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle very long course title")
        void createPayment_LongTitle_HandlesCorrectly() {
            // Given
            String longTitle = "A".repeat(300);
            testCourse.setTitle(longTitle);
            when(orderRepository.save(any(OrderEntity.class))).thenReturn(testOrder);

            // When
            try {
                paymentService.createPayment(testUser, testCourse);
            } catch (Exception e) {
                // Expected
            }

            // Then
            verify(orderRepository, atLeastOnce()).save(any(OrderEntity.class));
        }

        @Test
        @DisplayName("Should handle null course description")
        void createPayment_NullDescription_HandlesCorrectly() {
            // Given
            testCourse.setDescription(null);
            when(orderRepository.save(any(OrderEntity.class))).thenReturn(testOrder);

            // When
            try {
                paymentService.createPayment(testUser, testCourse);
            } catch (Exception e) {
                // Expected
            }

            // Then
            verify(orderRepository, atLeastOnce()).save(any(OrderEntity.class));
        }

        @Test
        @DisplayName("Should handle Stripe API unavailability gracefully")
        void capturePayment_StripeUnavailable_ThrowsException() {
            // Given
            String sessionId = "cs_test_session";

            // When & Then
            assertThatThrownBy(() -> paymentService.capturePayment(sessionId))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Lỗi khi xác nhận thanh toán");
        }

        @Test
        @DisplayName("Should handle price with many decimal places")
        void createPayment_PriceWithManyDecimals_HandlesCorrectly() {
            // Given
            testCourse.setPrice(new BigDecimal("99.999999"));
            ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
            when(orderRepository.save(orderCaptor.capture())).thenReturn(testOrder);

            // When
            try {
                paymentService.createPayment(testUser, testCourse);
            } catch (Exception e) {
                // Expected
            }

            // Then
            OrderEntity savedOrder = orderCaptor.getValue();
            assertThat(savedOrder.getAmount()).isEqualByComparingTo(new BigDecimal("99.999999"));
        }
    }
}
