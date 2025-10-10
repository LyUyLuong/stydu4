package com.lul.Stydu4.controller;

import com.lul.Stydu4.dto.request.PartTest.PartTestCreationRequest;
import com.lul.Stydu4.dto.request.PartTest.PartTestUpdateRequest;
import com.lul.Stydu4.dto.response.ApiResponse;
import com.lul.Stydu4.dto.response.PageResponse;
import com.lul.Stydu4.dto.response.PartTest.PartTestDetailResponse;
import com.lul.Stydu4.dto.response.PartTest.PartTestSummaryResponse;
import com.lul.Stydu4.service.IPartTestService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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


}
