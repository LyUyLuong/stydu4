package com.lul.Stydu4.enums;

import lombok.Getter;

@Getter
public enum PartType {

    //TOEIC
    PART_1_TOEIC("PART_1_TOEIC","Part 1 for Toeic Test"),
    PART_2_TOEIC("PART_2_TOEIC","Part 3 for Toeic Test"),
    PART_3_TOEIC("PART_3_TOEIC","Part 3 for Toeic Test"),
    PART_4_TOEIC("PART_4_TOEIC","Part 3 for Toeic Test"),
    PART_5_TOEIC("PART_5_TOEIC","Part 3 for Toeic Test"),
    PART_6_TOEIC("PART_6_TOEIC","Part 3 for Toeic Test"),
    PART_7_TOEIC("PART_7_TOEIC","Part 3 for Toeic Test"),

    //IELTS
    PART_1_IELTS("PART_1_IELTS","Part 1 for Ielts Test"),
    PART_2_IELTS("PART_2_IELTS","Part 3 for Ielts Test"),
    PART_3_IELTS("PART_3_IELTS","Part 3 for Ielts Test"),
    PART_4_IELTS("PART_4_IELTS","Part 3 for Ielts Test"),
    PART_5_IELTS("PART_5_IELTS","Part 3 for Ielts Test"),
    PART_6_IELTS("PART_6_IELTS","Part 3 for Ielts Test"),
    PART_7_IELTS("PART_7_IELTS","Part 3 for Ielts Test"),
    ;

    PartType(String partType, String partName){
        this.partType = partType;
        this.partName = partName;
    }

    private String partType;
    private String partName;
}
