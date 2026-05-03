package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.request.QuestionGroup.QuestionGroupCreateRequest;
import com.lul.Stydu4.dto.request.QuestionGroup.QuestionGroupUpdateRequest;
import com.lul.Stydu4.dto.response.PageResponse;
import com.lul.Stydu4.dto.response.QuestionGroupResponse.QuestionGroupDetailResponse;
import com.lul.Stydu4.dto.response.QuestionGroupResponse.QuestionGroupSummaryResponse;
import com.lul.Stydu4.entity.FileEntity;
import com.lul.Stydu4.entity.PartTestEntity;
import com.lul.Stydu4.entity.QuestionGroupEntity;
import com.lul.Stydu4.entity.QuestionTestEntity;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.enums.FileType;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.mapper.QuestionGroupMapper;
import com.lul.Stydu4.repository.*;
import com.lul.Stydu4.service.IFileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuestionGroupServiceImpl Tests")
class QuestionGroupServiceImplTest {

    @Mock
    private IQuestionGroupRepository questionGroupRepository;

    @Mock
    private IPartTestRepository partTestRepository;

    @Mock
    private IQuestionTestRepository questionTestRepository;

    @Mock
    private IFileRepository fileRepository;

    @Mock
    private QuestionGroupMapper questionGroupMapper;

    @Mock
    private IFileStorageService fileStorageService;

    @Mock
    private IAnswerRepository answerRepository;

    @InjectMocks
    private QuestionGroupServiceImpl questionGroupService;

    private QuestionGroupEntity questionGroup;
    private QuestionGroupCreateRequest createRequest;
    private QuestionGroupDetailResponse detailResponse;
    private FileEntity imageFile;
    private FileEntity audioFile;
    private PartTestEntity partTest;

    @BeforeEach
    void setUp() {
        imageFile = FileEntity.builder()
                .id("image-123")
                .fileType(FileType.IMAGE)
                .originalFilename("test-image.jpg")
                .build();

        audioFile = FileEntity.builder()
                .id("audio-123")
                .fileType(FileType.AUDIO)
                .originalFilename("test-audio.mp3")
                .build();

        partTest = PartTestEntity.builder()
                .id("part-123")
                .name("Part 3")
                .build();

        createRequest = QuestionGroupCreateRequest.builder()
                .name("Question Group 1")
                .content("Test content")
                .type("NORMAL")
                .partId("part-123")
                .imageId("image-123")
                .audioId("audio-123")
                .build();

        questionGroup = QuestionGroupEntity.builder()
                .id("group-123")
                .name("Question Group 1")
                .content("Test content")
                .type("NORMAL")
                .partEntity(partTest)
                .image(imageFile)
                .audio(audioFile)
                .questions(new ArrayList<>())
                .build();

        detailResponse = QuestionGroupDetailResponse.builder()
                .id("group-123")
                .name("Question Group 1")
                .content("Test content")
                .build();
    }

    @Nested
    @DisplayName("create Tests")
    class CreateTests {

        @Test
        @DisplayName("Should create question group with image and audio")
        void create_WithFiles_Success() {
            // GIVEN
            when(questionGroupMapper.toQuestionGroupEntity(createRequest)).thenReturn(questionGroup);
            when(fileRepository.findById("image-123")).thenReturn(Optional.of(imageFile));
            when(fileRepository.findById("audio-123")).thenReturn(Optional.of(audioFile));
            when(questionGroupRepository.save(any(QuestionGroupEntity.class))).thenReturn(questionGroup);
            when(questionGroupMapper.toQuestionGroupDetailResponse(questionGroup)).thenReturn(detailResponse);

            // WHEN
            QuestionGroupDetailResponse result = questionGroupService.create(createRequest);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("group-123");

            verify(fileRepository).findById("image-123");
            verify(fileRepository).findById("audio-123");
            verify(questionGroupRepository).save(any(QuestionGroupEntity.class));
        }

