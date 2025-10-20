package com.lul.Stydu4.controller;


import com.lul.Stydu4.dto.request.Question.QuestionTestCreateRequest;
import com.lul.Stydu4.dto.request.Question.QuestionTestSearchRequest;
import com.lul.Stydu4.dto.request.Question.QuestionTestUpdateRequest;
import com.lul.Stydu4.dto.response.ApiResponse;
import com.lul.Stydu4.dto.response.PageResponse;
import com.lul.Stydu4.dto.response.Question.QuestionTestDetailResponse;

import com.lul.Stydu4.dto.response.Question.QuestionTestSummaryResponse;
import com.lul.Stydu4.service.IQuestionTestService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/question-tests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class QuestionTestController {

    IQuestionTestService questionTestService;

    @PostMapping
    ApiResponse<QuestionTestDetailResponse> createQuestionTest(
            @RequestBody @Valid QuestionTestCreateRequest request
    ) {
        return ApiResponse.<QuestionTestDetailResponse>builder()
                .result(questionTestService.create(request))
                .build();
    }

    @GetMapping
    ApiResponse<PageResponse<QuestionTestSummaryResponse>> getAllQuestionTests(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "5") int size
    ) {
        return ApiResponse.<PageResponse<QuestionTestSummaryResponse>>builder()
                .result(questionTestService.getAllQuestionTests(page, size))
                .build();
    }

    @GetMapping("/search-with-specification")
    ApiResponse<PageResponse<QuestionTestSummaryResponse>> getQuestionTestsBySpecification(
            @ModelAttribute @Valid QuestionTestSearchRequest request,
            Pageable pageable
    ) {
        return ApiResponse.<PageResponse<QuestionTestSummaryResponse>>builder()
                .result(questionTestService.searchQuestionTests(request, pageable))
                .build();
    }

    @GetMapping("/{questionTestId}")
    ApiResponse<QuestionTestDetailResponse> getQuestionTest(
            @PathVariable String questionTestId
    ) {
        return ApiResponse.<QuestionTestDetailResponse>builder()
                .result(questionTestService.getQuestionTestById(questionTestId))
                .build();
    }

    @PutMapping("/{questionTestId}")
    ApiResponse<QuestionTestDetailResponse> updateQuestionTest(
            @PathVariable String questionTestId,
            @RequestBody @Valid QuestionTestUpdateRequest request
    ) {
        return ApiResponse.<QuestionTestDetailResponse>builder()
                .result(questionTestService.update(questionTestId, request))
                .build();
    }

    @DeleteMapping("/{questionTestId}")
    ApiResponse<String> deleteQuestionTest(
            @PathVariable String questionTestId
    ) {
        questionTestService.deleteQuestionTest(questionTestId);
        return ApiResponse.<String>builder()
                .result("QuestionTest deleted")
                .build();
    }
}
