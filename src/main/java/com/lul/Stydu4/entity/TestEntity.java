package com.lul.Stydu4.entity;


import com.lul.Stydu4.enums.TestType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "test_entity", indexes = {
    @Index(name = "idx_test_type", columnList = "type"),
    @Index(name = "idx_test_status", columnList = "status"),
    @Index(name = "idx_test_slug", columnList = "slug"),
    @Index(name = "idx_test_type_status", columnList = "type, status")
})
public class TestEntity extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private String description;
    private Integer status;
    private Long numberOfParticipants;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audio_id")
    private FileEntity audio;

    @Enumerated(EnumType.STRING)
    private TestType type;

    private String slug;

    @OneToMany(mappedBy = "testEntity")
    private List<PartTestEntity> partTestEntities;



}
//    ko can thiet
//    @OneToMany(mappedBy = "test")
//    private List<ResultEntity> resultEntities;
