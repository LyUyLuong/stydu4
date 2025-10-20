package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.request.Question.QuestionTestCreateRequest;
import com.lul.Stydu4.dto.request.Question.QuestionTestSearchRequest;
import com.lul.Stydu4.dto.request.Question.QuestionTestUpdateRequest;
import com.lul.Stydu4.dto.response.PageResponse;
import com.lul.Stydu4.dto.response.Question.QuestionTestDetailResponse;
import com.lul.Stydu4.dto.response.Question.QuestionTestSummaryResponse;
import com.lul.Stydu4.entity.AnswerEntity;
import com.lul.Stydu4.entity.PartTestEntity;
import com.lul.Stydu4.entity.QuestionGroupEntity;
import com.lul.Stydu4.entity.QuestionTestEntity;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.enums.QuestionType;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.mapper.AnswerMapper;
import com.lul.Stydu4.mapper.QuestionTestMapper;
import com.lul.Stydu4.repository.IAnswerRepository;
import com.lul.Stydu4.repository.IPartTestRepository;
import com.lul.Stydu4.repository.IQuestionGroupRepository;
import com.lul.Stydu4.repository.IQuestionTestRepository;
import com.lul.Stydu4.repository.specification.QuestionTestSpecification;
import com.lul.Stydu4.service.IQuestionTestService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.lul.Stydu4.util.EnumValidator.validateAndConvert;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class QuestionTestServiceImpl implements IQuestionTestService {

    IQuestionTestRepository questionTestRepository;
    IPartTestRepository partTestRepository;
    IQuestionGroupRepository questionGroupRepository;
    IAnswerRepository answerRepository;
    QuestionTestMapper questionTestMapper;
    AnswerMapper answerMapper;

    @Override
    @Transactional
    public QuestionTestDetailResponse create(QuestionTestCreateRequest request) {
        log.info("Creating new question test with name: {}", request.getName());

        // Map basic question fields
        QuestionTestEntity questionTest = questionTestMapper.toQuestionTestEntity(request);

        // Validate và convert type từ String → Enum
        QuestionType questionType = validateAndConvert(
                request.getType(),
                QuestionType.class,
                ErrorCode.INVALID_QUESTION_TYPE
        );
        questionTest.setType(questionType);

        // Set PartEntity if partId provided
        if (request.getPartId() != null && !request.getPartId().isBlank()) {
            PartTestEntity part = partTestRepository.findById(request.getPartId())
                    .orElseThrow(() -> new AppException(ErrorCode.PART_TEST_NOT_FOUND));
            questionTest.setPartEntity(part);
        }

        // Set QuestionGroupEntity if questionGroupId provided
        if (request.getQuestionGroupId() != null && !request.getQuestionGroupId().isBlank()) {
            QuestionGroupEntity questionGroup = questionGroupRepository
                    .findById(request.getQuestionGroupId())
                    .orElseThrow(() -> new AppException(ErrorCode.QUESTION_GROUP_NOT_FOUND));
            questionTest.setQuestionGroupEntity(questionGroup);
        }

        // Create answers from request
        if (request.getAnswers() != null && !request.getAnswers().isEmpty()) {
            List<AnswerEntity> answerEntities = request.getAnswers().stream()
                    .map(answerRequest -> {
                        AnswerEntity answer = answerMapper.toAnswerEntity(answerRequest);
                        answer.setQuestion(questionTest);
                        return answer;
                    })
                    .collect(Collectors.toList());

            questionTest.setAnswers(answerEntities);
        }

        // Save question (cascade will save answers automatically)
        QuestionTestEntity saved = questionTestRepository.save(questionTest);
        log.info("Question test created successfully with id: {} and {} answers",
                saved.getId(), saved.getAnswers().size());

        return questionTestMapper.toQuestionDetailResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<QuestionTestSummaryResponse> getAllQuestionTests(int page, int size) {
        log.info("Fetching all question tests - page: {}, size: {}", page, size);

        int pageNo = page > 0 ? page - 1 : 0;
        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate");
        Pageable pageable = PageRequest.of(pageNo, size, sort);

        Page<QuestionTestEntity> questionTestPage = questionTestRepository.findAll(pageable);

        List<QuestionTestSummaryResponse> responses = questionTestPage.getContent()
                .stream()
                .map(questionTestMapper::toQuestionSummaryResponse)
                .toList();

        return PageResponse.<QuestionTestSummaryResponse>builder()
                .currentPage(page)
                .pageSize(questionTestPage.getSize())
                .totalPages(questionTestPage.getTotalPages())
                .totalElements(questionTestPage.getTotalElements())
                .data(responses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<QuestionTestSummaryResponse> searchQuestionTests(
            QuestionTestSearchRequest request,
            Pageable pageable
    ) {
        log.info("Searching question tests with criteria: {}", request);

        int idx = Math.max(0, request.getPage() - 1);
        int size = Math.min(request.getSize(), 100);
        Sort sort = sanitizeSort(pageable.getSort());
        Pageable pg = PageRequest.of(idx, size, sort);

        long t0 = System.nanoTime();
        Page<QuestionTestEntity> page = questionTestRepository.findAll(
                QuestionTestSpecification.buildSpecification(request), pg
        );
        long t1 = System.nanoTime();

        List<QuestionTestSummaryResponse> responses = page.getContent()
                .stream()
                .map(questionTestMapper::toQuestionSummaryResponse)
                .toList();

        log.info("searchQuestionTests latency={}ms, page={}, size={}, total={}",
                (t1 - t0) / 1_000_000, idx + 1, size, page.getTotalElements());

        return PageResponse.<QuestionTestSummaryResponse>builder()
                .currentPage(idx + 1)
                .pageSize(size)
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .data(responses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionTestDetailResponse getQuestionTestById(String questionTestId) {
        log.info("Fetching question test with id: {}", questionTestId);

        QuestionTestEntity questionTest = questionTestRepository.findById(questionTestId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

        return questionTestMapper.toQuestionDetailResponse(questionTest);
    }

    @Override
    @Transactional
    public QuestionTestDetailResponse update(
            String questionTestId,
            QuestionTestUpdateRequest request
    ) {
        log.info("Updating question test with id: {}", questionTestId);

        QuestionTestEntity existing = questionTestRepository.findById(questionTestId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

        // Update basic fields
        if (request.getName() != null && !request.getName().isBlank()) {
            existing.setName(request.getName());
        }

        if (request.getContent() != null) {
            existing.setContent(request.getContent());
        }

        // Validate và convert type
        if (request.getType() != null && !request.getType().isBlank()) {
            QuestionType questionType = validateAndConvert(
                    request.getType(),
                    QuestionType.class,
                    ErrorCode.INVALID_QUESTION_TYPE
            );
            existing.setType(questionType);
        }

        if (request.getAudioPath() != null) {
            existing.setAudioPath(request.getAudioPath());
        }

        if (request.getImage() != null) {
            existing.setImage(request.getImage());
        }

        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }

        // Update PartEntity relationship
        if (request.getPartId() != null && !request.getPartId().isBlank()) {
            PartTestEntity part = partTestRepository.findById(request.getPartId())
                    .orElseThrow(() -> new AppException(ErrorCode.PART_TEST_NOT_FOUND));
            existing.setPartEntity(part);
        }

        // Update QuestionGroup relationship
        if (request.getQuestionGroupId() != null && !request.getQuestionGroupId().isBlank()) {
            QuestionGroupEntity questionGroup = questionGroupRepository
                    .findById(request.getQuestionGroupId())
                    .orElseThrow(() -> new AppException(ErrorCode.QUESTION_GROUP_NOT_FOUND));
            existing.setQuestionGroupEntity(questionGroup);
        }

        // Update Answers
        if (request.getAnswerIds() != null && !request.getAnswerIds().isEmpty()) {
            List<AnswerEntity> newAnswers = answerRepository.findAllById(request.getAnswerIds());

            if (newAnswers.size() != request.getAnswerIds().size()) {
                throw new AppException(ErrorCode.ANSWER_NOT_FOUND);
            }

            // Clear existing and add new
            existing.getAnswers().clear();
            newAnswers.forEach(answer -> {
                answer.setQuestion(existing);
                existing.getAnswers().add(answer);
            });
        }

        QuestionTestEntity updated = questionTestRepository.save(existing);
        log.info("Question test updated successfully with id: {}", updated.getId());

        return questionTestMapper.toQuestionDetailResponse(updated);
    }

    @Override
    @Transactional
    public void deleteQuestionTest(String questionTestId) {
        log.info("Deleting question test with id: {}", questionTestId);

        if (!questionTestRepository.existsById(questionTestId)) {
            throw new AppException(ErrorCode.QUESTION_NOT_FOUND);
        }

        questionTestRepository.deleteById(questionTestId);
        log.info("Question test deleted successfully with id: {}", questionTestId);
    }

    // ============ HELPER METHODS ============

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "name", "content", "type", "createdDate", "modifiedDate"
    );

    private Sort sanitizeSort(Sort sort) {
        if (sort.isUnsorted()) {
            log.info("No sort provided, using default: createdDate DESC");
            return Sort.by(Sort.Direction.DESC, "createdDate");
        }

        List<Sort.Order> validOrders = sort.stream()
                .filter(order -> {
                    boolean valid = ALLOWED_SORT_FIELDS.contains(order.getProperty());
                    if (!valid) {
                        log.warn("Invalid sort field '{}' detected, skipping", order.getProperty());
                    }
                    return valid;
                })
                .toList();

        if (validOrders.isEmpty()) {
            log.warn("All sort fields were invalid, using default: createdDate DESC");
            return Sort.by(Sort.Direction.DESC, "createdDate");
        }

        return Sort.by(validOrders);
    }
}
