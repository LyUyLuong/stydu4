package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.request.QuestionGroup.QuestionGroupCreateRequest;
import com.lul.Stydu4.dto.request.QuestionGroup.QuestionGroupSearchRequest;
import com.lul.Stydu4.dto.request.QuestionGroup.QuestionGroupUpdateRequest;
import com.lul.Stydu4.dto.response.PageResponse;
import com.lul.Stydu4.dto.response.QuestionGroupResponse.QuestionGroupDetailResponse;
import com.lul.Stydu4.dto.response.QuestionGroupResponse.QuestionGroupSummaryResponse;
import com.lul.Stydu4.entity.*;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.enums.FileType;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.mapper.QuestionGroupMapper;
import com.lul.Stydu4.repository.*;
import com.lul.Stydu4.repository.specification.QuestionGroupSpecification;
import com.lul.Stydu4.service.IFileStorageService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class QuestionGroupServiceImpl implements IQuestionGroupService {

    IQuestionGroupRepository questionGroupRepository;
    IPartTestRepository partTestRepository;
    IQuestionTestRepository questionTestRepository;
    IAnswerRepository answerRepository;
    IFileRepository fileRepository;
    QuestionGroupMapper questionGroupMapper;
    IFileStorageService fileStorageService;

    @Override
    @Transactional
    public QuestionGroupDetailResponse create(QuestionGroupCreateRequest request) {
        log.info("Creating new question group with name: {}", request.getName());

        QuestionGroupEntity questionGroup = questionGroupMapper.toQuestionGroupEntity(request);

        // ✅ NEW: Handle imageId if provided
        if (request.getImageId() != null && !request.getImageId().isBlank()) {
            FileEntity imageFile = fileRepository.findById(request.getImageId())
                    .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));
            questionGroup.setImage(imageFile);
        }

        // ✅ NEW: Handle audioId if provided
        if (request.getAudioId() != null && !request.getAudioId().isBlank()) {
            FileEntity audioFile = fileRepository.findById(request.getAudioId())
                    .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));
            questionGroup.setAudio(audioFile);
        }

        QuestionGroupEntity saved = questionGroupRepository.save(questionGroup);
        log.info("Question group created successfully with id: {}", saved.getId());

        return questionGroupMapper.toQuestionGroupDetailResponse(saved);
    }


    @Override
    @Transactional
    public QuestionGroupDetailResponse createWithFiles(
            QuestionGroupCreateRequest request,
            MultipartFile audio,
            MultipartFile image
    ) {
        log.info("Creating question group with files: {}", request.getName());

        QuestionGroupEntity entity = questionGroupMapper.toQuestionGroupEntity(request);

        if(request.getPartId() != null) {
            PartTestEntity part = partTestRepository.findById(request.getPartId())
                    .orElseThrow(() -> new AppException(ErrorCode.PART_TEST_NOT_FOUND));
            entity.setPartEntity(part);
        }

        // ✅ Upload Audio file if provided
        if (audio != null && !audio.isEmpty()) {
            FileEntity audioFile = fileStorageService.storeFile(audio, FileType.AUDIO, "question-groups");
            entity.setAudio(audioFile);
            log.info("Audio uploaded with ID: {}", audioFile.getId());
        }

        // ✅ Upload Image file if provided
        if (image != null && !image.isEmpty()) {
            FileEntity imageFile = fileStorageService.storeFile(image, FileType.IMAGE, "question-groups");
            entity.setImage(imageFile);
            log.info("Image uploaded with ID: {}", imageFile.getId());
        }

        QuestionGroupEntity saved = questionGroupRepository.save(entity);
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

        // ✅ Build custom Pageable from request params (same as QuestionTestServiceImpl)
        int idx = Math.max(0, request.getPage() - 1);
        int size = Math.min(request.getSize(), 100);
        Pageable pg = PageRequest.of(idx, size, pageable.getSort());

        Specification<QuestionGroupEntity> spec = QuestionGroupSpecification.buildSpecification(request);
        Page<QuestionGroupEntity> questionGroupPage = questionGroupRepository.findAll(spec, pg);

        List<QuestionGroupSummaryResponse> responses = questionGroupPage.getContent()
                .stream()
                .map(questionGroupMapper::toQuestionGroupSummaryResponse)
                .toList();

        log.info("searchQuestionGroups found {} groups, page={}, size={}, total={}",
                responses.size(), idx + 1, size, questionGroupPage.getTotalElements());

        return PageResponse.<QuestionGroupSummaryResponse>builder()
                .currentPage(idx + 1)
                .pageSize(size)
                .totalPages(questionGroupPage.getTotalPages())
                .totalElements(questionGroupPage.getTotalElements())
                .data(responses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionGroupDetailResponse getQuestionGroupById(String questionGroupId) {
        log.info("Fetching question group with id: {}", questionGroupId);

        // Q1: group + audio + image (1 row, 2 LEFT JOIN)
        QuestionGroupEntity questionGroup = questionGroupRepository.findByIdWithMedia(questionGroupId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_GROUP_NOT_FOUND));

        // Q2: questions của group (kèm audio + image)
        List<QuestionTestEntity> questions = questionTestRepository
                .findByGroupIdsWithMedia(List.of(questionGroupId));

        // Q3: answers cho các question đó
        if (!questions.isEmpty()) {
            List<String> qIds = questions.stream()
                    .map(QuestionTestEntity::getId)
                    .toList();

            Map<String, List<AnswerEntity>> answersByQ = answerRepository.findByQuestionIds(qIds).stream()
                    .collect(Collectors.groupingBy(a -> a.getQuestion().getId()));

            questions.forEach(q -> q.setAnswers(
                    new ArrayList<>(answersByQ.getOrDefault(q.getId(), List.of()))));
        }

        // Lắp ráp in-memory rồi map sang DTO
        questionGroup.setQuestions(new ArrayList<>(questions));

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

        // ❌ OLD: Remove these
        // if (request.getAudioPath() != null) {
        //     existing.setAudioPath(request.getAudioPath());
        // }
        // if (request.getImage() != null) {
        //     existing.setImage(request.getImage());
        // }

        // ✅ NEW: Handle imageId update
        if (request.getImageId() != null && !request.getImageId().isBlank()) {
            FileEntity imageFile = fileRepository.findById(request.getImageId())
                    .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));
            existing.setImage(imageFile);
        }

        // ✅ NEW: Handle audioId update
        if (request.getAudioId() != null && !request.getAudioId().isBlank()) {
            FileEntity audioFile = fileRepository.findById(request.getAudioId())
                    .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));
            existing.setAudio(audioFile);
        }

        // Update PartEntity relationship
        if (request.getPartId() != null) {
            PartTestEntity part = partTestRepository.findById(request.getPartId())
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

    @Override
    @Transactional
    public QuestionGroupDetailResponse updateGroupAudio(String questionGroupId, MultipartFile audio) {
        log.info("Updating audio for question group: {}", questionGroupId);

        QuestionGroupEntity group = questionGroupRepository.findById(questionGroupId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_GROUP_NOT_FOUND));

        // Delete old audio if exists
        if (group.getAudio() != null) {
            String oldAudioId = group.getAudio().getId();
            group.setAudio(null);
            questionGroupRepository.save(group);

            try {
                fileStorageService.deleteFile(oldAudioId);
                log.info("Old audio deleted: {}", oldAudioId);
            } catch (Exception e) {
                log.warn("Failed to delete old audio: {}", oldAudioId, e);
            }
        }

        // Upload new audio
        if (audio != null && !audio.isEmpty()) {
            FileEntity newAudio = fileStorageService.storeFile(audio, FileType.AUDIO, "question-groups");
            group.setAudio(newAudio);
            log.info("New audio uploaded with ID: {}", newAudio.getId());
        }

        QuestionGroupEntity saved = questionGroupRepository.save(group);
        return questionGroupMapper.toQuestionGroupDetailResponse(saved);
    }

    @Override
    @Transactional
    public QuestionGroupDetailResponse updateGroupImage(String questionGroupId, MultipartFile image) {
        log.info("Updating image for question group: {}", questionGroupId);

        QuestionGroupEntity group = questionGroupRepository.findById(questionGroupId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_GROUP_NOT_FOUND));

        // Delete old image if exists
        if (group.getImage() != null) {
            String oldImageId = group.getImage().getId();
            group.setImage(null);
            questionGroupRepository.save(group);

            try {
                fileStorageService.deleteFile(oldImageId);
                log.info("Old image deleted: {}", oldImageId);
            } catch (Exception e) {
                log.warn("Failed to delete old image: {}", oldImageId, e);
            }
        }

        // Upload new image
        if (image != null && !image.isEmpty()) {
            FileEntity newImage = fileStorageService.storeFile(image, FileType.IMAGE, "question-groups");
            group.setImage(newImage);
            log.info("New image uploaded with ID: {}", newImage.getId());
        }

        QuestionGroupEntity saved = questionGroupRepository.save(group);
        return questionGroupMapper.toQuestionGroupDetailResponse(saved);
    }
}
