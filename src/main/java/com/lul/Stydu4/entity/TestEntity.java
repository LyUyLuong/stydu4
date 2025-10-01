package com.lul.Stydu4.entity;


import com.lul.Stydu4.enums.TestType;
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
public class TestEntity extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private String description;
    private Integer status;
    private Long numberOfParticipants;
    private String audioPath;

    @Enumerated(EnumType.STRING)
    private TestType type;

    private String slug;

    @OneToMany(mappedBy = "testEntity")
    private List<PartTestEntity> partTestEntities;

//    ko can thiet
//    @OneToMany(mappedBy = "test")
//    private List<ResultEntity> resultEntities;


}
