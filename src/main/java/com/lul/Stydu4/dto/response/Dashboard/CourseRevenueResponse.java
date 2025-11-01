package com.lul.Stydu4.dto.response.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseRevenueResponse {
    private String courseId;
    private String courseName;
    private BigDecimal revenue;
    private Long totalEnrollments;
    private BigDecimal price;
}
