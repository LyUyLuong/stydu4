package com.lul.Stydu4.controller;

import com.lul.Stydu4.dto.request.PartTest.PartTestCreationRequest;
import com.lul.Stydu4.dto.request.PartTest.PartTestSearchRequest;
import com.lul.Stydu4.dto.request.PartTest.PartTestUpdateRequest;
import com.lul.Stydu4.dto.response.ApiResponse;
import com.lul.Stydu4.dto.response.PageResponse;
import com.lul.Stydu4.dto.response.PartTest.PartTestDetailResponse;
import com.lul.Stydu4.dto.response.PartTest.PartTestSummaryResponse;
import com.lul.Stydu4.enums.PartType;
import com.lul.Stydu4.service.IPartTestService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/part-tests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PartTestController {

    IPartTestService partTestService;

    @PostMapping
    ApiResponse<PartTestDetailResponse> createPartTest(@RequestBody @Valid PartTestCreationRequest request) {
        return ApiResponse.<PartTestDetailResponse>builder()
                .result(partTestService.create(request))
                .build();
    }

    @GetMapping
    ApiResponse<PageResponse<PartTestSummaryResponse>> getAllPartTests(@RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                                                       @RequestParam(value = "size", required = false, defaultValue = "5") int size) {
        return ApiResponse.<PageResponse<PartTestSummaryResponse>>builder()
                .result(partTestService.getAllPartTests(page,size))
                .build();
    }

    @GetMapping("/search-with-specification")
    ApiResponse<PageResponse<PartTestSummaryResponse>> getPartTestsBySpecification(@ModelAttribute @Valid PartTestSearchRequest request,
                                                                           Pageable pageable) {
        return ApiResponse.<PageResponse<PartTestSummaryResponse>>builder()
                .result(partTestService.searchPartTests(request,pageable))
                .build();
    }

    @GetMapping("/{partTestId}")
    ApiResponse<PartTestDetailResponse> getPartTest(@PathVariable String partTestId) {
        return ApiResponse.<PartTestDetailResponse>builder()
                .result(partTestService.getPartTestById(partTestId))
                .build();
    }

    @PutMapping("/{partTestId}")
    ApiResponse<PartTestDetailResponse> updatePartTest(
            @PathVariable String partTestId,
            @RequestBody @Valid PartTestUpdateRequest request
    ) {
        return ApiResponse.<PartTestDetailResponse>builder()
                .result(partTestService.update(partTestId, request))
                .build();
    }

    @DeleteMapping("/{partTestId}")
    ApiResponse<String> deletePartTest(@PathVariable String partTestId) {
        partTestService.deletePartTest(partTestId);
        return ApiResponse.<String>builder()
                .result("PartTest deleted")
                .build();
    }


    @GetMapping("/types")
    ApiResponse<List<PartTestTypeDto>> getPartTestTypes() {
        List<PartTestTypeDto> types = Arrays.stream(PartType.values())
                .map(type -> new PartTestTypeDto(type.name(), type.getPartName()))
                .collect(Collectors.toList());

        return ApiResponse.<List<PartTestTypeDto>>builder()
                .result(types)
                .build();
    }

    public static class PartTestTypeDto {
        public String value;
        public String label;

        public PartTestTypeDto(String value, String label) {
            this.value = value;
            this.label = label;
        }
    }
}
