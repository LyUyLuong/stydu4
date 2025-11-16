package com.lul.Stydu4.controller;

import com.lul.Stydu4.annotation.RateLimited;
import com.lul.Stydu4.dto.request.Exam.SubmitExamRequest;
import com.lul.Stydu4.dto.response.ApiResponse;
import com.lul.Stydu4.dto.response.Exam.ExamQuestionsResponse;
import com.lul.Stydu4.dto.response.Exam.ExamResultResponse;
import com.lul.Stydu4.service.IExamService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ExamController {

    IExamService examService;

    /**
     * Start Full Test
     * GET /exams/tests/{testId}/start
     *
     * Example: GET /exams/tests/abc-123/start
     */
    @GetMapping("/tests/{testId}/start")
    public ApiResponse<ExamQuestionsResponse> startFullTest(
            @PathVariable String testId
    ) {
        log.info("Starting full test: {}", testId);
        return ApiResponse.<ExamQuestionsResponse>builder()
                .result(examService.getExamQuestions(testId, null))
                .build();
    }

    /**
     * Start Practice Test with Selected Parts
     * GET /exams/tests/{testId}/practice?part=part1-id&part=part2-id&part=part3-id
     *
     * Example: GET /exams/tests/abc-123/practice?part=p1&part=p3&part=p5
     */
    @GetMapping("/tests/{testId}/practice")
    public ApiResponse<ExamQuestionsResponse> practiceTest(
            @PathVariable String testId,
            @RequestParam(value = "part", required = false) List<String> partIds
    ) {
        log.info("Starting practice test: {}, selected parts: {}", testId, partIds);
        return ApiResponse.<ExamQuestionsResponse>builder()
                .result(examService.getExamQuestions(testId, partIds))
                .build();
    }

    /**
     * Submit Exam (Both Full Test and Practice)
     * POST /exams/submit
     *
     * Body: {
     *   "testId": "abc-123",
     *   "partIds": ["p1", "p3"] or null for full test,
     *   "answers": [{"questionId": "q1", "answerId": "a1"}]
     * }
     */
    @PostMapping("/submit")
    @RateLimited(maxRequests = 3, timeWindowSeconds = 60, keyPrefix = "exam_submit")
    public ApiResponse<ExamResultResponse> submitExam(
            @RequestBody @Valid SubmitExamRequest request
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        log.info("Submitting exam for user: {}, test: {}", userName, request.getTestId());

        return ApiResponse.<ExamResultResponse>builder()
                .result(examService.submitExam(request, userName))
                .build();
    }

    /**
     * Get Exam Result by Result ID
     * GET /exams/results/{resultId}
     *
     * Example: GET /exams/results/result-xyz-789
     */
    @GetMapping("/results/{resultId}")
    public ApiResponse<ExamResultResponse> getExamResult(
            @PathVariable String resultId
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        log.info("Fetching exam result: {} for user: {}", resultId, userName);

        return ApiResponse.<ExamResultResponse>builder()
                .result(examService.getExamResult(resultId, userName))
                .build();
    }

    /**
     * Get User's All Exam Results for a Test
     * GET /exams/tests/{testId}/my-results
     *
     * Example: GET /exams/tests/abc-123/my-results
     */
    @GetMapping("/tests/{testId}/my-results")
    public ApiResponse<List<ExamResultResponse>> getUserExamResults(
            @PathVariable String testId
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        log.info("Fetching all results for test: {} and user: {}", testId, userName);

        return ApiResponse.<List<ExamResultResponse>>builder()
                .result(examService.getUserExamResults(testId, userName))
                .build();
    }

    /**
     * Get All Exam Results for Current User (across all tests)
     * GET /exams/my-results
     *
     * Example: GET /exams/my-results
     */
    @GetMapping("/my-results")
    public ApiResponse<List<ExamResultResponse>> getAllUserExamResults() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        log.info("Fetching all exam results for user: {}", userName);

        return ApiResponse.<List<ExamResultResponse>>builder()
                .result(examService.getAllUserExamResults(userName))
                .build();
    }
}
