package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.response.Cart.CartResponse;
import com.lul.Stydu4.entity.CartEntity;
import com.lul.Stydu4.entity.CourseEntity;
import com.lul.Stydu4.entity.UserEntity;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.repository.ICartRepository;
import com.lul.Stydu4.repository.ICourseRepository;
import com.lul.Stydu4.repository.IEnrollmentRepository;
import com.lul.Stydu4.repository.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartServiceImpl Tests")
class CartServiceImplTest {

    @Mock
    private ICartRepository cartRepository;

    @Mock
    private ICourseRepository courseRepository;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IEnrollmentRepository enrollmentRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private UserEntity user;
    private CourseEntity course;
    private CartEntity cartItem;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .id("user-123")
                .username("john_doe")
                .email("john@example.com")
                .build();

        course = CourseEntity.builder()
                .id("course-123")
                .title("TOEIC Preparation Course")
                .description("Complete TOEIC course")
                .price(new BigDecimal("99.99"))
                .imageUrl("https://example.com/image.jpg")
                .duration(30)
                .build();

        cartItem = CartEntity.builder()
                .id("cart-123")
                .user(user)
                .course(course)
                .addedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("addToCart Tests")
    class AddToCartTests {

        @Test
        @DisplayName("Should add course to cart successfully")
        void addToCart_ValidData_Success() {
            // GIVEN
            when(userRepository.findById("user-123")).thenReturn(Optional.of(user));
            when(courseRepository.findById("course-123")).thenReturn(Optional.of(course));
            when(enrollmentRepository.existsByUserIdAndCourseId("user-123", "course-123")).thenReturn(false);
            when(cartRepository.existsByUserIdAndCourseId("user-123", "course-123")).thenReturn(false);
            when(cartRepository.save(any(CartEntity.class))).thenReturn(cartItem);

            // WHEN
            CartResponse result = cartService.addToCart("user-123", "course-123");

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getCourseId()).isEqualTo("course-123");
            assertThat(result.getCourseTitle()).isEqualTo("TOEIC Preparation Course");
            assertThat(result.getCoursePrice()).isEqualTo(new BigDecimal("99.99"));

            ArgumentCaptor<CartEntity> captor = ArgumentCaptor.forClass(CartEntity.class);
            verify(cartRepository).save(captor.capture());
            
