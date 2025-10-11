package com.lul.Stydu4.controller;

import com.lul.Stydu4.dto.request.Test.TestCreationRequest;
import com.lul.Stydu4.dto.request.Test.TestSearchRequest;
import com.lul.Stydu4.dto.request.Test.TestUpdateRequest;
import com.lul.Stydu4.dto.response.ApiResponse;
import com.lul.Stydu4.dto.response.PageResponse;
import com.lul.Stydu4.dto.response.Test.TestDetailResponse;
import com.lul.Stydu4.dto.response.Test.TestSummaryResponse;
import com.lul.Stydu4.service.ITestService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TestController {

    ITestService testService;

    @PostMapping
    ApiResponse<TestDetailResponse> createTest(@RequestBody @Valid TestCreationRequest request) {
        return ApiResponse.<TestDetailResponse>builder()
                .result(testService.create(request))
                .build();
    }

    @GetMapping
    ApiResponse<PageResponse<TestSummaryResponse>> getAllTests(@RequestParam(required = false, value = "page", defaultValue = "1") int page,
                                                               @RequestParam(required = false, value = "size", defaultValue = "5") int size
    ) {
        return ApiResponse.<PageResponse<TestSummaryResponse>>builder()
                .result(testService.getAllTests(page, size))
                .build();
    }


    @GetMapping("/search-with-specification")
    ApiResponse<PageResponse<TestSummaryResponse>> getTestsBySpecification(@ModelAttribute @Valid TestSearchRequest request,
                                                                           Pageable pageable) {
        return ApiResponse.<PageResponse<TestSummaryResponse>>builder()
                .result(testService.searchTests(request,pageable))
                .build();
    }


    @GetMapping("/{testId}")
    ApiResponse<TestDetailResponse> getTest(@PathVariable String testId) {
        return ApiResponse.<TestDetailResponse>builder()
                .result(testService.getTestById(testId))
                .build();
    }

    @PutMapping("/{testId}")
    ApiResponse<TestDetailResponse> updateTest(@PathVariable String testId, @RequestBody @Valid TestUpdateRequest request) {
        return ApiResponse.<TestDetailResponse>builder()
                .result(testService.update(testId, request))
                .build();
    }

    @DeleteMapping("/{testId}")
    ApiResponse<String> deleteTest(@PathVariable String testId) {
        testService.deleteTest(testId);
        return ApiResponse.<String>builder()
                .result("Test deleted")
                .build();
    }
}
