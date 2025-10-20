package com.lul.Stydu4.dto.response.Exam;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionResultDetail {

    String questionId;
    String questionContent;
    String userAnswerId;
    String userAnswerContent;
    String correctAnswerId;
    String correctAnswerContent;
    Boolean isCorrect;
    String partName;
}
