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
public class RevenueAnalyticsResponse {
    private BigDecimal totalRevenue;
    private BigDecimal periodRevenue;
    private Long totalOrders;
    private Long periodOrders;
    private BigDecimal averageOrderValue;
    private BigDecimal growthRate;
    private List<DailyRevenueResponse> dailyRevenue;
    private List<MonthlyRevenueResponse> monthlyRevenue;
    private List<CourseRevenueResponse> topCourses;
}