            CartEntity savedCart = captor.getValue();
            assertThat(savedCart.getUser()).isEqualTo(user);
            assertThat(savedCart.getCourse()).isEqualTo(course);
            assertThat(savedCart.getAddedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void addToCart_UserNotFound_ThrowException() {
            // GIVEN
            when(userRepository.findById("invalid-user")).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> cartService.addToCart("invalid-user", "course-123"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_EXISTED);

            verify(userRepository).findById("invalid-user");
            verify(cartRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when course not found")
        void addToCart_CourseNotFound_ThrowException() {
            // GIVEN
            when(userRepository.findById("user-123")).thenReturn(Optional.of(user));
            when(courseRepository.findById("invalid-course")).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> cartService.addToCart("user-123", "invalid-course"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_NOT_FOUND);

            verify(courseRepository).findById("invalid-course");
            verify(cartRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when course already purchased")
        void addToCart_AlreadyEnrolled_ThrowException() {
            // GIVEN
            when(userRepository.findById("user-123")).thenReturn(Optional.of(user));
            when(courseRepository.findById("course-123")).thenReturn(Optional.of(course));
            when(enrollmentRepository.existsByUserIdAndCourseId("user-123", "course-123")).thenReturn(true);

            // WHEN & THEN
            assertThatThrownBy(() -> cartService.addToCart("user-123", "course-123"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_ALREADY_PURCHASED);

            verify(enrollmentRepository).existsByUserIdAndCourseId("user-123", "course-123");
            verify(cartRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when course already in cart")
        void addToCart_AlreadyInCart_ThrowException() {
            // GIVEN
            when(userRepository.findById("user-123")).thenReturn(Optional.of(user));
            when(courseRepository.findById("course-123")).thenReturn(Optional.of(course));
            when(enrollmentRepository.existsByUserIdAndCourseId("user-123", "course-123")).thenReturn(false);
            when(cartRepository.existsByUserIdAndCourseId("user-123", "course-123")).thenReturn(true);

            // WHEN & THEN
            assertThatThrownBy(() -> cartService.addToCart("user-123", "course-123"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_ALREADY_IN_CART);

            verify(cartRepository).existsByUserIdAndCourseId("user-123", "course-123");
            verify(cartRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getCartItems Tests")
    class GetCartItemsTests {

        @Test
        @DisplayName("Should return all cart items for user")
        void getCartItems_Success() {
            // GIVEN
            CourseEntity course2 = CourseEntity.builder()
                    .id("course-456")
                    .title("Advanced TOEIC")
                    .description("Advanced course")
                    .price(new BigDecimal("149.99"))
                    .imageUrl("https://example.com/image2.jpg")
                    .build();

            CartEntity cartItem2 = CartEntity.builder()
                    .id("cart-456")
                    .user(user)
                    .course(course2)
                    .addedAt(LocalDateTime.now())
                    .build();

            when(cartRepository.findByUserId("user-123")).thenReturn(List.of(cartItem, cartItem2));

            // WHEN
            List<CartResponse> result = cartService.getCartItems("user-123");

            // THEN
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getCourseTitle()).isEqualTo("TOEIC Preparation Course");
            assertThat(result.get(1).getCourseTitle()).isEqualTo("Advanced TOEIC");

            verify(cartRepository).findByUserId("user-123");
        }

        @Test
        @DisplayName("Should return empty list when cart is empty")
        void getCartItems_EmptyCart() {
            // GIVEN
            when(cartRepository.findByUserId("user-123")).thenReturn(List.of());

            // WHEN
            List<CartResponse> result = cartService.getCartItems("user-123");

            // THEN
            assertThat(result).isEmpty();
            verify(cartRepository).findByUserId("user-123");
        }
    }

    @Nested
    @DisplayName("removeFromCart Tests")
    class RemoveFromCartTests {

        @Test
        @DisplayName("Should remove course from cart successfully")
        void removeFromCart_Success() {
            // GIVEN
            doNothing().when(cartRepository).deleteByUserIdAndCourseId("user-123", "course-123");

            // WHEN
            cartService.removeFromCart("user-123", "course-123");

            // THEN
            verify(cartRepository).deleteByUserIdAndCourseId("user-123", "course-123");
        }
    }

    @Nested
    @DisplayName("clearCart Tests")
    class ClearCartTests {

        @Test
        @DisplayName("Should clear all items from cart")
        void clearCart_Success() {
            // GIVEN
            doNothing().when(cartRepository).deleteByUserId("user-123");

            // WHEN
            cartService.clearCart("user-123");

            // THEN
            verify(cartRepository).deleteByUserId("user-123");
        }
    }

    @Nested
    @DisplayName("getTotalPrice Tests")
    class GetTotalPriceTests {

        @Test
        @DisplayName("Should calculate total price correctly")
        void getTotalPrice_Success() {
            // GIVEN
            CourseEntity course2 = CourseEntity.builder()
                    .id("course-456")
                    .title("Advanced TOEIC")
                    .price(new BigDecimal("149.99"))
                    .build();

            CartEntity cartItem2 = CartEntity.builder()
                    .id("cart-456")
                    .user(user)
                    .course(course2)
                    .build();

            when(cartRepository.findByUserId("user-123")).thenReturn(List.of(cartItem, cartItem2));

            // WHEN
            BigDecimal totalPrice = cartService.getTotalPrice("user-123");

            // THEN
            assertThat(totalPrice).isEqualByComparingTo(new BigDecimal("249.98"));
            verify(cartRepository).findByUserId("user-123");
        }

        @Test
        @DisplayName("Should return zero when cart is empty")
        void getTotalPrice_EmptyCart() {
            // GIVEN
            when(cartRepository.findByUserId("user-123")).thenReturn(List.of());

            // WHEN
            BigDecimal totalPrice = cartService.getTotalPrice("user-123");

            // THEN
            assertThat(totalPrice).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("getCartCount Tests")
    class GetCartCountTests {

        @Test
        @DisplayName("Should return correct cart count")
        void getCartCount_Success() {
            // GIVEN
            CartEntity cartItem2 = CartEntity.builder().id("cart-456").build();
            when(cartRepository.findByUserId("user-123")).thenReturn(List.of(cartItem, cartItem2));

            // WHEN
            int count = cartService.getCartCount("user-123");

            // THEN
            assertThat(count).isEqualTo(2);
            verify(cartRepository).findByUserId("user-123");
        }

        @Test
        @DisplayName("Should return zero when cart is empty")
        void getCartCount_EmptyCart() {
            // GIVEN
            when(cartRepository.findByUserId("user-123")).thenReturn(List.of());

            // WHEN
            int count = cartService.getCartCount("user-123");

            // THEN
            assertThat(count).isZero();
        }
    }
}
