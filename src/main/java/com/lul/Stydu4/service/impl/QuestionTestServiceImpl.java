package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.request.Question.QuestionTestCreateRequest;
import com.lul.Stydu4.dto.request.Question.QuestionTestSearchRequest;
import com.lul.Stydu4.dto.request.Question.QuestionTestUpdateRequest;
import com.lul.Stydu4.dto.response.PageResponse;
import com.lul.Stydu4.dto.response.Question.QuestionTestDetailResponse;
import com.lul.Stydu4.dto.response.Question.QuestionTestSummaryResponse;
import com.lul.Stydu4.entity.AnswerEntity;
import com.lul.Stydu4.entity.FileEntity;
import com.lul.Stydu4.entity.PartTestEntity;
import com.lul.Stydu4.entity.QuestionGroupEntity;
import com.lul.Stydu4.entity.QuestionTestEntity;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.enums.FileType;
import com.lul.Stydu4.enums.QuestionType;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.mapper.AnswerMapper;
import com.lul.Stydu4.mapper.QuestionTestMapper;
import com.lul.Stydu4.repository.IAnswerRepository;
import com.lul.Stydu4.repository.IFileRepository;
import com.lul.Stydu4.repository.IPartTestRepository;
import com.lul.Stydu4.repository.IQuestionGroupRepository;
import com.lul.Stydu4.repository.IQuestionTestRepository;
import com.lul.Stydu4.repository.specification.QuestionTestSpecification;
import com.lul.Stydu4.service.IFileStorageService;
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
import org.springframework.web.multipart.MultipartFile;

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
    IFileRepository fileRepository;
    QuestionTestMapper questionTestMapper;
    AnswerMapper answerMapper;
    IFileStorageService fileStorageService;

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

        // ✅ Handle imageId if provided
        if (request.getImageId() != null && !request.getImageId().isBlank()) {
            FileEntity imageFile = fileRepository.findById(request.getImageId())
                    .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));
            questionTest.setImage(imageFile);
        }

        // ✅ Handle audioId if provided
        if (request.getAudioId() != null && !request.getAudioId().isBlank()) {
            FileEntity audioFile = fileRepository.findById(request.getAudioId())
                    .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));
            questionTest.setAudio(audioFile);
        }

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

        QuestionTestDetailResponse response = questionTestMapper.toQuestionDetailResponse(questionTest);
        
        log.info("Question fetched - Audio: {} (URL: {}), Image: {} (URL: {})",
                response.getAudioId(), response.getAudioUrl(),
                response.getImageId(), response.getImageUrl());
        
        return response;
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

        // ✅ Handle imageId update
        if (request.getImageId() != null && !request.getImageId().isBlank()) {
            FileEntity imageFile = fileRepository.findById(request.getImageId())
                    .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));
            existing.setImage(imageFile);
        }

        // ✅ Handle audioId update
        if (request.getAudioId() != null && !request.getAudioId().isBlank()) {
            FileEntity audioFile = fileRepository.findById(request.getAudioId())
                    .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));
            existing.setAudio(audioFile);
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

        // ✅ Update Answers - Delete old answers and create new ones
        if (request.getAnswers() != null && !request.getAnswers().isEmpty()) {
            // Clear existing answers (cascade delete should handle this)
            existing.getAnswers().clear();
            
            // Create new answers
            List<AnswerEntity> newAnswers = request.getAnswers().stream()
                    .map(answerReq -> {
                        AnswerEntity answer = AnswerEntity.builder()
                                .content(answerReq.getContent())
                                .isCorrect(answerReq.getIsCorrect())
                                .mark(answerReq.getMark())
                                .question(existing)
                                .build();
                        return answer;
                    })
                    .toList();
            
            existing.getAnswers().addAll(newAnswers);
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


    @Override
    @Transactional
    public QuestionTestDetailResponse createWithFiles(
            QuestionTestCreateRequest request,
            MultipartFile audio,
            MultipartFile image
    ) {
        log.info("=== QuestionTestServiceImpl.createWithFiles ===");
        log.info("Question name: {}", request.getName());
        log.info("Audio file: {} (isEmpty: {})", 
                audio != null ? audio.getOriginalFilename() : "null",
                audio != null ? audio.isEmpty() : "null");
        log.info("Image file: {} (isEmpty: {})", 
                image != null ? image.getOriginalFilename() : "null",
                image != null ? image.isEmpty() : "null");

        QuestionTestEntity entity = questionTestMapper.toQuestionTestEntity(request);

        // Validate and set QuestionType
        QuestionType questionType = validateAndConvert(
                request.getType(),
                QuestionType.class,
                ErrorCode.INVALID_QUESTION_TYPE
        );
        entity.setType(questionType);

        // Set Part (optional)
        if (request.getPartId() != null && !request.getPartId().isBlank()) {
            PartTestEntity part = partTestRepository.findById(request.getPartId())
                    .orElseThrow(() -> new AppException(ErrorCode.PART_TEST_NOT_FOUND));
            entity.setPartEntity(part);
        }

        // Set QuestionGroup (optional)
        if (request.getQuestionGroupId() != null && !request.getQuestionGroupId().isBlank()) {
            QuestionGroupEntity group = questionGroupRepository.findById(request.getQuestionGroupId())
                    .orElseThrow(() -> new AppException(ErrorCode.QUESTION_GROUP_NOT_FOUND));
            entity.setQuestionGroupEntity(group);
        }

        // ✅ Upload Audio file if provided
        if (audio != null && !audio.isEmpty()) {
            FileEntity audioFile = fileStorageService.storeFile(audio, FileType.AUDIO, "questions");
            entity.setAudio(audioFile);
            log.info("Audio uploaded with ID: {}", audioFile.getId());
        }

        // ✅ Upload Image file if provided
        if (image != null && !image.isEmpty()) {
            FileEntity imageFile = fileStorageService.storeFile(image, FileType.IMAGE, "questions");
            entity.setImage(imageFile);
            log.info("Image uploaded with ID: {}", imageFile.getId());
        }

        // Save question first
        QuestionTestEntity savedQuestion = questionTestRepository.save(entity);

        // Create answers
        if (request.getAnswers() != null && !request.getAnswers().isEmpty()) {
            List<AnswerEntity> answers = request.getAnswers().stream()
                    .map(answerRequest -> {
                        AnswerEntity answer = answerMapper.toAnswerEntity(answerRequest);
                        answer.setQuestion(savedQuestion);
                        return answer;
                    })
                    .collect(Collectors.toList());

            answerRepository.saveAll(answers);
            savedQuestion.setAnswers(answers);
        }

        return questionTestMapper.toQuestionDetailResponse(savedQuestion);
    }

    @Override
    @Transactional
    public QuestionTestDetailResponse updateQuestionAudio(String questionTestId, MultipartFile audio) {
        log.info("Updating audio for question: {}", questionTestId);

        QuestionTestEntity question = questionTestRepository.findById(questionTestId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

        // Delete old audio if exists
        if (question.getAudio() != null) {
            String oldAudioId = question.getAudio().getId();
            question.setAudio(null);
            questionTestRepository.save(question);

            try {
                fileStorageService.deleteFile(oldAudioId);
                log.info("Old audio deleted: {}", oldAudioId);
            } catch (Exception e) {
                log.warn("Failed to delete old audio: {}", oldAudioId, e);
            }
        }

        // Upload new audio
        if (audio != null && !audio.isEmpty()) {
            FileEntity newAudio = fileStorageService.storeFile(audio, FileType.AUDIO, "questions");
            question.setAudio(newAudio);
            log.info("New audio uploaded with ID: {}", newAudio.getId());
        }

        QuestionTestEntity saved = questionTestRepository.save(question);
        return questionTestMapper.toQuestionDetailResponse(saved);
    }

    @Override
    @Transactional
    public QuestionTestDetailResponse updateQuestionImage(String questionTestId, MultipartFile image) {
        log.info("Updating image for question: {}", questionTestId);

        QuestionTestEntity question = questionTestRepository.findById(questionTestId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

        // Delete old image if exists
        if (question.getImage() != null) {
            String oldImageId = question.getImage().getId();
            question.setImage(null);
            questionTestRepository.save(question);

            try {
                fileStorageService.deleteFile(oldImageId);
                log.info("Old image deleted: {}", oldImageId);
            } catch (Exception e) {
                log.warn("Failed to delete old image: {}", oldImageId, e);
            }
        }

        // Upload new image
        if (image != null && !image.isEmpty()) {
            FileEntity newImage = fileStorageService.storeFile(image, FileType.IMAGE, "questions");
            question.setImage(newImage);
            log.info("New image uploaded with ID: {}", newImage.getId());
        }

        QuestionTestEntity saved = questionTestRepository.save(question);
        return questionTestMapper.toQuestionDetailResponse(saved);
    }
}
