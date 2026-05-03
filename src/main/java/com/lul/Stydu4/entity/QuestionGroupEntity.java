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
@Table(name = "question_group_entity", indexes = {
        @Index(name = "idx_qg_created", columnList = "createdDate")
})
public class QuestionGroupEntity extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

//    @Enumerated(EnumType.STRING)
    private String type;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private FileEntity image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audio_id")
    private FileEntity audio;

    @ManyToOne
    @JoinColumn(name = "part_id")
    private PartTestEntity partEntity;

    @OneToMany(mappedBy = "questionGroupEntity")
    private List<QuestionTestEntity> questions;

}
