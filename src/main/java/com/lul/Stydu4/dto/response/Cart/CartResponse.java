package com.lul.Stydu4.dto.response.Cart;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {
    private String id;
    private String courseId;
    private String courseTitle;
    private String courseDescription;
    private String courseImageUrl;
    private BigDecimal coursePrice;
    private LocalDateTime addedAt;
}
