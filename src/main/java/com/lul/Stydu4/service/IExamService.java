package com.lul.Stydu4.service;

import com.lul.Stydu4.dto.request.Exam.SubmitExamRequest;
import com.lul.Stydu4.dto.response.Exam.ExamQuestionsResponse;
import com.lul.Stydu4.dto.response.Exam.ExamResultResponse;

import java.util.List;

public interface IExamService {

    /**
     * Get exam questions for full test or selected parts
     * @param testId Test ID
     * @param partIds List of part IDs (null or empty for full test)
     * @return ExamQuestionsResponse with all questions
     */
    ExamQuestionsResponse getExamQuestions(String testId, List<String> partIds);

    /**
     * Submit exam answers and calculate result
     * @param request Submit exam request with answers
     * @param userName Current user ID
     * @return ExamResultResponse with scores and details
     */
    ExamResultResponse submitExam(SubmitExamRequest request, String userName);

    /**
     * Get exam result by result ID
     * @param resultId Result ID
     * @param userName Current user ID
     * @return ExamResultResponse
     */
    ExamResultResponse getExamResult(String resultId, String userName);

    /**
     * Get all exam results for a user on a specific test
     * @param testId Test ID
     * @param userName Current user ID
     * @return List of ExamResultResponse
     */
    List<ExamResultResponse> getUserExamResults(String testId, String userName);
}
