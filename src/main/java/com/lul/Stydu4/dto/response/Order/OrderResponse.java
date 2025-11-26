package com.lul.Stydu4.dto.response.Order;

import com.lul.Stydu4.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private String id;
    private String courseId;
    private String courseTitle;
    private String courseDescription;
    private BigDecimal amount;
    private PaymentStatus status;
    private String stripeSessionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
