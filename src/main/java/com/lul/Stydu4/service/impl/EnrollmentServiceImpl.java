package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.response.Course.EnrollmentResponse;
import com.lul.Stydu4.entity.CourseEntity;
import com.lul.Stydu4.entity.EnrollmentEntity;
import com.lul.Stydu4.entity.UserEntity;
import com.lul.Stydu4.enums.EnrollmentStatus;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.repository.IEnrollmentRepository;
import com.lul.Stydu4.service.IEnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentServiceImpl implements IEnrollmentService {

    private final IEnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public EnrollmentResponse enrollUser(UserEntity user, CourseEntity course) {
        // Kiểm tra đã mua chưa
        if (enrollmentRepository.existsByUserIdAndCourseId(user.getId(), course.getId())) {
            throw new AppException(ErrorCode.COURSE_ALREADY_PURCHASED);
        }

        // Tạo enrollment mới
        EnrollmentEntity enrollment = EnrollmentEntity.builder()
                .user(user)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .enrolledAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(course.getDuration()))
                .build();

        enrollment = enrollmentRepository.save(enrollment);
        log.info("User {} enrolled in course {}", user.getId(), course.getId());

        return mapToResponse(enrollment);
    }

    @Override
    public List<EnrollmentResponse> getUserEnrollments(String userId) {
        return enrollmentRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasActiveEnrollment(String userId, String courseId) {
        return enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .map(enrollment -> enrollment.getStatus() == EnrollmentStatus.ACTIVE
                        && enrollment.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *") // Chạy hàng ngày lúc 00:00
    @Transactional
    public void checkAndExpireEnrollments() {
        List<EnrollmentEntity> activeEnrollments = enrollmentRepository.findByUserIdAndStatus(null, EnrollmentStatus.ACTIVE);
        
        LocalDateTime now = LocalDateTime.now();
        int expiredCount = 0;
        
        for (EnrollmentEntity enrollment : activeEnrollments) {
            if (enrollment.getExpiresAt().isBefore(now)) {
                enrollment.setStatus(EnrollmentStatus.EXPIRED);
                enrollmentRepository.save(enrollment);
                expiredCount++;
            }
        }
        
        if (expiredCount > 0) {
            log.info("Expired {} enrollments", expiredCount);
        }
    }

    private EnrollmentResponse mapToResponse(EnrollmentEntity enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .courseId(enrollment.getCourse().getId())
                .courseTitle(enrollment.getCourse().getTitle())
                .status(enrollment.getStatus())
                .enrolledAt(enrollment.getEnrolledAt())
                .expiresAt(enrollment.getExpiresAt())
                .build();
    }
}
