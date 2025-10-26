package com.lul.Stydu4.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.lul.Stydu4.dto.request.QuestionGroup.QuestionGroupCreateRequest;
import com.lul.Stydu4.dto.request.QuestionGroup.QuestionGroupSearchRequest;
import com.lul.Stydu4.dto.request.QuestionGroup.QuestionGroupUpdateRequest;
import com.lul.Stydu4.dto.response.ApiResponse;
import com.lul.Stydu4.dto.response.PageResponse;
import com.lul.Stydu4.dto.response.QuestionGroupResponse.QuestionGroupDetailResponse;
import com.lul.Stydu4.dto.response.QuestionGroupResponse.QuestionGroupSummaryResponse;
import com.lul.Stydu4.enums.PartType;
import com.lul.Stydu4.enums.QuestionType;
import com.lul.Stydu4.service.IQuestionGroupService;
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
@RequestMapping("/question-groups")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class QuestionGroupController {

    IQuestionGroupService questionGroupService;
    ObjectMapper objectMapper;

    @PostMapping
    ApiResponse<QuestionGroupDetailResponse> createQuestionGroup(
            @RequestBody @Valid QuestionGroupCreateRequest request
    ) {
        return ApiResponse.<QuestionGroupDetailResponse>builder()
                .result(questionGroupService.create(request))
                .build();
    }

    @PostMapping(value = "/with-files")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<QuestionGroupDetailResponse> createQuestionGroupWithFiles(
            @RequestPart("data") String groupDataJson,
            @RequestPart(value = "audio", required = false) MultipartFile audio,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws Exception {
        log.info("Creating question group with files - audio: {}, image: {}",
                audio != null ? audio.getOriginalFilename() : "none",
                image != null ? image.getOriginalFilename() : "none");

        QuestionGroupCreateRequest request = objectMapper.readValue(
                groupDataJson,
                QuestionGroupCreateRequest.class
        );

        QuestionGroupDetailResponse response = questionGroupService
                .createWithFiles(request, audio, image);

        return ApiResponse.<QuestionGroupDetailResponse>builder()
                .result(response)
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

    // ✅ NEW - Update Audio file
    @PostMapping(value = "/{questionGroupId}/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<QuestionGroupDetailResponse> updateQuestionGroupAudio(
            @PathVariable String questionGroupId,
            @RequestPart("audio") MultipartFile audio
    ) {
        log.info("Updating audio for question group: {} with file: {}",
                questionGroupId, audio.getOriginalFilename());

        QuestionGroupDetailResponse response = questionGroupService
                .updateGroupAudio(questionGroupId, audio);

        return ApiResponse.<QuestionGroupDetailResponse>builder()
                .result(response)
                .build();
    }

    // ✅ NEW - Update Image file
    @PostMapping(value = "/{questionGroupId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<QuestionGroupDetailResponse> updateQuestionGroupImage(
            @PathVariable String questionGroupId,
            @RequestPart("image") MultipartFile image
    ) {
        log.info("Updating image for question group: {} with file: {}",
                questionGroupId, image.getOriginalFilename());

        QuestionGroupDetailResponse response = questionGroupService
                .updateGroupImage(questionGroupId, image);

        return ApiResponse.<QuestionGroupDetailResponse>builder()
                .result(response)
                .build();
    }

    @GetMapping("/types")
    ApiResponse<List<QuestionGroupTypeDto>> getQuestionGroupTypes() {
        List<QuestionGroupTypeDto> types = Arrays.stream(QuestionType.values())
                .map(type -> new QuestionGroupTypeDto(type.getType(), type.getName()))
                .collect(Collectors.toList());

        return ApiResponse.<List<QuestionGroupTypeDto>>builder()
                .result(types)
                .build();
    }

    public static class QuestionGroupTypeDto {
        public String value;
        public String label;

        public QuestionGroupTypeDto(String value, String label) {
            this.value = value;
            this.label = label;
        }
    }
}
