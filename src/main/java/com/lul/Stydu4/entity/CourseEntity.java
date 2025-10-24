package com.lul.Stydu4.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "courses")
public class CourseEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private String imageUrl;

    private Integer duration; // Số ngày có thể truy cập

    @Builder.Default
    private Boolean isPublished = false;

    @ManyToMany
    @JoinTable(
        name = "course_tests",
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "test_id")
    )
    private List<TestEntity> tests;

    @OneToMany(mappedBy = "course")
    private List<EnrollmentEntity> enrollments;
}
