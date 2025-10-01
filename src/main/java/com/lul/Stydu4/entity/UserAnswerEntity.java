package com.lul.Stydu4.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class UserAnswerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;



}
