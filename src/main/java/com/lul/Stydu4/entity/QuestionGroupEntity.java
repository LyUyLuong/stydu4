package com.lul.Stydu4.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class QuestionGroupEntity extends BaseEntity{

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

    @OneToMany(mappedBy = "questionGroupEntity")
    private List<QuestionTestEntity> questions;

}
