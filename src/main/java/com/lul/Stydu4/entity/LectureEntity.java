package com.lul.Stydu4.entity;

import com.lul.Stydu4.enums.LectureType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "lectures", indexes = {
    @Index(name = "idx_lecture_course_id", columnList = "course_id"),
    @Index(name = "idx_lecture_course_order", columnList = "course_id, orderIndex"),
    @Index(name = "idx_lecture_published", columnList = "isPublished")
})
public class LectureEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseEntity course;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer orderIndex; // Thứ tự của lecture trong course

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private LectureType type;

    @Column(columnDefinition = "TEXT")
    private String content; // URL video, nội dung text, hoặc URL file

    private Integer duration; // Thời lượng (phút) - chỉ áp dụng cho VIDEO

    @Builder.Default
    @Column(nullable = false)
    private Boolean isPublished = false;
}