        @Test
        @DisplayName("Should create question group without files")
        void create_WithoutFiles_Success() {
            // GIVEN
            createRequest.setImageId(null);
            createRequest.setAudioId(null);

            when(questionGroupMapper.toQuestionGroupEntity(createRequest)).thenReturn(questionGroup);
            when(questionGroupRepository.save(any(QuestionGroupEntity.class))).thenReturn(questionGroup);
            when(questionGroupMapper.toQuestionGroupDetailResponse(questionGroup)).thenReturn(detailResponse);

            // WHEN
            QuestionGroupDetailResponse result = questionGroupService.create(createRequest);

            // THEN
            assertThat(result).isNotNull();
            verify(fileRepository, never()).findById(anyString());
            verify(questionGroupRepository).save(any(QuestionGroupEntity.class));
        }

        @Test
        @DisplayName("Should throw exception when image not found")
        void create_ImageNotFound_ThrowException() {
            // GIVEN
            when(questionGroupMapper.toQuestionGroupEntity(createRequest)).thenReturn(questionGroup);
            when(fileRepository.findById("image-123")).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> questionGroupService.create(createRequest))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_NOT_FOUND);

            verify(questionGroupRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when audio not found")
        void create_AudioNotFound_ThrowException() {
            // GIVEN
            when(questionGroupMapper.toQuestionGroupEntity(createRequest)).thenReturn(questionGroup);
            when(fileRepository.findById("image-123")).thenReturn(Optional.of(imageFile));
            when(fileRepository.findById("audio-123")).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> questionGroupService.create(createRequest))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_NOT_FOUND);

            verify(questionGroupRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("createWithFiles Tests")
    class CreateWithFilesTests {

        @Test
        @DisplayName("Should create question group and upload files")
        void createWithFiles_Success() {
            // GIVEN
            MockMultipartFile audioMultipart = new MockMultipartFile(
                    "audio", "test.mp3", "audio/mpeg", "audio content".getBytes()
            );
            MockMultipartFile imageMultipart = new MockMultipartFile(
                    "image", "test.jpg", "image/jpeg", "image content".getBytes()
            );

            when(questionGroupMapper.toQuestionGroupEntity(createRequest)).thenReturn(questionGroup);
            when(partTestRepository.findById("part-123")).thenReturn(Optional.of(partTest));
            when(fileStorageService.storeFile(audioMultipart, FileType.AUDIO, "question-groups"))
                    .thenReturn(audioFile);
            when(fileStorageService.storeFile(imageMultipart, FileType.IMAGE, "question-groups"))
                    .thenReturn(imageFile);
            when(questionGroupRepository.save(any(QuestionGroupEntity.class))).thenReturn(questionGroup);
            when(questionGroupMapper.toQuestionGroupDetailResponse(questionGroup)).thenReturn(detailResponse);

            // WHEN
            QuestionGroupDetailResponse result = questionGroupService.createWithFiles(
                    createRequest, audioMultipart, imageMultipart
            );

            // THEN
            assertThat(result).isNotNull();
            verify(fileStorageService).storeFile(audioMultipart, FileType.AUDIO, "question-groups");
            verify(fileStorageService).storeFile(imageMultipart, FileType.IMAGE, "question-groups");
            verify(questionGroupRepository).save(any(QuestionGroupEntity.class));
        }

        @Test
        @DisplayName("Should create without files when files are null")
        void createWithFiles_NullFiles_Success() {
            // GIVEN
            when(questionGroupMapper.toQuestionGroupEntity(createRequest)).thenReturn(questionGroup);
            when(partTestRepository.findById("part-123")).thenReturn(Optional.of(partTest));
            when(questionGroupRepository.save(any(QuestionGroupEntity.class))).thenReturn(questionGroup);
            when(questionGroupMapper.toQuestionGroupDetailResponse(questionGroup)).thenReturn(detailResponse);

            // WHEN
            QuestionGroupDetailResponse result = questionGroupService.createWithFiles(
                    createRequest, null, null
            );

            // THEN
            assertThat(result).isNotNull();
            verify(fileStorageService, never()).storeFile(any(), any(), anyString());
        }

        @Test
        @DisplayName("Should throw exception when part not found")
        void createWithFiles_PartNotFound_ThrowException() {
            // GIVEN
            when(questionGroupMapper.toQuestionGroupEntity(createRequest)).thenReturn(questionGroup);
            when(partTestRepository.findById("part-123")).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> questionGroupService.createWithFiles(createRequest, null, null))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PART_TEST_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("getAllQuestionGroups Tests")
    class GetAllQuestionGroupsTests {

        @Test
        @DisplayName("Should return paginated question groups")
        void getAllQuestionGroups_Success() {
            // GIVEN
            QuestionGroupSummaryResponse summary1 = QuestionGroupSummaryResponse.builder()
                    .id("group-1")
                    .name("Group 1")
                    .build();

            QuestionGroupSummaryResponse summary2 = QuestionGroupSummaryResponse.builder()
                    .id("group-2")
                    .name("Group 2")
                    .build();

            QuestionGroupEntity group1 = QuestionGroupEntity.builder().id("group-1").build();
            QuestionGroupEntity group2 = QuestionGroupEntity.builder().id("group-2").build();

            Page<QuestionGroupEntity> page = new PageImpl<>(
                    List.of(group1, group2),
                    PageRequest.of(0, 10),
                    2
            );

            when(questionGroupRepository.findAll(any(Pageable.class))).thenReturn(page);
            when(questionGroupMapper.toQuestionGroupSummaryResponse(group1)).thenReturn(summary1);
            when(questionGroupMapper.toQuestionGroupSummaryResponse(group2)).thenReturn(summary2);

            // WHEN
            PageResponse<QuestionGroupSummaryResponse> result = questionGroupService.getAllQuestionGroups(1, 10);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getCurrentPage()).isEqualTo(1);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getData()).hasSize(2);
        }
    }

