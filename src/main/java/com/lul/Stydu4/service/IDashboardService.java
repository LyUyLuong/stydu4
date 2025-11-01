package com.lul.Stydu4.service;

import com.lul.Stydu4.dto.response.Dashboard.DashboardStatsResponse;

import java.time.LocalDateTime;

public interface IDashboardService {
    DashboardStatsResponse getDashboardStats(LocalDateTime startDate, LocalDateTime endDate);
}
