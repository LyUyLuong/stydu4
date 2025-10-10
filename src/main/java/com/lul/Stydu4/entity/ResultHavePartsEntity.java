package com.lul.Stydu4.entity;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class ResultHavePartsEntity extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "result_id", nullable = false)
    private ResultEntity result;

    @ManyToOne
    @JoinColumn(name = "part_id", nullable = false)
    private PartTestEntity partTest;

}
