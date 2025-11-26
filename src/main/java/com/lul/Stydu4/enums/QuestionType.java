package com.lul.Stydu4.enums;

import lombok.Getter;

@Getter
public enum QuestionType {

    MULTIPLE_CHOICE("MULTIPLE_CHOICE","Multiple Choice"),
    TRUE_FALSE("TRUE_FALSE","True/False"),
    FILL_IN_BLANK("FILL_IN_BLANK","Fill in the Blank");


    QuestionType(String type, String name){
        this.type=type;
        this.name=name;
    }

    private String type;
    private String name;
}
