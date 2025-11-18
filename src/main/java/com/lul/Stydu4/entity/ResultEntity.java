package com.lul.Stydu4.entity;

import com.lul.Stydu4.enums.TestType;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Time;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class ResultEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private Integer readingPoint;
    private Integer listeningPoint;
    private Integer totalPoint;  // Overall score (IELTS: average band, TOEIC: sum)
    private Integer readingCorrectAnswer;
    private Integer listeningCorrectAnswer;
    private Long  completeTime;

    private Boolean isFullTest;

    private Integer totalQuestions;

    @Enumerated(EnumType.STRING)
    private TestType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private TestEntity test;

    @OneToMany(mappedBy = "result", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ResultHavePartsEntity> resultHaveParts = new ArrayList<>();

    // Thêm relationship với UserAnswerEntity
    @OneToMany(mappedBy = "result", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserAnswerEntity> userAnswers = new ArrayList<>();

    @Transient
    public Duration getCompleteTime() {
        return completeTime != null ? Duration.ofMillis(completeTime) : Duration.ZERO;
    }

    public void setCompleteTime(Duration duration) {
        this.completeTime = duration != null ? duration.toMillis() : null;
    }
}
