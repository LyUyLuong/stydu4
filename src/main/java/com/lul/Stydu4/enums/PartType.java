package com.lul.Stydu4.enums;

import lombok.Getter;

@Getter
public enum PartType {

    // TOEIC Parts (7 parts)
    PART_1_TOEIC("PART_1_TOEIC","Part 1 - Photographs (Listening)"),
    PART_2_TOEIC("PART_2_TOEIC","Part 2 - Question-Response (Listening)"),
    PART_3_TOEIC("PART_3_TOEIC","Part 3 - Conversations (Listening)"),
    PART_4_TOEIC("PART_4_TOEIC","Part 4 - Talks (Listening)"),
    PART_5_TOEIC("PART_5_TOEIC","Part 5 - Incomplete Sentences (Reading)"),
    PART_6_TOEIC("PART_6_TOEIC","Part 6 - Text Completion (Reading)"),
    PART_7_TOEIC("PART_7_TOEIC","Part 7 - Reading Comprehension (Reading)"),

    // IELTS Listening Parts (4 sections)
    LISTENING_SECTION_1_IELTS("LISTENING_SECTION_1_IELTS","Listening Section 1 - Social Conversation"),
    LISTENING_SECTION_2_IELTS("LISTENING_SECTION_2_IELTS","Listening Section 2 - Social Monologue"),
    LISTENING_SECTION_3_IELTS("LISTENING_SECTION_3_IELTS","Listening Section 3 - Academic Conversation"),
    LISTENING_SECTION_4_IELTS("LISTENING_SECTION_4_IELTS","Listening Section 4 - Academic Monologue"),

    // IELTS Reading Parts (3 passages)
    READING_PASSAGE_1_IELTS("READING_PASSAGE_1_IELTS","Reading Passage 1"),
    READING_PASSAGE_2_IELTS("READING_PASSAGE_2_IELTS","Reading Passage 2"),
    READING_PASSAGE_3_IELTS("READING_PASSAGE_3_IELTS","Reading Passage 3"),

    // IELTS Writing (Manual grading required)
    WRITING_TASK_1_IELTS("WRITING_TASK_1_IELTS","Writing Task 1 - Report/Graph"),
    WRITING_TASK_2_IELTS("WRITING_TASK_2_IELTS","Writing Task 2 - Essay"),

    // IELTS Speaking (Manual grading required)
    SPEAKING_PART_1_IELTS("SPEAKING_PART_1_IELTS","Speaking Part 1 - Introduction"),
    SPEAKING_PART_2_IELTS("SPEAKING_PART_2_IELTS","Speaking Part 2 - Long Turn"),
    SPEAKING_PART_3_IELTS("SPEAKING_PART_3_IELTS","Speaking Part 3 - Discussion"),
    ;

    PartType(String partType, String partName){
        this.partType = partType;
        this.partName = partName;
    }

    private String partType;
    private String partName;
}