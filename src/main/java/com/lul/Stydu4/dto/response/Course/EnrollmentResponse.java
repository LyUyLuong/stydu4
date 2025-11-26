package com.lul.Stydu4.dto.response.Course;

import com.lul.Stydu4.enums.EnrollmentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentResponse {
    
    private String id;
    private String courseId;
    private String courseTitle;
    private String courseDescription;
    private String courseImageUrl;
    private BigDecimal coursePrice;
    private Integer courseDuration;
    private EnrollmentStatus status;
    private LocalDateTime enrolledAt;
    private LocalDateTime expiresAt;
}
