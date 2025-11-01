package com.lul.Stydu4.controller;

import com.lul.Stydu4.dto.response.ApiResponse;
import com.lul.Stydu4.dto.response.Dashboard.DashboardStatsResponse;
import com.lul.Stydu4.service.IDashboardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DashboardController {
    
    IDashboardService dashboardService;
    
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DashboardStatsResponse> getDashboardStats(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime startDate,
            
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime endDate
    ) {
        log.info("Getting dashboard stats from {} to {}", startDate, endDate);
        DashboardStatsResponse stats = dashboardService.getDashboardStats(startDate, endDate);
        return ApiResponse.<DashboardStatsResponse>builder()
                .result(stats)
                .build();
    }
}
