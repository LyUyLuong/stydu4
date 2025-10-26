package com.lul.Stydu4.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lul.Stydu4.dto.request.Test.TestCreationRequest;
import com.lul.Stydu4.dto.request.Test.TestSearchRequest;
import com.lul.Stydu4.dto.request.Test.TestUpdateRequest;
import com.lul.Stydu4.dto.response.ApiResponse;
import com.lul.Stydu4.dto.response.PageResponse;
import com.lul.Stydu4.dto.response.Test.TestDetailResponse;
import com.lul.Stydu4.dto.response.Test.TestSummaryResponse;
import com.lul.Stydu4.enums.QuestionType;
import com.lul.Stydu4.enums.TestType;
import com.lul.Stydu4.service.ITestService;
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TestController {

    ITestService testService;
    ObjectMapper objectMapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<TestDetailResponse> createTest(@RequestBody @Valid TestCreationRequest request) {
        return ApiResponse.<TestDetailResponse>builder()
                .result(testService.create(request))
                .build();
    }

    @PostMapping(value = "/with-files")
//    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<TestDetailResponse> createTestWithFiles(
            @RequestPart("data") String testDataJson,
            @RequestPart(value = "audio", required = false) MultipartFile audio
    ) throws Exception {
        log.info("Creating test with audio file: {}",
                audio != null ? audio.getOriginalFilename() : "none");

        TestCreationRequest request = objectMapper.readValue(
                testDataJson,
                TestCreationRequest.class
        );

        TestDetailResponse response = testService.createWithAudio(request, audio);

        return ApiResponse.<TestDetailResponse>builder()
                .result(response)
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

    @GetMapping("/search")
    ApiResponse<PageResponse<TestSummaryResponse>> searchTests(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false, value = "page", defaultValue = "1") int page,
            @RequestParam(required = false, value = "size", defaultValue = "20") int size
    ) {
        TestSearchRequest request = TestSearchRequest.builder()
                .name(name)
                .type(type)
                .status(1) // Only show active tests for normal users
                .page(page)
                .size(size)
                .build();
        
        return ApiResponse.<PageResponse<TestSummaryResponse>>builder()
                .result(testService.searchTests(request, 
                    org.springframework.data.domain.PageRequest.of(page - 1, size)))
                .build();
    }


    @GetMapping("/search-with-specification")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<TestDetailResponse> updateTest(@PathVariable String testId, @RequestBody @Valid TestUpdateRequest request) {
        return ApiResponse.<TestDetailResponse>builder()
                .result(testService.update(testId, request))
                .build();
    }

    @PostMapping(value = "/{testId}/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<TestDetailResponse> updateTestAudio(
            @PathVariable String testId,
            @RequestPart("audio") MultipartFile audio
    ) {
        log.info("Updating audio for test: {} with file: {}",
                testId, audio.getOriginalFilename());

        TestDetailResponse response = testService.updateTestAudio(testId, audio);

        return ApiResponse.<TestDetailResponse>builder()
                .result(response)
                .build();
    }

    @DeleteMapping("/{testId}")
    ApiResponse<String> deleteTest(@PathVariable String testId) {
        testService.deleteTest(testId);
        return ApiResponse.<String>builder()
                .result("Test deleted")
                .build();
    }

    @GetMapping("/types")
    ApiResponse<List<TestTypeDto>> getTestTypes() {
        List<TestTypeDto> types = Arrays.stream(TestType.values())
                .map(type -> new TestTypeDto(type.getType(), type.getName()))
                .collect(Collectors.toList());

        return ApiResponse.<List<TestTypeDto>>builder()
                .result(types)
                .build();
    }

    public static class TestTypeDto {
        public String value;
        public String label;

        public TestTypeDto(String value, String label) {
            this.value = value;
            this.label = label;
        }
    }
}
