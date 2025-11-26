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
import com.lul.Stydu4.service.ICartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements ICartService {

    private final ICartRepository cartRepository;
    private final ICourseRepository courseRepository;
    private final IUserRepository userRepository;
    private final IEnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public CartResponse addToCart(String userId, String courseId) {
        log.info("Adding course {} to cart for user {}", courseId, userId);
        
        // Check if user exists
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        log.debug("User found: {}", user.getUsername());
        
        // Check if course exists
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        
        log.debug("Course found: {}", course.getTitle());
        
        // Check if already enrolled
        boolean alreadyEnrolled = enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
        log.debug("Already enrolled check: {}", alreadyEnrolled);
        
        if (alreadyEnrolled) {
            log.warn("User {} already enrolled in course {}", userId, courseId);
            throw new AppException(ErrorCode.COURSE_ALREADY_PURCHASED);
        }
        
        // Check if already in cart
        boolean alreadyInCart = cartRepository.existsByUserIdAndCourseId(userId, courseId);
        log.debug("Already in cart check: {}", alreadyInCart);
        
        if (alreadyInCart) {
            log.warn("Course {} already in cart for user {}", courseId, userId);
            throw new AppException(ErrorCode.COURSE_ALREADY_IN_CART);
        }
        
        CartEntity cartItem = CartEntity.builder()
                .user(user)
                .course(course)
                .addedAt(LocalDateTime.now())
                .build();
        
        cartItem = cartRepository.save(cartItem);
        log.info("Added course {} to cart for user {}", courseId, userId);
        
        return mapToResponse(cartItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartResponse> getCartItems(String userId) {
        List<CartEntity> cartItems = cartRepository.findByUserId(userId);
        return cartItems.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeFromCart(String userId, String courseId) {
        cartRepository.deleteByUserIdAndCourseId(userId, courseId);
        log.info("Removed course {} from cart for user {}", courseId, userId);
    }

    @Override
    @Transactional
    public void clearCart(String userId) {
        cartRepository.deleteByUserId(userId);
        log.info("Cleared cart for user {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalPrice(String userId) {
        List<CartEntity> cartItems = cartRepository.findByUserId(userId);
        return cartItems.stream()
                .map(item -> item.getCourse().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public int getCartCount(String userId) {
        return (int) cartRepository.findByUserId(userId).size();
    }

    private CartResponse mapToResponse(CartEntity cartItem) {
        CourseEntity course = cartItem.getCourse();
        return CartResponse.builder()
                .id(cartItem.getId())
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .courseDescription(course.getDescription())
                .courseImageUrl(course.getImageUrl())
                .coursePrice(course.getPrice())
                .addedAt(cartItem.getAddedAt())
                .build();
    }

}
