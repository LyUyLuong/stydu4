package com.lul.Stydu4.service;

import com.lul.Stydu4.dto.response.Dashboard.DashboardStatsResponse;
import com.lul.Stydu4.dto.response.Dashboard.RevenueAnalyticsResponse;

import java.time.LocalDateTime;

public interface IDashboardService {
    DashboardStatsResponse getDashboardStats(LocalDateTime startDate, LocalDateTime endDate);
    RevenueAnalyticsResponse getRevenueAnalytics(LocalDateTime startDate, LocalDateTime endDate);
}
