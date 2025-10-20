package com.lul.Stydu4.service.impl;


import com.lul.Stydu4.dto.request.QuestionGroup.QuestionGroupCreateRequest;
import com.lul.Stydu4.dto.request.QuestionGroup.QuestionGroupSearchRequest;
import com.lul.Stydu4.dto.request.QuestionGroup.QuestionGroupUpdateRequest;
import com.lul.Stydu4.dto.response.PageResponse;
import com.lul.Stydu4.dto.response.QuestionGroupResponse.QuestionGroupDetailResponse;
import com.lul.Stydu4.dto.response.QuestionGroupResponse.QuestionGroupSummaryResponse;
import com.lul.Stydu4.entity.PartTestEntity;
import com.lul.Stydu4.entity.QuestionGroupEntity;
import com.lul.Stydu4.entity.QuestionTestEntity;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.mapper.QuestionGroupMapper;
import com.lul.Stydu4.repository.IPartTestRepository;
import com.lul.Stydu4.repository.IQuestionGroupRepository;
import com.lul.Stydu4.repository.IQuestionTestRepository;
import com.lul.Stydu4.repository.specification.QuestionGroupSpecification;
import com.lul.Stydu4.service.IQuestionGroupService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class QuestionGroupServiceImpl implements IQuestionGroupService {

    IQuestionGroupRepository questionGroupRepository;
    IPartTestRepository partTestRepository;
    IQuestionTestRepository questionTestRepository;
    QuestionGroupMapper questionGroupMapper;

    @Override
    @Transactional
    public QuestionGroupDetailResponse create(QuestionGroupCreateRequest request) {
        log.info("Creating new question group with name: {}", request.getName());

        QuestionGroupEntity questionGroup = questionGroupMapper.toQuestionGroupEntity(request);


        QuestionGroupEntity saved = questionGroupRepository.save(questionGroup);
        log.info("Question group created successfully with id: {}", saved.getId());

        return questionGroupMapper.toQuestionGroupDetailResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<QuestionGroupSummaryResponse> getAllQuestionGroups(int page, int size) {
        log.info("Fetching all question groups - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<QuestionGroupEntity> questionGroupPage = questionGroupRepository.findAll(pageable);

        List<QuestionGroupSummaryResponse> responses = questionGroupPage.getContent()
                .stream()
                .map(questionGroupMapper::toQuestionGroupSummaryResponse)
                .toList();

        return PageResponse.<QuestionGroupSummaryResponse>builder()
                .currentPage(page)
                .pageSize(questionGroupPage.getSize())
                .totalPages(questionGroupPage.getTotalPages())
                .totalElements(questionGroupPage.getTotalElements())
                .data(responses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<QuestionGroupSummaryResponse> searchQuestionGroups(
            QuestionGroupSearchRequest request,
            Pageable pageable
    ) {
        log.info("Searching question groups with criteria: {}", request);

        Specification<QuestionGroupEntity> spec = QuestionGroupSpecification.buildSpecification(request);
        Page<QuestionGroupEntity> questionGroupPage = questionGroupRepository.findAll(spec,pageable);

        List<QuestionGroupSummaryResponse> responses = questionGroupPage.getContent()
                .stream()
                .map(questionGroupMapper::toQuestionGroupSummaryResponse)
                .toList();

        return PageResponse.<QuestionGroupSummaryResponse>builder()
                .currentPage(pageable.getPageNumber() + 1)
                .pageSize(questionGroupPage.getSize())
                .totalPages(questionGroupPage.getTotalPages())
                .totalElements(questionGroupPage.getTotalElements())
                .data(responses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionGroupDetailResponse getQuestionGroupById(String questionGroupId) {
        log.info("Fetching question group with id: {}", questionGroupId);

        QuestionGroupEntity questionGroup = questionGroupRepository.findById(questionGroupId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_GROUP_NOT_FOUND));

        return questionGroupMapper.toQuestionGroupDetailResponse(questionGroup);
    }

    @Override
    @Transactional
    public QuestionGroupDetailResponse update(
            String questionGroupId,
            QuestionGroupUpdateRequest request
    ) {
        log.info("Updating question group with id: {}", questionGroupId);

        QuestionGroupEntity existing = questionGroupRepository.findById(questionGroupId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_GROUP_NOT_FOUND));

        // Update basic fields
        if (request.getName() != null && !request.getName().isBlank()) {
            existing.setName(request.getName());
        }

        if (request.getContent() != null) {
            existing.setContent(request.getContent());
        }

        if (request.getType() != null) {
            existing.setType(request.getType());
        }

        if (request.getAudioPath() != null) {
            existing.setAudioPath(request.getAudioPath());
        }

        if (request.getImage() != null) {
            existing.setImage(request.getImage());
        }

        // Update PartEntity relationship
        if (request.getPartId() != null) {
            PartTestEntity part = partTestRepository.findById(request.getPartId().toString())
                    .orElseThrow(() -> new AppException(ErrorCode.PART_TEST_NOT_FOUND));
            existing.setPartEntity(part);
        }

        // Update Questions (OneToMany relationship)
        if (request.getQuestionIds() != null && !request.getQuestionIds().isEmpty()) {
            List<QuestionTestEntity> newQuestions = questionTestRepository
                    .findAllById(request.getQuestionIds());

            if (newQuestions.size() != request.getQuestionIds().size()) {
                throw new AppException(ErrorCode.QUESTION_NOT_FOUND);
            }

            // Clear existing and add new - ensures orphanRemoval works
            existing.getQuestions().clear();
            newQuestions.forEach(question -> {
                question.setQuestionGroupEntity(existing);
                existing.getQuestions().add(question);
            });
        }

        QuestionGroupEntity updated = questionGroupRepository.save(existing);
        log.info("Question group updated successfully with id: {}", updated.getId());

        return questionGroupMapper.toQuestionGroupDetailResponse(updated);
    }

    @Override
    @Transactional
    public void deleteQuestionGroup(String questionGroupId) {
        log.info("Deleting question group with id: {}", questionGroupId);

        if (!questionGroupRepository.existsById(questionGroupId)) {
            throw new AppException(ErrorCode.QUESTION_GROUP_NOT_FOUND);
        }

        questionGroupRepository.deleteById(questionGroupId);
        log.info("Question group deleted successfully with id: {}", questionGroupId);
    }
}
