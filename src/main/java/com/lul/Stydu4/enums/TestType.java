package com.lul.Stydu4.enums;

import lombok.Getter;

@Getter
public enum TestType {
    TOEIC("TOEIC", "Toeic"),
    IELTS("IELTS", "Ielts");

    TestType(String type, String name){
        this.type = type;
        this.name = name;
    }

    private String type;
    private String name;
}
