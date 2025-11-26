package com.lul.Stydu4.enums;

import lombok.Getter;

@Getter
public enum LectureType {
    VIDEO("VIDEO", "Video Lecture"),
    TEXT("TEXT", "Text Content"),
    FILE("FILE", "Downloadable File");

    private final String code;
    private final String description;

    LectureType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
