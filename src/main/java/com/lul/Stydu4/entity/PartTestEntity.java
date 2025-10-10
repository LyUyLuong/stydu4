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
public class PartTestEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private String description;
    private String type;

    @ManyToOne
    @JoinColumn(name = "test_id")
    private TestEntity testEntity;

    @OneToMany(mappedBy = "partEntity")
    private List<QuestionTestEntity> questions;

    @OneToMany(mappedBy = "partEntity")
    private List<QuestionGroupEntity> questionGroups;

}
