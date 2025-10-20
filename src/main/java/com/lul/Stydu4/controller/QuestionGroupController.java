package com.lul.Stydu4.controller;


import com.lul.Stydu4.dto.request.QuestionGroup.QuestionGroupCreateRequest;
import com.lul.Stydu4.dto.request.QuestionGroup.QuestionGroupSearchRequest;
import com.lul.Stydu4.dto.request.QuestionGroup.QuestionGroupUpdateRequest;
import com.lul.Stydu4.dto.response.ApiResponse;
import com.lul.Stydu4.dto.response.PageResponse;
import com.lul.Stydu4.dto.response.QuestionGroupResponse.QuestionGroupDetailResponse;
import com.lul.Stydu4.dto.response.QuestionGroupResponse.QuestionGroupSummaryResponse;
import com.lul.Stydu4.service.IQuestionGroupService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/question-groups")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class QuestionGroupController {

    IQuestionGroupService questionGroupService;

    @PostMapping
    ApiResponse<QuestionGroupDetailResponse> createQuestionGroup(
            @RequestBody @Valid QuestionGroupCreateRequest request
    ) {
        return ApiResponse.<QuestionGroupDetailResponse>builder()
                .result(questionGroupService.create(request))
                .build();
    }

    @GetMapping
    ApiResponse<PageResponse<QuestionGroupSummaryResponse>> getAllQuestionGroups(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "5") int size
    ) {
        return ApiResponse.<PageResponse<QuestionGroupSummaryResponse>>builder()
                .result(questionGroupService.getAllQuestionGroups(page, size))
                .build();
    }

    @GetMapping("/search-with-specification")
    ApiResponse<PageResponse<QuestionGroupSummaryResponse>> getQuestionGroupsBySpecification(
            @ModelAttribute @Valid QuestionGroupSearchRequest request,
            Pageable pageable
    ) {
        return ApiResponse.<PageResponse<QuestionGroupSummaryResponse>>builder()
                .result(questionGroupService.searchQuestionGroups(request, pageable))
                .build();
    }

    @GetMapping("/{questionGroupId}")
    ApiResponse<QuestionGroupDetailResponse> getQuestionGroup(
            @PathVariable String questionGroupId
    ) {
        return ApiResponse.<QuestionGroupDetailResponse>builder()
                .result(questionGroupService.getQuestionGroupById(questionGroupId))
                .build();
    }

    @PutMapping("/{questionGroupId}")
    ApiResponse<QuestionGroupDetailResponse> updateQuestionGroup(
            @PathVariable String questionGroupId,
            @RequestBody @Valid QuestionGroupUpdateRequest request
    ) {
        return ApiResponse.<QuestionGroupDetailResponse>builder()
                .result(questionGroupService.update(questionGroupId, request))
                .build();
    }

    @DeleteMapping("/{questionGroupId}")
    ApiResponse<String> deleteQuestionGroup(
            @PathVariable String questionGroupId
    ) {
        questionGroupService.deleteQuestionGroup(questionGroupId);
        return ApiResponse.<String>builder()
                .result("QuestionGroup deleted")
                .build();
    }
}
