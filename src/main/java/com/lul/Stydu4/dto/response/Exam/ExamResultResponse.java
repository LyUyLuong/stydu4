package com.lul.Stydu4.dto.response.Exam;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamResultResponse {

    String resultId;
    String testId;
    String testName;
    String testType;
    String userId;
    String userName;

    // Thông tin về chế độ thi
    Boolean isFullTest;
    List<String> completedPartIds;

    // Overall scores
    Integer totalScore;
    Integer listeningScore;
    Integer readingScore;

    // Correct answers count
    Integer totalCorrectAnswers;
    Integer listeningCorrectAnswers;
    Integer readingCorrectAnswers;

    // Total questions
    Integer totalQuestions;
    Integer listeningQuestions;
    Integer readingQuestions;

    // Completion time
    String completeTime;

    // Part breakdown
    List<PartResultDetail> partResults;

    // Answer details (optional)
    List<QuestionResultDetail> questionResults;
}