    @Test
    @DisplayName("Should return question group when ID exists")
    void getQuestionGroupById_ValidId_Success() {
        // GIVEN
        when(questionGroupRepository.findByIdWithMedia("group-123"))
                .thenReturn(Optional.of(questionGroup));
        when(questionTestRepository.findByGroupIdsWithMedia(List.of("group-123")))
                .thenReturn(java.util.Collections.emptyList());
        when(questionGroupMapper.toQuestionGroupDetailResponse(questionGroup))
                .thenReturn(detailResponse);

        // WHEN
        QuestionGroupDetailResponse result = questionGroupService.getQuestionGroupById("group-123");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("group-123");
        verify(questionGroupRepository).findByIdWithMedia("group-123");
        verify(questionTestRepository).findByGroupIdsWithMedia(List.of("group-123"));
    }

    @Test
    @DisplayName("Should throw exception when question group not found")
    void getQuestionGroupById_InvalidId_ThrowException() {
        // GIVEN
        when(questionGroupRepository.findByIdWithMedia("invalid-id"))
                .thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> questionGroupService.getQuestionGroupById("invalid-id"))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.QUESTION_GROUP_NOT_FOUND);
    }

    @Nested
    @DisplayName("update Tests")
    class UpdateTests {

        private QuestionGroupUpdateRequest updateRequest;

        @BeforeEach
        void setUp() {
            updateRequest = QuestionGroupUpdateRequest.builder()
                    .name("Updated Group")
                    .content("Updated content")
                    .type("CONVERSATION")
                    .imageId("new-image-123")
                    .audioId("new-audio-123")
                    .partId("part-456")
                    .questionIds(List.of("q1", "q2"))
                    .build();
        }

        @Test
        @DisplayName("Should update question group successfully")
        void update_ValidData_Success() {
            // GIVEN
            FileEntity newImage = FileEntity.builder().id("new-image-123").build();
            FileEntity newAudio = FileEntity.builder().id("new-audio-123").build();
            PartTestEntity newPart = PartTestEntity.builder().id("part-456").build();
            QuestionTestEntity q1 = QuestionTestEntity.builder().id("q1").build();
            QuestionTestEntity q2 = QuestionTestEntity.builder().id("q2").build();

            when(questionGroupRepository.findById("group-123")).thenReturn(Optional.of(questionGroup));
            when(fileRepository.findById("new-image-123")).thenReturn(Optional.of(newImage));
            when(fileRepository.findById("new-audio-123")).thenReturn(Optional.of(newAudio));
            when(partTestRepository.findById("part-456")).thenReturn(Optional.of(newPart));
            when(questionTestRepository.findAllById(List.of("q1", "q2"))).thenReturn(List.of(q1, q2));
            when(questionGroupRepository.save(any(QuestionGroupEntity.class))).thenReturn(questionGroup);
            when(questionGroupMapper.toQuestionGroupDetailResponse(questionGroup)).thenReturn(detailResponse);

            // WHEN
            QuestionGroupDetailResponse result = questionGroupService.update("group-123", updateRequest);

            // THEN
            assertThat(result).isNotNull();
            verify(questionGroupRepository).findById("group-123");
            verify(questionGroupRepository).save(any(QuestionGroupEntity.class));
        }

        @Test
        @DisplayName("Should throw exception when question group not found")
        void update_GroupNotFound_ThrowException() {
            // GIVEN
            when(questionGroupRepository.findById("invalid-id")).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> questionGroupService.update("invalid-id", updateRequest))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.QUESTION_GROUP_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw exception when part not found")
        void update_PartNotFound_ThrowException() {
            // GIVEN
            when(questionGroupRepository.findById("group-123")).thenReturn(Optional.of(questionGroup));
            when(fileRepository.findById(anyString())).thenReturn(Optional.of(imageFile));
            when(partTestRepository.findById("part-456")).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> questionGroupService.update("group-123", updateRequest))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PART_TEST_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw exception when question not found")
        void update_QuestionNotFound_ThrowException() {
            // GIVEN
            PartTestEntity newPart = PartTestEntity.builder().id("part-456").build();
            QuestionTestEntity q1 = QuestionTestEntity.builder().id("q1").build();
            // Only return 1 question instead of 2

            when(questionGroupRepository.findById("group-123")).thenReturn(Optional.of(questionGroup));
            when(fileRepository.findById(anyString())).thenReturn(Optional.of(imageFile));
            when(partTestRepository.findById("part-456")).thenReturn(Optional.of(newPart));
            when(questionTestRepository.findAllById(List.of("q1", "q2"))).thenReturn(List.of(q1));

            // WHEN & THEN
            assertThatThrownBy(() -> questionGroupService.update("group-123", updateRequest))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.QUESTION_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("deleteQuestionGroup Tests")
    class DeleteQuestionGroupTests {

        @Test
        @DisplayName("Should delete question group successfully")
        void deleteQuestionGroup_ValidId_Success() {
            // GIVEN
            when(questionGroupRepository.existsById("group-123")).thenReturn(true);
            doNothing().when(questionGroupRepository).deleteById("group-123");

            // WHEN
            questionGroupService.deleteQuestionGroup("group-123");

            // THEN
            verify(questionGroupRepository).existsById("group-123");
            verify(questionGroupRepository).deleteById("group-123");
        }

        @Test
        @DisplayName("Should throw exception when question group not found")
        void deleteQuestionGroup_NotFound_ThrowException() {
            // GIVEN
            when(questionGroupRepository.existsById("invalid-id")).thenReturn(false);

            // WHEN & THEN
            assertThatThrownBy(() -> questionGroupService.deleteQuestionGroup("invalid-id"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.QUESTION_GROUP_NOT_FOUND);

            verify(questionGroupRepository, never()).deleteById(anyString());
        }
    }

    @Nested
    @DisplayName("updateGroupAudio Tests")
    class UpdateGroupAudioTests {

        @Test
        @DisplayName("Should update audio and delete old audio")
        void updateGroupAudio_WithOldAudio_Success() {
            // GIVEN
            MockMultipartFile newAudio = new MockMultipartFile(
                    "audio", "new.mp3", "audio/mpeg", "new audio".getBytes()
            );

            FileEntity newAudioFile = FileEntity.builder().id("new-audio").build();

            when(questionGroupRepository.findById("group-123")).thenReturn(Optional.of(questionGroup));
            when(questionGroupRepository.save(any(QuestionGroupEntity.class))).thenReturn(questionGroup);
            when(fileStorageService.storeFile(newAudio, FileType.AUDIO, "question-groups"))
                    .thenReturn(newAudioFile);
            doNothing().when(fileStorageService).deleteFile("audio-123");
            when(questionGroupMapper.toQuestionGroupDetailResponse(questionGroup)).thenReturn(detailResponse);

            // WHEN
            QuestionGroupDetailResponse result = questionGroupService.updateGroupAudio("group-123", newAudio);

            // THEN
            assertThat(result).isNotNull();
            verify(fileStorageService).deleteFile("audio-123");
            verify(fileStorageService).storeFile(newAudio, FileType.AUDIO, "question-groups");
        }

        @Test
        @DisplayName("Should update audio without deleting when no old audio")
        void updateGroupAudio_NoOldAudio_Success() {
            // GIVEN
            questionGroup.setAudio(null);
            MockMultipartFile newAudio = new MockMultipartFile(
                    "audio", "new.mp3", "audio/mpeg", "new audio".getBytes()
            );

            FileEntity newAudioFile = FileEntity.builder().id("new-audio").build();

            when(questionGroupRepository.findById("group-123")).thenReturn(Optional.of(questionGroup));
            when(fileStorageService.storeFile(newAudio, FileType.AUDIO, "question-groups"))
                    .thenReturn(newAudioFile);
            when(questionGroupRepository.save(any(QuestionGroupEntity.class))).thenReturn(questionGroup);
            when(questionGroupMapper.toQuestionGroupDetailResponse(questionGroup)).thenReturn(detailResponse);

            // WHEN
            QuestionGroupDetailResponse result = questionGroupService.updateGroupAudio("group-123", newAudio);

            // THEN
            assertThat(result).isNotNull();
            verify(fileStorageService, never()).deleteFile(anyString());
            verify(fileStorageService).storeFile(newAudio, FileType.AUDIO, "question-groups");
        }
    }

    @Nested
    @DisplayName("updateGroupImage Tests")
    class UpdateGroupImageTests {

        @Test
        @DisplayName("Should update image and delete old image")
        void updateGroupImage_WithOldImage_Success() {
            // GIVEN
            MockMultipartFile newImage = new MockMultipartFile(
                    "image", "new.jpg", "image/jpeg", "new image".getBytes()
            );

            FileEntity newImageFile = FileEntity.builder().id("new-image").build();

            when(questionGroupRepository.findById("group-123")).thenReturn(Optional.of(questionGroup));
            when(questionGroupRepository.save(any(QuestionGroupEntity.class))).thenReturn(questionGroup);
            when(fileStorageService.storeFile(newImage, FileType.IMAGE, "question-groups"))
                    .thenReturn(newImageFile);
            doNothing().when(fileStorageService).deleteFile("image-123");
            when(questionGroupMapper.toQuestionGroupDetailResponse(questionGroup)).thenReturn(detailResponse);

            // WHEN
            QuestionGroupDetailResponse result = questionGroupService.updateGroupImage("group-123", newImage);

            // THEN
            assertThat(result).isNotNull();
            verify(fileStorageService).deleteFile("image-123");
            verify(fileStorageService).storeFile(newImage, FileType.IMAGE, "question-groups");
        }

        @Test
        @DisplayName("Should throw exception when question group not found")
        void updateGroupImage_NotFound_ThrowException() {
            // GIVEN
            MockMultipartFile newImage = new MockMultipartFile(
                    "image", "new.jpg", "image/jpeg", "new image".getBytes()
            );

            when(questionGroupRepository.findById("invalid-id")).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> questionGroupService.updateGroupImage("invalid-id", newImage))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.QUESTION_GROUP_NOT_FOUND);
        }
    }
}
