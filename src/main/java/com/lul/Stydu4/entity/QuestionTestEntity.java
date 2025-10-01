package com.lul.Stydu4.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class QuestionTestEntity extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private String content;
    private String type;
    private String audioPath;
    private String image;


    @ManyToOne
    @JoinColumn(name = "part_id")
    private PartTestEntity partEntity;

    @ManyToOne
    @JoinColumn(name = "question_group_id")
    private QuestionGroupEntity questionGroupEntity;

    @OneToMany(mappedBy = "question")
    private List<AnswerEntity> answers;

}
