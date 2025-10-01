package com.lul.Stydu4.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class AnswerEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String content;

    private Boolean isCorrect;

    private String mark;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private QuestionTestEntity question;

}
