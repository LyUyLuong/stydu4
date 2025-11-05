package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.request.PartTest.PartTestCreationRequest;
import com.lul.Stydu4.dto.request.PartTest.PartTestSearchRequest;
import com.lul.Stydu4.entity.QuestionGroupEntity;
import com.lul.Stydu4.entity.QuestionTestEntity;
import com.lul.Stydu4.repository.specification.PartTestSpecification;
import com.lul.Stydu4.dto.request.PartTest.PartTestUpdateRequest;
import com.lul.Stydu4.dto.response.PageResponse;
import com.lul.Stydu4.dto.response.PartTest.PartTestDetailResponse;
import com.lul.Stydu4.dto.response.PartTest.PartTestSummaryResponse;
import com.lul.Stydu4.entity.PartTestEntity;
import com.lul.Stydu4.entity.TestEntity;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.enums.PartType;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.mapper.PartTestMapper;
import com.lul.Stydu4.repository.IPartTestRepository;
import com.lul.Stydu4.repository.IQuestionGroupRepository;
import com.lul.Stydu4.repository.IQuestionTestRepository;
import com.lul.Stydu4.repository.ITestRepository;
import com.lul.Stydu4.service.IPartTestService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.lul.Stydu4.util.EnumValidator.validateAndConvert;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PartTestServiceImpl implements IPartTestService {

    IPartTestRepository partTestRepository;
    ITestRepository testRepository;
    PartTestMapper partTestMapper;
    IQuestionTestRepository questionTestRepository;
    IQuestionGroupRepository questionGroupRepository;

    @Override
    public PartTestDetailResponse create(PartTestCreationRequest request) {
        // Map basic fields
        PartTestEntity entity = partTestMapper.toPartTestEntity(request);

        PartType partType = validateAndConvert(
                request.getType(),
                PartType.class,
                ErrorCode.INVALID_PART_TYPE
        );
        entity.setType(partType);

        // Gán testEntity nếu testId hợp lệ
        if (request.getTestId() != null) {
            TestEntity test = testRepository.findById(request.getTestId())
                    .orElseThrow(() -> new AppException(ErrorCode.TEST_NOT_FOUND));
            entity.setTestEntity(test);
        }

        return partTestMapper.toPartTestResponse(partTestRepository.save(entity));
    }

    @Override
    @Transactional
    public PartTestDetailResponse update(String partTestID, PartTestUpdateRequest request) {
        PartTestEntity existing = partTestRepository.findById(partTestID)
                .orElseThrow(() -> new AppException(ErrorCode.PART_TEST_NOT_FOUND));

        // Update basic fields
        if (request.getName() != null && !request.getName().isBlank()) {
            existing.setName(request.getName());
        }

        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }

        if (request.getType() != null) {
            PartType partType = validateAndConvert(
                    request.getType(),
                    PartType.class,
                    ErrorCode.INVALID_PART_TYPE
            );
            existing.setType(partType);
        }


        if (request.getQuestionIds() != null) {
            // ✅ Remove old relationships without deleting entities
            List<QuestionTestEntity> currentQuestions = new ArrayList<>(existing.getQuestions());
            currentQuestions.forEach(q -> q.setPartEntity(null));
            existing.getQuestions().clear();

            if (!request.getQuestionIds().isEmpty()) {
                List<QuestionTestEntity> newQuestions = questionTestRepository
                        .findAllById(request.getQuestionIds());

                if (newQuestions.size() != request.getQuestionIds().size()) {
                    throw new AppException(ErrorCode.QUESTION_NOT_FOUND);
                }

                newQuestions.forEach(existing::addQuestion);
            }
        }


        if (request.getQuestionGroupsIds() != null ) {
            // ✅ Remove old relationships without deleting entities
            List<QuestionGroupEntity> currentGroups = new ArrayList<>(existing.getQuestionGroups());
            currentGroups.forEach(g -> g.setPartEntity(null));
            existing.getQuestionGroups().clear();

            if (!request.getQuestionGroupsIds().isEmpty()) {
                List<QuestionGroupEntity> newGroups = questionGroupRepository
                        .findAllById(request.getQuestionGroupsIds());

                if (newGroups.size() != request.getQuestionGroupsIds().size()) {
                    throw new AppException(ErrorCode.QUESTION_GROUP_NOT_FOUND);
                }

                newGroups.forEach(existing::addQuestionGroup);
            }
        }

        if (request.getTestId() != null) {
            TestEntity test = testRepository.findById(request.getTestId())
                    .orElseThrow(() -> new AppException(ErrorCode.TEST_NOT_FOUND));
            existing.setTestEntity(test);
        }   

        PartTestEntity updated = partTestRepository.save(existing);
        return partTestMapper.toPartTestResponse(updated);
    }


    @Override
    public PageResponse<PartTestSummaryResponse> getAllPartTests(int page, int size) {

        int pageNo = page > 0 ? page - 1 : 0;

        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate");

        Pageable pageable = PageRequest.of(pageNo,size,sort);


        Page<PartTestEntity> partTestEntities = partTestRepository.findAllBy(pageable);

        List<PartTestSummaryResponse> partTestSummaries = partTestEntities.getContent().stream().map(partTestMapper::toPartTestSummary).toList();

        return PageResponse.<PartTestSummaryResponse>builder()
                .currentPage(pageNo+1)
                .totalPages(partTestEntities.getTotalPages())
                .totalElements(partTestEntities.getTotalElements())
                .pageSize(size)
                .data(partTestSummaries)
                .build();
    }

    @Override
    public PartTestDetailResponse getPartTestById(String partTestId) {
        PartTestEntity partTest = partTestRepository.findById(partTestId)
                .orElseThrow(() -> new AppException(ErrorCode.PART_TEST_NOT_FOUND));
        return partTestMapper.toPartTestResponse(partTest);
    }

    @Override
    public void deletePartTest(String partTestId) {
        partTestRepository.deleteById(partTestId);
    }



    @Transactional(readOnly = true)
    @Override
    public PageResponse<PartTestSummaryResponse> searchPartTests(PartTestSearchRequest request, Pageable pageable) {
        int idx = Math.max(0, request.getPage() - 1);
        int size = Math.min(request.getSize(), 100);
        Sort sort = sanitizeSort(pageable.getSort());
        Pageable pg = PageRequest.of(idx, size, sort);

        long t0 = System.nanoTime();
        Page<PartTestEntity> page = partTestRepository.findAll(
                PartTestSpecification.buildSpecification(request), pg);
        long t1 = System.nanoTime();

        List<PartTestSummaryResponse> data;

        if (page.isEmpty()) {
            // Empty page → skip count queries, return empty list
            data = List.of();
        } else {
            // Bulk count questions
            var partIds = page.getContent().stream().map(PartTestEntity::getId).toList();
            var questionCounts = questionTestRepository.countQuestionsByPartIds(partIds).stream()
                    .collect(Collectors.toMap(r -> (String) r[0], r -> (Long) r[1]));
            var questionGroupCounts = questionGroupRepository.countQuestionGroupsByPartIds(partIds).stream()
                    .collect(Collectors.toMap(r -> (String) r[0], r -> (Long) r[1]));

            data = page.getContent().stream().map(e -> {
                var dto = partTestMapper.toPartTestSummary(e);
                dto.setQuestionsCount(questionCounts.getOrDefault(e.getId(), 0L).intValue());
                dto.setQuestionGroupsCount(questionGroupCounts.getOrDefault(e.getId(), 0L).intValue());
                return dto;
            }).toList();
        }

        log.info("searchPartTests latency={}ms, page={}, size={}, total={}",
                (t1 - t0) / 1_000_000, idx + 1, size, page.getTotalElements());

        return PageResponse.<PartTestSummaryResponse>builder()
                .data(data)
                .currentPage(idx + 1)
                .pageSize(size)
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }


    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "name", "type", "status", "createdDate", "modifiedDate"
    );

    /**
     * Sanitize sort: whitelist fields và set default nếu empty
     */
    private Sort sanitizeSort(Sort sort) {
        if (sort.isUnsorted()) {
            log.info("No sort provided, using default: createdDate DESC");
            return Sort.by(Sort.Direction.DESC, "createdDate");
        }

        // Filter chỉ giữ các field hợp lệ
        List<Sort.Order> validOrders = sort.stream()
                .filter(order -> {
                    boolean valid = ALLOWED_SORT_FIELDS.contains(order.getProperty());
                    if (!valid) {
                        log.warn("Invalid sort field '{}' detected, skipping", order.getProperty());
                    }
                    return valid;
                })
                .toList();

        // Nếu tất cả field đều invalid, dùng default
        if (validOrders.isEmpty()) {
            log.warn("All sort fields were invalid, using default: createdDate DESC");
            return Sort.by(Sort.Direction.DESC, "createdDate");
        }

        return Sort.by(validOrders);
    }

}
