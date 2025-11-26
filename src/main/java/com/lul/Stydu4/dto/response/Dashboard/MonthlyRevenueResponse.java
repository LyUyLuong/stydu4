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
public class MonthlyRevenueResponse {
    private Integer year;
    private Integer month;
    private String monthName;
    private BigDecimal revenue;
    private Long orderCount;
}
