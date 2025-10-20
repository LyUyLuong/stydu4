package com.lul.Stydu4.entity;

import com.lul.Stydu4.enums.PartType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class PartTestEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private PartType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id")
    private TestEntity testEntity;

    @OneToMany(
            mappedBy = "partEntity",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<QuestionTestEntity> questions = new ArrayList<>();

    @OneToMany(
            mappedBy = "partEntity",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<QuestionGroupEntity> questionGroups = new ArrayList<>();

    // Helper methods để sync bidirectional relationships
    public void addQuestion(QuestionTestEntity question) {
        questions.add(question);
        question.setPartEntity(this);
    }

    public void removeQuestion(QuestionTestEntity question) {
        questions.remove(question);
        question.setPartEntity(null);
    }

    public void addQuestionGroup(QuestionGroupEntity questionGroup) {
        questionGroups.add(questionGroup);
        questionGroup.setPartEntity(this);
    }

    public void removeQuestionGroup(QuestionGroupEntity questionGroup) {
        questionGroups.remove(questionGroup);
        questionGroup.setPartEntity(null);
    }
}
