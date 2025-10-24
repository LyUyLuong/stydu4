package com.lul.Stydu4.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/question-tests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class QuestionTestController {

    IQuestionTestService questionTestService;
    ObjectMapper objectMapper;

    @PostMapping
    ApiResponse<QuestionTestDetailResponse> createQuestionTest(
            @RequestBody @Valid QuestionTestCreateRequest request
    ) {
        return ApiResponse.<QuestionTestDetailResponse>builder()
                .result(questionTestService.create(request))
                .build();
    }

    @PostMapping(value = "/with-files")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<QuestionTestDetailResponse> createQuestionWithFiles(
            @RequestPart("data") String questionDataJson,
            @RequestPart(value = "audio", required = false) MultipartFile audio,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws Exception {
        log.info("Creating question with files - audio: {}, image: {}",
                audio != null ? audio.getOriginalFilename() : "none",
                image != null ? image.getOriginalFilename() : "none");

        QuestionTestCreateRequest request = objectMapper.readValue(
                questionDataJson,
                QuestionTestCreateRequest.class
        );

        QuestionTestDetailResponse response = questionTestService
                .createWithFiles(request, audio, image);

        return ApiResponse.<QuestionTestDetailResponse>builder()
                .result(response)
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


    // ✅ Update Audio file
    @PostMapping(value = "/{questionId}/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<QuestionTestDetailResponse> updateQuestionAudio(
            @PathVariable String questionId,
            @RequestPart("audio") MultipartFile audio
    ) {
        log.info("Updating audio for question: {} with file: {}",
                questionId, audio.getOriginalFilename());

        QuestionTestDetailResponse response = questionTestService
                .updateQuestionAudio(questionId, audio);

        return ApiResponse.<QuestionTestDetailResponse>builder()
                .result(response)
                .build();
    }

    // ✅ Update Image file
    @PostMapping(value = "/{questionId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<QuestionTestDetailResponse> updateQuestionImage(
            @PathVariable String questionId,
            @RequestPart("image") MultipartFile image
    ) {
        log.info("Updating image for question: {} with file: {}",
                questionId, image.getOriginalFilename());

        QuestionTestDetailResponse response = questionTestService
                .updateQuestionImage(questionId, image);

        return ApiResponse.<QuestionTestDetailResponse>builder()
                .result(response)
                .build();
    }
}
