package com.lul.Stydu4.dto.response.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentEnrollmentResponse {
    private String id;
    private String courseName;
    private String userName;
    private LocalDateTime enrolledAt;
    private String status;
}
