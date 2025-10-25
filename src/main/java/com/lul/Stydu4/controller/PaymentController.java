package com.lul.Stydu4.controller;

import com.lul.Stydu4.dto.response.ApiResponse;
import com.lul.Stydu4.service.IPaymentService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final IPaymentService paymentService;

    @GetMapping("/verify/{sessionId}")
    public ResponseEntity<ApiResponse<Boolean>> verifyPayment(@PathVariable String sessionId) {
        try {
            boolean success = paymentService.verifyAndProcessPayment(sessionId);
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
}
