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
import java.time.LocalDateTime;
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
}
