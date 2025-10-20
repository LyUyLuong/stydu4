package com.lul.Stydu4.enums;

import lombok.Getter;

@Getter
public enum QuestionType {

    MULTIPLE_CHOICE("MULTIPLE_CHOICE","Multiple Choice"),
    TRUE_FALSE("TRUE_FALSE","True/False"),
    SHORT_ANSWER("SHORT_ANSWER","Short Answer");


    QuestionType(String type, String name){
        this.type=type;
        this.name=name;
    }

    private String type;
    private String name;
}
