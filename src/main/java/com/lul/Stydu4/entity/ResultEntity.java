package com.lul.Stydu4.entity;


import com.lul.Stydu4.enums.TestType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class ResultEntity extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private Integer readingPoint;

    private Integer listeningPoint;

    private Integer readingCorrectAnswer;

    private Integer listeningCorrectAnswer;

    private Time completeTime;

    @Enumerated(EnumType.STRING)
    private TestType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "test_id", nullable = false)
    private TestEntity test;

    @OneToMany(mappedBy = "result")
    private List<ResultHavePartsEntity> results;


}
