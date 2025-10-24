package com.lul.Stydu4.dto.response.Course;

import com.lul.Stydu4.enums.EnrollmentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentResponse {
    
    private String id;
    private String courseId;
    private String courseTitle;
    private EnrollmentStatus status;
    private LocalDateTime enrolledAt;
    private LocalDateTime expiresAt;
}
