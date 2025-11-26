package com.lul.Stydu4.controller;

import com.lul.Stydu4.dto.response.ApiResponse;
import com.lul.Stydu4.dto.response.Cart.CartResponse;
import com.lul.Stydu4.dto.response.Course.PaymentResponse;
import com.lul.Stydu4.entity.CourseEntity;
import com.lul.Stydu4.entity.UserEntity;
import com.lul.Stydu4.repository.ICourseRepository;
import com.lul.Stydu4.repository.IUserRepository;
import com.lul.Stydu4.service.ICartService;
import com.lul.Stydu4.service.IPaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final ICartService cartService;
    private final ICourseRepository courseRepository;
    private final IUserRepository userRepository;
    
    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @PostMapping("/add/{courseId}")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(@PathVariable String courseId) {
        UserEntity user = getCurrentUser();
        CartResponse cart = cartService.addToCart(user.getId(), courseId);
        
        return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                .result(cart)
                .build());
    }

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<CartResponse>>> getCartItems() {
        UserEntity user = getCurrentUser();
        List<CartResponse> items = cartService.getCartItems(user.getId());
        
        return ResponseEntity.ok(ApiResponse.<List<CartResponse>>builder()
                .result(items)
                .build());
    }

    @DeleteMapping("/remove/{courseId}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(@PathVariable String courseId) {
        UserEntity user = getCurrentUser();
        cartService.removeFromCart(user.getId(), courseId);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Đã xóa khóa học khỏi giỏ hàng")
                .build());
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        UserEntity user = getCurrentUser();
        cartService.clearCart(user.getId());
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Đã xóa toàn bộ giỏ hàng")
                .build());
    }

    @GetMapping("/total")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalPrice() {
        UserEntity user = getCurrentUser();
        BigDecimal total = cartService.getTotalPrice(user.getId());
        
        return ResponseEntity.ok(ApiResponse.<BigDecimal>builder()
                .result(total)
                .build());
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> getCartCount() {
        UserEntity user = getCurrentUser();
        int count = cartService.getCartCount(user.getId());
        
        return ResponseEntity.ok(ApiResponse.<Integer>builder()
                .result(count)
                .build());
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<PaymentResponse>> checkout() {
        try {
            UserEntity user = getCurrentUser();
            List<CartResponse> cartItems = cartService.getCartItems(user.getId());
            
            if (cartItems.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.<PaymentResponse>builder()
                        .message("Giỏ hàng trống")
                        .build());
            }

            // Tạo line items cho Stripe
            List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
            for (CartResponse item : cartItems) {
                CourseEntity course = courseRepository.findById(item.getCourseId())
                        .orElseThrow(() -> new Exception("Không tìm thấy khóa học"));
                
                SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                        .setPriceData(
                                SessionCreateParams.LineItem.PriceData.builder()
                                        .setCurrency("usd")
                                        .setUnitAmount(course.getPrice().multiply(new BigDecimal("100")).longValue())
                                        .setProductData(
                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                        .setName(course.getTitle())
                                                        .setDescription(course.getDescription())
                                                        .build()
                                        )
                                        .build()
                        )
                        .setQuantity(1L)
                        .build();
                
                lineItems.add(lineItem);
            }

            // Tạo Stripe Checkout Session
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .addAllLineItem(lineItems)
                    .putMetadata("userId", user.getId())
                    .putMetadata("type", "cart_checkout")
                    .build();

            Session session = Session.create(params);

            PaymentResponse response = PaymentResponse.builder()
                    .sessionId(session.getId())
                    .checkoutUrl(session.getUrl())
                    .build();

            return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder()
                    .result(response)
                    .build());
                    
        } catch (StripeException e) {
            log.error("Stripe error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.<PaymentResponse>builder()
                    .message("Lỗi thanh toán: " + e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("Error during checkout: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.<PaymentResponse>builder()
                    .message(e.getMessage())
                    .build());
        }
    }

    private UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
