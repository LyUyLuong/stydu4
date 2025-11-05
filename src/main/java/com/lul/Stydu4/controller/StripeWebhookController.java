package com.lul.Stydu4.controller;

import com.lul.Stydu4.service.IPaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Stripe Webhook Controller - Xử lý events từ Stripe
 * 
 * Webhook này đảm bảo:
 * 1. Signature verification - Xác thực request từ Stripe
 * 2. Idempotency - Không xử lý trùng lặp event
 * 3. Error handling - Xử lý lỗi một cách an toàn
 * 4. Logging - Ghi log đầy đủ cho audit trail
 */
@RestController
@RequestMapping("/webhook/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final IPaymentService paymentService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    /**
     * Health check endpoint for webhook
     * GET /webhook/stripe/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        boolean webhookConfigured = webhookSecret != null && !webhookSecret.isEmpty();
        
        log.info("Webhook health check - Configured: {}", webhookConfigured);
        
        if (webhookConfigured) {
            return ResponseEntity.ok("Webhook is configured and ready");
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Webhook secret is not configured");
        }
    }

    /**
     * Handle Stripe webhook events
     * 
     * Stripe sẽ gửi các events quan trọng:
     * - checkout.session.completed: Khi thanh toán hoàn tất
     * - checkout.session.expired: Khi session hết hạn
     * - payment_intent.succeeded: Khi payment intent thành công
     * - payment_intent.payment_failed: Khi thanh toán thất bại
     */
    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        
        log.info("Received Stripe webhook");
        
        Event event;
        
        try {
            // 1. Verify webhook signature
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            log.info("Webhook signature verified for event: {}", event.getType());
            
        } catch (SignatureVerificationException e) {
            log.error("Invalid webhook signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            log.error("Error parsing webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook error");
        }

        // 2. Handle the event based on type
        try {
            switch (event.getType()) {
                case "checkout.session.completed":
                    handleCheckoutSessionCompleted(event);
                    break;
                    
                case "checkout.session.expired":
                    handleCheckoutSessionExpired(event);
                    break;
                    
                case "payment_intent.succeeded":
                    handlePaymentIntentSucceeded(event);
                    break;
                    
                case "payment_intent.payment_failed":
                    handlePaymentIntentFailed(event);
                    break;
                    
                default:
                    log.info("Unhandled event type: {}", event.getType());
            }
            
            return ResponseEntity.ok("Webhook processed");
            
        } catch (Exception e) {
            log.error("Error processing webhook event {}: {}", event.getType(), e.getMessage(), e);
            // Return 200 anyway to prevent Stripe from retrying indefinitely
            return ResponseEntity.ok("Webhook received but processing failed");
        }
    }

    /**
     * Handle checkout.session.completed event
     * Đây là event chính để xử lý thanh toán thành công
     */
    private void handleCheckoutSessionCompleted(Event event) {
        try {
            Session session = (Session) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new Exception("Failed to deserialize session"));
            
            log.info("===============================================");
            log.info("PROCESSING CHECKOUT SESSION COMPLETED");
            log.info("===============================================");
            log.info("Session ID: {}", session.getId());
            log.info("Payment Status: {}", session.getPaymentStatus());
            log.info("Amount Total: {}", session.getAmountTotal());
            log.info("Currency: {}", session.getCurrency());
            log.info("Customer Email: {}", session.getCustomerEmail());
            log.info("===============================================");
            
            // Process payment (với idempotency protection)
            boolean success = paymentService.verifyAndProcessPayment(session.getId());
            
            if (success) {
                log.info("Successfully processed checkout session: {}", session.getId());
            } else {
                log.error("Failed to process checkout session: {}", session.getId());
            }
            
        } catch (Exception e) {
            log.error("Error handling checkout.session.completed: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle checkout.session.expired event
     * Đánh dấu order là CANCELLED khi session hết hạn
     */
    private void handleCheckoutSessionExpired(Event event) {
        try {
            Session session = (Session) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new Exception("Failed to deserialize session"));
            
            log.info("Checkout session expired: {}", session.getId());
            
            // TODO: Update order status to CANCELLED
            // orderRepository.findByStripeSessionId(session.getId())
            //     .ifPresent(order -> {
            //         order.setStatus(PaymentStatus.CANCELLED);
            //         orderRepository.save(order);
            //     });
            
        } catch (Exception e) {
            log.error("Error handling checkout.session.expired: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle payment_intent.succeeded event
     * Backup event để confirm payment
     */
    private void handlePaymentIntentSucceeded(Event event) {
        try {
            log.info("Payment intent succeeded");
            // Additional processing if needed
            
        } catch (Exception e) {
            log.error("Error handling payment_intent.succeeded: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle payment_intent.payment_failed event
     * Đánh dấu order là FAILED khi thanh toán thất bại
     */
    private void handlePaymentIntentFailed(Event event) {
        try {
            log.warn("Payment intent failed");
            // TODO: Update order status to FAILED
            
        } catch (Exception e) {
            log.error("Error handling payment_intent.payment_failed: {}", e.getMessage(), e);
        }
    }
}
