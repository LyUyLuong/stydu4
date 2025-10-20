package com.lul.Stydu4.dto.response.Exam;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamQuestionsResponse {

    String testId;
    String testName;
    String testType;
    String description;

    // Thông tin về chế độ thi
    Boolean isFullTest;
    List<String> selectedPartIds;

    Integer totalQuestions;

    List<PartQuestionsDetail> parts;
}
