package com.lul.Stydu4.entity;

import com.lul.Stydu4.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "enrollments", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_course", columnNames = {"user_id", "course_id"})
    },
    indexes = {
        @Index(name = "idx_enrollment_user_id", columnList = "user_id"),
        @Index(name = "idx_enrollment_course_id", columnList = "course_id"),
        @Index(name = "idx_enrollment_status", columnList = "status"),
        @Index(name = "idx_enrollment_user_status", columnList = "user_id, status")
    }
)
public class EnrollmentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseEntity course;

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;

    private LocalDateTime enrolledAt;
    
    private LocalDateTime expiresAt; // Thời gian hết hạn
}
