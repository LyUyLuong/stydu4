package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.response.Dashboard.*;
import com.lul.Stydu4.entity.EnrollmentEntity;
import com.lul.Stydu4.entity.TestEntity;
import com.lul.Stydu4.enums.PaymentStatus;
import com.lul.Stydu4.repository.*;
import com.lul.Stydu4.service.IDashboardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DashboardServiceImpl implements IDashboardService {
    
    IUserRepository userRepository;
    ITestRepository testRepository;
    ICourseRepository courseRepository;
    IOrderRepository orderRepository;
    IEnrollmentRepository enrollmentRepository;
    
    @Override
    public DashboardStatsResponse getDashboardStats(LocalDateTime startDate, LocalDateTime endDate) {
        // Nếu không có startDate và endDate, mặc định là 30 ngày gần nhất
        if (startDate == null || endDate == null) {
            endDate = LocalDateTime.now();
            startDate = endDate.minusDays(30);
        }
        
        // Tổng số users
        Long totalUsers = userRepository.count();
        
        // Tổng số tests
        Long totalTests = testRepository.count();
        
        // Tổng số courses
        Long totalCourses = courseRepository.count();
        
        // Số tests đang active (status = 1)
        Long activeTests = testRepository.countByStatus(1);
        
        // Tổng doanh thu toàn thời gian
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue(PaymentStatus.COMPLETED);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        
        // Doanh thu trong khoảng thời gian
        BigDecimal periodRevenue = orderRepository.calculateRevenueByPeriod(
            PaymentStatus.COMPLETED, startDate, endDate);
        if (periodRevenue == null) periodRevenue = BigDecimal.ZERO;
        
        // Top 5 tests gần nhất
        Pageable top5 = PageRequest.of(0, 5);
        List<TestEntity> tests = testRepository.findAll(top5).getContent();
        List<RecentTestResponse> recentTests = tests.stream()
            .map(test -> RecentTestResponse.builder()
                .id(test.getId())
                .name(test.getName())
                .type(test.getType() != null ? test.getType().name() : "UNKNOWN")
                .status(test.getStatus())
                .numberOfParticipants(test.getNumberOfParticipants() != null ? test.getNumberOfParticipants().intValue() : 0)
                .build())
            .collect(Collectors.toList());
        
        // Top khóa học theo doanh thu trong khoảng thời gian
        List<Object[]> topCoursesData = orderRepository.findTopCoursesByRevenue(
            PaymentStatus.COMPLETED, startDate, endDate);
        List<CourseRevenueResponse> topCourses = topCoursesData.stream()
            .limit(5)
            .map(row -> CourseRevenueResponse.builder()
                .courseId((String) row[0])
                .courseName((String) row[1])
                .revenue((BigDecimal) row[2])
                .totalEnrollments((Long) row[3])
                .price((BigDecimal) row[4])
                .build())
            .collect(Collectors.toList());
        
        // 10 enrollments gần nhất
        List<EnrollmentEntity> enrollments = enrollmentRepository
            .findTop10ByOrderByEnrolledAtDesc(PageRequest.of(0, 10));
        List<RecentEnrollmentResponse> recentEnrollments = enrollments.stream()
            .map(enrollment -> RecentEnrollmentResponse.builder()
                .id(enrollment.getId())
                .courseName(enrollment.getCourse() != null ? enrollment.getCourse().getTitle() : "Unknown")
                .userName(enrollment.getUser() != null ? 
                    (enrollment.getUser().getFirstName() + " " + enrollment.getUser().getLastName()) : "Unknown")
                .enrolledAt(enrollment.getEnrolledAt())
                .status(enrollment.getStatus() != null ? enrollment.getStatus().name() : "ACTIVE")
                .build())
            .collect(Collectors.toList());
        
        return DashboardStatsResponse.builder()
            .totalUsers(totalUsers)
            .totalTests(totalTests)
            .totalCourses(totalCourses)
            .activeTests(activeTests)
            .totalRevenue(totalRevenue)
            .periodRevenue(periodRevenue)
            .recentTests(recentTests)
            .topCourses(topCourses)
            .recentEnrollments(recentEnrollments)
            .build();
    }

    @Override
    public RevenueAnalyticsResponse getRevenueAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        // Nếu không có startDate và endDate, mặc định là 30 ngày gần nhất
        if (startDate == null || endDate == null) {
            endDate = LocalDateTime.now();
            startDate = endDate.minusDays(30);
        }

        log.info("Calculating revenue analytics from {} to {}", startDate, endDate);

        // Tổng doanh thu toàn thời gian
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue(PaymentStatus.COMPLETED);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        // Doanh thu trong khoảng thời gian
        BigDecimal periodRevenue = orderRepository.calculateRevenueByPeriod(
            PaymentStatus.COMPLETED, startDate, endDate);
        if (periodRevenue == null) periodRevenue = BigDecimal.ZERO;

        // Tổng số orders
        Long totalOrders = orderRepository.count();

        // Số orders trong khoảng thời gian
        Long periodOrders = orderRepository.countOrdersByPeriod(
            PaymentStatus.COMPLETED, startDate, endDate);
        if (periodOrders == null) periodOrders = 0L;

        // Giá trị đơn hàng trung bình
        BigDecimal averageOrderValue = BigDecimal.ZERO;
        if (periodOrders > 0) {
            averageOrderValue = periodRevenue.divide(
                BigDecimal.valueOf(periodOrders), 2, RoundingMode.HALF_UP);
        }

        // Tỷ lệ tăng trưởng (so với tổng doanh thu)
        BigDecimal growthRate = BigDecimal.ZERO;
        if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growthRate = periodRevenue
                .divide(totalRevenue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }

        // Doanh thu theo ngày
        List<Object[]> dailyData = orderRepository.findDailyRevenue(
            PaymentStatus.COMPLETED, startDate, endDate);
        List<DailyRevenueResponse> dailyRevenue = dailyData.stream()
            .map(row -> DailyRevenueResponse.builder()
                .date(row[0] instanceof Date ? ((Date) row[0]).toLocalDate() : (LocalDate) row[0])
                .revenue((BigDecimal) row[1])
                .orderCount(((Number) row[2]).longValue())
                .build())
            .collect(Collectors.toList());

        // Doanh thu theo tháng
        List<Object[]> monthlyData = orderRepository.findMonthlyRevenue(
            PaymentStatus.COMPLETED, startDate, endDate);
        List<MonthlyRevenueResponse> monthlyRevenue = monthlyData.stream()
            .map(row -> {
                Integer year = (Integer) row[0];
                Integer month = (Integer) row[1];
                String monthName = Month.of(month).name();

                return MonthlyRevenueResponse.builder()
                    .year(year)
                    .month(month)
                    .monthName(monthName)
                    .revenue((BigDecimal) row[2])
                    .orderCount(((Number) row[3]).longValue())
                    .build();
            })
            .collect(Collectors.toList());

        // Top khóa học theo doanh thu
        List<Object[]> topCoursesData = orderRepository.findTopCoursesByRevenue(
            PaymentStatus.COMPLETED, startDate, endDate);
        List<CourseRevenueResponse> topCourses = topCoursesData.stream()
            .limit(10)
            .map(row -> CourseRevenueResponse.builder()
                .courseId((String) row[0])
                .courseName((String) row[1])
                .revenue((BigDecimal) row[2])
                .totalEnrollments((Long) row[3])
                .price((BigDecimal) row[4])
                .build())
            .collect(Collectors.toList());

        return RevenueAnalyticsResponse.builder()
            .totalRevenue(totalRevenue)
            .periodRevenue(periodRevenue)
            .totalOrders(totalOrders)
            .periodOrders(periodOrders)
            .averageOrderValue(averageOrderValue)
            .growthRate(growthRate)
            .dailyRevenue(dailyRevenue)
            .monthlyRevenue(monthlyRevenue)
            .topCourses(topCourses)
            .build();
    }
}
