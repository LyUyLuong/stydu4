package com.lul.Stydu4.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lul.Stydu4.dto.request.Exam.SubmitExamRequest;
import com.lul.Stydu4.dto.request.Exam.UserAnswerSubmit;
import com.lul.Stydu4.dto.response.Exam.ExamQuestionsResponse;
import com.lul.Stydu4.dto.response.Exam.ExamResultResponse;
import com.lul.Stydu4.service.IExamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExamController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ExamController Tests")
class ExamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IExamService examService;

    private ExamQuestionsResponse mockExamQuestions;
    private ExamResultResponse mockExamResult;
    private SubmitExamRequest mockSubmitRequest;

    @BeforeEach
    void setUp() {
        // Setup mock ExamQuestionsResponse
        mockExamQuestions = ExamQuestionsResponse.builder()
                .testId("test-123")
                .testName("TOEIC Practice Test")
                .testType("TOEIC")
                .isFullTest(true)
                .parts(new ArrayList<>())
                .build();

        // Setup mock ExamResultResponse
        mockExamResult = ExamResultResponse.builder()
                .resultId("result-123")
                .testId("test-123")
                .testName("TOEIC Practice Test")
                .testType("TOEIC")
                .userId("user-123")
                .userName("testuser")
                .totalScore(850)
                .listeningScore(400)
                .readingScore(450)
                .totalCorrectAnswers(85)
                .totalQuestions(100)
                .completeTime("02:00:00")
                .build();

        // Setup mock SubmitExamRequest
        List<UserAnswerSubmit> answers = Arrays.asList(
                UserAnswerSubmit.builder()
                        .questionId("q1")
                        .answerId("a1")
                        .build(),
                UserAnswerSubmit.builder()
                        .questionId("q2")
                        .answerId("a2")
                        .build()
        );

        mockSubmitRequest = SubmitExamRequest.builder()
                .testId("test-123")
                .partIds(null)  // Full test
                .answers(answers)
                .build();
    }

    @Nested
    @DisplayName("Start Full Test Tests")
    class StartFullTestTests {

        @Test
        @DisplayName("Should start full test successfully")
        void startFullTest_Success() throws Exception {
            // GIVEN
            when(examService.getExamQuestions(eq("test-123"), isNull()))
                    .thenReturn(mockExamQuestions);

            // WHEN & THEN
            mockMvc.perform(get("/exams/tests/test-123/start")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.testId").value("test-123"))
                    .andExpect(jsonPath("$.result.testName").value("TOEIC Practice Test"))
                    .andExpect(jsonPath("$.result.isFullTest").value(true));
        }
    }

    @Nested
    @DisplayName("Start Practice Test Tests")
    class StartPracticeTestTests {

        @Test
        @DisplayName("Should start practice test with selected parts")
        void practiceTest_WithSelectedParts_Success() throws Exception {
            // GIVEN
            List<String> partIds = Arrays.asList("part-1", "part-2");
            ExamQuestionsResponse practiceResponse = ExamQuestionsResponse.builder()
                    .testId("test-123")
                    .testName("TOEIC Practice Test")
                    .testType("TOEIC")
                    .isFullTest(false)
                    .parts(new ArrayList<>())
                    .build();

            when(examService.getExamQuestions(eq("test-123"), eq(partIds)))
                    .thenReturn(practiceResponse);

            // WHEN & THEN
            mockMvc.perform(get("/exams/tests/test-123/practice")
                            .param("part", "part-1")
                            .param("part", "part-2")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.testId").value("test-123"))
                    .andExpect(jsonPath("$.result.isFullTest").value(false));
        }

        @Test
        @DisplayName("Should handle practice test without parts (defaults to full test)")
        void practiceTest_WithoutParts_Success() throws Exception {
            // GIVEN
            when(examService.getExamQuestions(eq("test-123"), isNull()))
                    .thenReturn(mockExamQuestions);

            // WHEN & THEN
            mockMvc.perform(get("/exams/tests/test-123/practice")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.testId").value("test-123"));
        }
    }

}
