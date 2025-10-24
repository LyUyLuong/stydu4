package com.lul.Stydu4.dto.request.Course;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCaptureRequest {
    
    private String sessionId;    // Stripe Checkout Session ID
}
