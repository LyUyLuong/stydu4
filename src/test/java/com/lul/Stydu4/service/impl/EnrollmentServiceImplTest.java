package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.response.Course.EnrollmentResponse;
import com.lul.Stydu4.entity.CourseEntity;
import com.lul.Stydu4.entity.EnrollmentEntity;
import com.lul.Stydu4.entity.UserEntity;
import com.lul.Stydu4.enums.EnrollmentStatus;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.repository.IEnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnrollmentServiceImpl Tests")
class EnrollmentServiceImplTest {

    @Mock
    private IEnrollmentRepository enrollmentRepository;

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    private UserEntity user;
    private CourseEntity course;
    private EnrollmentEntity enrollment;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .id("user-123")
                .username("john_doe")
                .email("john@example.com")
                .build();

        course = CourseEntity.builder()
                .id("course-123")
                .title("TOEIC Complete Course")
                .description("Complete TOEIC preparation")
                .price(new BigDecimal("99.99"))
                .imageUrl("https://example.com/image.jpg")
                .duration(30)
                .build();

        enrollment = EnrollmentEntity.builder()
                .id("enrollment-123")
                .user(user)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .enrolledAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }

    @Nested
    @DisplayName("enrollUser Tests")
    class EnrollUserTests {

        @Test
        @DisplayName("Should enroll user successfully")
        void enrollUser_ValidData_Success() {
            // GIVEN
            when(enrollmentRepository.existsByUserIdAndCourseId("user-123", "course-123")).thenReturn(false);
            when(enrollmentRepository.save(any(EnrollmentEntity.class))).thenReturn(enrollment);

            // WHEN
            EnrollmentResponse result = enrollmentService.enrollUser(user, course);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getCourseId()).isEqualTo("course-123");
            assertThat(result.getCourseTitle()).isEqualTo("TOEIC Complete Course");
            assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);

            ArgumentCaptor<EnrollmentEntity> captor = ArgumentCaptor.forClass(EnrollmentEntity.class);
            verify(enrollmentRepository).save(captor.capture());
            
            EnrollmentEntity savedEnrollment = captor.getValue();
            assertThat(savedEnrollment.getUser()).isEqualTo(user);
            assertThat(savedEnrollment.getCourse()).isEqualTo(course);
            assertThat(savedEnrollment.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
            assertThat(savedEnrollment.getEnrolledAt()).isNotNull();
            assertThat(savedEnrollment.getExpiresAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when already enrolled")
        void enrollUser_AlreadyEnrolled_ThrowException() {
            // GIVEN
            when(enrollmentRepository.existsByUserIdAndCourseId("user-123", "course-123")).thenReturn(true);

            // WHEN & THEN
            assertThatThrownBy(() -> enrollmentService.enrollUser(user, course))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_ALREADY_PURCHASED);

            verify(enrollmentRepository).existsByUserIdAndCourseId("user-123", "course-123");
            verify(enrollmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should set correct expiration date based on course duration")
        void enrollUser_CorrectExpirationDate() {
            // GIVEN
            course.setDuration(60); // 60 days
            when(enrollmentRepository.existsByUserIdAndCourseId(anyString(), anyString())).thenReturn(false);
            when(enrollmentRepository.save(any(EnrollmentEntity.class))).thenAnswer(invocation -> {
                EnrollmentEntity arg = invocation.getArgument(0);
                return arg;
            });

            // WHEN
            enrollmentService.enrollUser(user, course);

            // THEN
            ArgumentCaptor<EnrollmentEntity> captor = ArgumentCaptor.forClass(EnrollmentEntity.class);
            verify(enrollmentRepository).save(captor.capture());
            
            EnrollmentEntity savedEnrollment = captor.getValue();
            LocalDateTime expectedExpiration = savedEnrollment.getEnrolledAt().plusDays(60);
            
            assertThat(savedEnrollment.getExpiresAt()).isEqualTo(expectedExpiration);
        }
    }

    @Nested
    @DisplayName("getUserEnrollments Tests")
    class GetUserEnrollmentsTests {

        @Test
        @DisplayName("Should return all enrollments for user")
        void getUserEnrollments_Success() {
            // GIVEN
            CourseEntity course2 = CourseEntity.builder()
                    .id("course-456")
                    .title("Advanced TOEIC")
                    .description("Advanced course")
                    .price(new BigDecimal("149.99"))
                    .imageUrl("https://example.com/image2.jpg")
                    .duration(30)
                    .build();

            EnrollmentEntity enrollment2 = EnrollmentEntity.builder()
                    .id("enrollment-456")
                    .user(user)
                    .course(course2)
                    .status(EnrollmentStatus.ACTIVE)
                    .enrolledAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusDays(30))
                    .build();

            when(enrollmentRepository.findByUserId("user-123")).thenReturn(List.of(enrollment, enrollment2));

            // WHEN
            List<EnrollmentResponse> result = enrollmentService.getUserEnrollments("user-123");

            // THEN
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getCourseTitle()).isEqualTo("TOEIC Complete Course");
            assertThat(result.get(1).getCourseTitle()).isEqualTo("Advanced TOEIC");

            verify(enrollmentRepository).findByUserId("user-123");
        }

        @Test
        @DisplayName("Should return empty list when user has no enrollments")
        void getUserEnrollments_EmptyList() {
            // GIVEN
            when(enrollmentRepository.findByUserId("user-123")).thenReturn(List.of());

            // WHEN
            List<EnrollmentResponse> result = enrollmentService.getUserEnrollments("user-123");

            // THEN
            assertThat(result).isEmpty();
            verify(enrollmentRepository).findByUserId("user-123");
        }
    }

    @Nested
    @DisplayName("hasActiveEnrollment Tests")
    class HasActiveEnrollmentTests {

        @Test
        @DisplayName("Should return true when enrollment is active and not expired")
        void hasActiveEnrollment_ActiveNotExpired_ReturnTrue() {
            // GIVEN
            enrollment.setStatus(EnrollmentStatus.ACTIVE);
            enrollment.setExpiresAt(LocalDateTime.now().plusDays(10));
            
            when(enrollmentRepository.findByUserIdAndCourseId("user-123", "course-123"))
                    .thenReturn(Optional.of(enrollment));

            // WHEN
            boolean result = enrollmentService.hasActiveEnrollment("user-123", "course-123");

            // THEN
            assertThat(result).isTrue();
            verify(enrollmentRepository).findByUserIdAndCourseId("user-123", "course-123");
        }

        @Test
        @DisplayName("Should return false when enrollment is expired")
        void hasActiveEnrollment_Expired_ReturnFalse() {
            // GIVEN
            enrollment.setStatus(EnrollmentStatus.ACTIVE);
            enrollment.setExpiresAt(LocalDateTime.now().minusDays(1));
            
            when(enrollmentRepository.findByUserIdAndCourseId("user-123", "course-123"))
                    .thenReturn(Optional.of(enrollment));

            // WHEN
            boolean result = enrollmentService.hasActiveEnrollment("user-123", "course-123");

            // THEN
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when enrollment status is not ACTIVE")
        void hasActiveEnrollment_NotActive_ReturnFalse() {
            // GIVEN
            enrollment.setStatus(EnrollmentStatus.EXPIRED);
            enrollment.setExpiresAt(LocalDateTime.now().plusDays(10));
            
            when(enrollmentRepository.findByUserIdAndCourseId("user-123", "course-123"))
                    .thenReturn(Optional.of(enrollment));

            // WHEN
            boolean result = enrollmentService.hasActiveEnrollment("user-123", "course-123");

            // THEN
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when enrollment not found")
        void hasActiveEnrollment_NotFound_ReturnFalse() {
            // GIVEN
            when(enrollmentRepository.findByUserIdAndCourseId("user-123", "course-123"))
                    .thenReturn(Optional.empty());

            // WHEN
            boolean result = enrollmentService.hasActiveEnrollment("user-123", "course-123");

            // THEN
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("checkAndExpireEnrollments Tests")
    class CheckAndExpireEnrollmentsTests {

        @Test
        @DisplayName("Should expire enrollments that have passed expiration date")
        void checkAndExpireEnrollments_ExpireOldEnrollments() {
            // GIVEN
            EnrollmentEntity expiredEnrollment1 = EnrollmentEntity.builder()
                    .id("enrollment-1")
                    .status(EnrollmentStatus.ACTIVE)
                    .expiresAt(LocalDateTime.now().minusDays(5))
                    .build();

            EnrollmentEntity expiredEnrollment2 = EnrollmentEntity.builder()
                    .id("enrollment-2")
                    .status(EnrollmentStatus.ACTIVE)
                    .expiresAt(LocalDateTime.now().minusDays(1))
                    .build();

            EnrollmentEntity activeEnrollment = EnrollmentEntity.builder()
                    .id("enrollment-3")
                    .status(EnrollmentStatus.ACTIVE)
                    .expiresAt(LocalDateTime.now().plusDays(10))
                    .build();

            when(enrollmentRepository.findByUserIdAndStatus(null, EnrollmentStatus.ACTIVE))
                    .thenReturn(List.of(expiredEnrollment1, expiredEnrollment2, activeEnrollment));
            when(enrollmentRepository.save(any(EnrollmentEntity.class))).thenAnswer(i -> i.getArgument(0));

            // WHEN
            enrollmentService.checkAndExpireEnrollments();

            // THEN
            assertThat(expiredEnrollment1.getStatus()).isEqualTo(EnrollmentStatus.EXPIRED);
            assertThat(expiredEnrollment2.getStatus()).isEqualTo(EnrollmentStatus.EXPIRED);
            assertThat(activeEnrollment.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);

            verify(enrollmentRepository, times(2)).save(any(EnrollmentEntity.class));
        }

        @Test
        @DisplayName("Should not expire any enrollments when all are still valid")
        void checkAndExpireEnrollments_AllValid_NoChanges() {
            // GIVEN
            EnrollmentEntity activeEnrollment1 = EnrollmentEntity.builder()
                    .id("enrollment-1")
                    .status(EnrollmentStatus.ACTIVE)
                    .expiresAt(LocalDateTime.now().plusDays(10))
                    .build();

            EnrollmentEntity activeEnrollment2 = EnrollmentEntity.builder()
                    .id("enrollment-2")
                    .status(EnrollmentStatus.ACTIVE)
                    .expiresAt(LocalDateTime.now().plusDays(20))
                    .build();

            when(enrollmentRepository.findByUserIdAndStatus(null, EnrollmentStatus.ACTIVE))
                    .thenReturn(List.of(activeEnrollment1, activeEnrollment2));

            // WHEN
            enrollmentService.checkAndExpireEnrollments();

            // THEN
            verify(enrollmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should handle empty enrollment list")
        void checkAndExpireEnrollments_EmptyList_NoError() {
            // GIVEN
            when(enrollmentRepository.findByUserIdAndStatus(null, EnrollmentStatus.ACTIVE))
                    .thenReturn(List.of());

            // WHEN & THEN
            assertThatCode(() -> enrollmentService.checkAndExpireEnrollments())
                    .doesNotThrowAnyException();

            verify(enrollmentRepository, never()).save(any());
        }
    }
}
