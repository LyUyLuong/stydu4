package com.lul.Stydu4.dto.response.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private Long totalUsers;
    private Long totalTests;
    private Long totalCourses;
    private Long activeTests;
    private BigDecimal totalRevenue;
    private BigDecimal periodRevenue;
    private List<RecentTestResponse> recentTests;
    private List<CourseRevenueResponse> topCourses;
    private List<RecentEnrollmentResponse> recentEnrollments;
}
