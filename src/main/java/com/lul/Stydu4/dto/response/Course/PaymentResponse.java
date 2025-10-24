package com.lul.Stydu4.dto.response.Course;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    
    private String orderId;        // Order ID trong DB
    private String sessionId;      // Stripe Checkout Session ID
    private String checkoutUrl;    // URL để redirect user đến Stripe
    private String status;
}
