package com.lul.Stydu4.controller;

import com.lul.Stydu4.dto.response.ApiResponse;
import com.lul.Stydu4.entity.UserEntity;
import com.lul.Stydu4.repository.IUserRepository;
import com.lul.Stydu4.service.IPaymentService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final IPaymentService paymentService;
    private final IUserRepository userRepository;

    @GetMapping("/verify/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> verifyPayment(@PathVariable String sessionId) {
        try {
            // Get current authenticated user
            UserEntity user = getCurrentUser();
            log.info("User {} attempting to verify payment session: {}", user.getUsername(), sessionId);
            
            // Verify and process payment with user context
            boolean success = paymentService.verifyAndProcessPayment(sessionId, user.getId());
            
            if (success) {
                log.info("Payment verification successful for user: {}, session: {}", 
                        user.getUsername(), sessionId);
            } else {
                log.warn("Payment verification failed for user: {}, session: {}", 
                        user.getUsername(), sessionId);
            }
            
            return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                    .result(success)
                    .build());
        } catch (StripeException e) {
            log.error("Stripe error during verification: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.<Boolean>builder()
                    .message("Lỗi xác minh thanh toán: " + e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("Error during payment verification: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.<Boolean>builder()
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
