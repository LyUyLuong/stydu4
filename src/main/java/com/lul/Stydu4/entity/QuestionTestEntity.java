package com.lul.Stydu4.entity;

import com.lul.Stydu4.enums.QuestionType;
import com.lul.Stydu4.enums.TestType;
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
public class QuestionTestEntity extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private QuestionType type;

    private String audioPath;
    private String image;
    private String description;


    @ManyToOne
    @JoinColumn(name = "part_id")
    private PartTestEntity partEntity;

    @ManyToOne
    @JoinColumn(name = "question_group_id")
    private QuestionGroupEntity questionGroupEntity;

    // QuestionTestEntity (sửa)
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AnswerEntity> answers = new ArrayList<>();


}
