package com.lul.Stydu4.dto.response.Exam;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionResultDetail {

    String questionId;
    String questionContent;

    String audioId;
    String audioUrl;

    String imageId;
    String imageUrl;

    String userAnswerId;
    String userAnswerContent;
    String correctAnswerId;
    String correctAnswerContent;
    Boolean isCorrect;
    String partName;
    
    // All answer options for this question
    List<AnswerDetail> allAnswers;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class AnswerDetail {
        String answerId;
        String mark;        // A, B, C, D
        String content;     // Answer text content (may be null for Part 1, 2)
        Boolean isCorrect;
    }
}
