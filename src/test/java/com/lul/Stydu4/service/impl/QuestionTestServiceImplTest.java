package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.request.Answer.AnswerCreateRequest;
import com.lul.Stydu4.dto.request.Question.QuestionTestCreateRequest;
import com.lul.Stydu4.dto.request.Question.QuestionTestSearchRequest;
import com.lul.Stydu4.dto.request.Question.QuestionTestUpdateRequest;
import com.lul.Stydu4.dto.response.PageResponse;
import com.lul.Stydu4.dto.response.Question.QuestionTestDetailResponse;
import com.lul.Stydu4.dto.response.Question.QuestionTestSummaryResponse;
import com.lul.Stydu4.entity.*;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.enums.FileType;
import com.lul.Stydu4.enums.QuestionType;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.mapper.AnswerMapper;
import com.lul.Stydu4.mapper.QuestionTestMapper;
import com.lul.Stydu4.repository.*;
import com.lul.Stydu4.service.IFileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuestionTestServiceImpl Tests")
class QuestionTestServiceImplTest {

    @Mock
    private IQuestionTestRepository questionTestRepository;

    @Mock
    private IPartTestRepository partTestRepository;

    @Mock
    private IQuestionGroupRepository questionGroupRepository;

    @Mock
    private IAnswerRepository answerRepository;

    @Mock
    private IFileRepository fileRepository;

    @Mock
    private QuestionTestMapper questionTestMapper;

    @Mock
    private AnswerMapper answerMapper;

    @Mock
    private IFileStorageService fileStorageService;

    @InjectMocks
    private QuestionTestServiceImpl questionTestService;

    private QuestionTestEntity questionTestEntity;
    private QuestionTestDetailResponse questionTestDetailResponse;
    private QuestionTestSummaryResponse questionTestSummaryResponse;
    private PartTestEntity partTestEntity;
    private QuestionGroupEntity questionGroupEntity;
    private FileEntity audioFile;
    private FileEntity imageFile;

    @BeforeEach
    void setUp() {
        // Setup audio file
        audioFile = FileEntity.builder()
                .id("audio-123")
                .originalFilename("audio.mp3")
                .storedFilename("uuid-audio.mp3")
                .filePath("audio/questions/2024/01/audio.mp3")
                .fileUrl("http://localhost/audio/audio-123")
                .fileType(FileType.AUDIO)
                .fileSize(1024L)
                .contentType("audio/mpeg")
                .build();

        // Setup image file
        imageFile = FileEntity.builder()
                .id("image-123")
                .originalFilename("image.jpg")
                .storedFilename("uuid-image.jpg")
                .filePath("images/questions/2024/01/image.jpg")
                .fileUrl("http://localhost/images/image-123")
                .fileType(FileType.IMAGE)
                .fileSize(2048L)
                .contentType("image/jpeg")
                .build();

        // Setup part entity
        partTestEntity = PartTestEntity.builder()
                .id("part-123")
                .name("Part 1 - TOEIC Listening")
                .build();

        // Setup question group entity
        questionGroupEntity = QuestionGroupEntity.builder()
                .id("group-123")
                .name("Question Group 1")
                .build();

        // Setup answer entities
        List<AnswerEntity> answers = List.of(
                AnswerEntity.builder()
                        .id("answer-1")
                        .content("Answer A")
                        .isCorrect(true)
                        .mark("1.0")
                        .build(),
                AnswerEntity.builder()
                        .id("answer-2")
                        .content("Answer B")
                        .isCorrect(false)
                        .mark("0.0")
                        .build()
        );

        // Setup question test entity
        questionTestEntity = QuestionTestEntity.builder()
                .id("question-123")
                .name("Sample Question")
                .content("What is the capital of France?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .description("Geography question")
                .partEntity(partTestEntity)
                .questionGroupEntity(questionGroupEntity)
                .audio(audioFile)
                .image(imageFile)
                .answers(new ArrayList<>(answers))
                .build();

        // Setup response DTOs
        questionTestDetailResponse = QuestionTestDetailResponse.builder()
                .id("question-123")
                .name("Sample Question")
                .content("What is the capital of France?")
                .type(QuestionType.MULTIPLE_CHOICE.name())
                .audioId("audio-123")
                .audioUrl("http://localhost/audio/audio-123")
                .imageId("image-123")
                .imageUrl("http://localhost/images/image-123")
                .build();

        questionTestSummaryResponse = QuestionTestSummaryResponse.builder()
                .id("question-123")
                .name("Sample Question")
                .type(QuestionType.MULTIPLE_CHOICE.name())
                .build();
    }

    @Nested
    @DisplayName("create Tests")
    class CreateTests {

        @Test
        @DisplayName("create_ValidRequest_Success")
        void create_ValidRequest_Success() {
            // Given
            List<AnswerCreateRequest> answerRequests = List.of(
                    AnswerCreateRequest.builder().content("Answer A").isCorrect(true).mark("1.0").build(),
                    AnswerCreateRequest.builder().content("Answer B").isCorrect(false).mark("0.0").build()
            );

            QuestionTestCreateRequest request = QuestionTestCreateRequest.builder()
                    .name("Sample Question")
                    .content("What is the capital of France?")
                    .type("MULTIPLE_CHOICE")
                    .partId("part-123")
                    .questionGroupId("group-123")
                    .imageId("image-123")
                    .audioId("audio-123")
                    .answers(answerRequests)
                    .build();

            when(questionTestMapper.toQuestionTestEntity(request)).thenReturn(questionTestEntity);
            when(fileRepository.findById("image-123")).thenReturn(Optional.of(imageFile));
            when(fileRepository.findById("audio-123")).thenReturn(Optional.of(audioFile));
            when(partTestRepository.findById("part-123")).thenReturn(Optional.of(partTestEntity));
            when(questionGroupRepository.findById("group-123")).thenReturn(Optional.of(questionGroupEntity));
            when(answerMapper.toAnswerEntity(any(AnswerCreateRequest.class)))
                    .thenAnswer(invocation -> {
                        AnswerCreateRequest req = invocation.getArgument(0);
                        return AnswerEntity.builder()
                                .content(req.getContent())
                                .isCorrect(req.getIsCorrect())
                                .mark(req.getMark())
                                .build();
                    });
            when(questionTestRepository.save(any(QuestionTestEntity.class))).thenReturn(questionTestEntity);
            when(questionTestMapper.toQuestionDetailResponse(questionTestEntity)).thenReturn(questionTestDetailResponse);

            // When
            QuestionTestDetailResponse response = questionTestService.create(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo("question-123");
            assertThat(response.getName()).isEqualTo("Sample Question");
            verify(questionTestRepository).save(any(QuestionTestEntity.class));
        }

        @Test
        @DisplayName("create_WithoutOptionalFields_Success")
        void create_WithoutOptionalFields_Success() {
            // Given
            QuestionTestCreateRequest request = QuestionTestCreateRequest.builder()
                    .name("Simple Question")
                    .content("Content")
                    .type("MULTIPLE_CHOICE")
                    .build();

            QuestionTestEntity simpleEntity = QuestionTestEntity.builder()
                    .id("question-456")
                    .name("Simple Question")
                    .type(QuestionType.MULTIPLE_CHOICE)
                    .answers(new ArrayList<>())
                    .build();

            when(questionTestMapper.toQuestionTestEntity(request)).thenReturn(simpleEntity);
            when(questionTestRepository.save(any(QuestionTestEntity.class))).thenReturn(simpleEntity);
            when(questionTestMapper.toQuestionDetailResponse(simpleEntity)).thenReturn(questionTestDetailResponse);

            // When
            QuestionTestDetailResponse response = questionTestService.create(request);

            // Then
            assertThat(response).isNotNull();
            verify(fileRepository, never()).findById(anyString());
            verify(partTestRepository, never()).findById(anyString());
            verify(questionGroupRepository, never()).findById(anyString());
        }

        @Test
        @DisplayName("create_InvalidImageId_ThrowException")
        void create_InvalidImageId_ThrowException() {
            // Given
            QuestionTestCreateRequest request = QuestionTestCreateRequest.builder()
                    .name("Sample Question")
                    .content("Content")
                    .type("MULTIPLE_CHOICE")
                    .imageId("invalid-image")
                    .build();

            when(questionTestMapper.toQuestionTestEntity(request)).thenReturn(questionTestEntity);
            when(fileRepository.findById("invalid-image")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> questionTestService.create(request))
                    .isInstanceOf(AppException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.FILE_NOT_FOUND);
        }

        @Test
        @DisplayName("create_InvalidAudioId_ThrowException")
        void create_InvalidAudioId_ThrowException() {
            // Given
            QuestionTestCreateRequest request = QuestionTestCreateRequest.builder()
                    .name("Sample Question")
                    .content("Content")
                    .type("MULTIPLE_CHOICE")
                    .audioId("invalid-audio")
                    .build();

            when(questionTestMapper.toQuestionTestEntity(request)).thenReturn(questionTestEntity);
            when(fileRepository.findById("invalid-audio")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> questionTestService.create(request))
                    .isInstanceOf(AppException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.FILE_NOT_FOUND);
        }

        @Test
        @DisplayName("create_InvalidPartId_ThrowException")
        void create_InvalidPartId_ThrowException() {
            // Given
            QuestionTestCreateRequest request = QuestionTestCreateRequest.builder()
                    .name("Sample Question")
                    .content("Content")
                    .type("MULTIPLE_CHOICE")
                    .partId("invalid-part")
                    .build();

            when(questionTestMapper.toQuestionTestEntity(request)).thenReturn(questionTestEntity);
            when(partTestRepository.findById("invalid-part")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> questionTestService.create(request))
                    .isInstanceOf(AppException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PART_TEST_NOT_FOUND);
        }

        @Test
        @DisplayName("create_InvalidQuestionGroupId_ThrowException")
        void create_InvalidQuestionGroupId_ThrowException() {
            // Given
            QuestionTestCreateRequest request = QuestionTestCreateRequest.builder()
                    .name("Sample Question")
                    .content("Content")
                    .type("MULTIPLE_CHOICE")
                    .questionGroupId("invalid-group")
                    .build();

            when(questionTestMapper.toQuestionTestEntity(request)).thenReturn(questionTestEntity);
            when(questionGroupRepository.findById("invalid-group")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> questionTestService.create(request))
                    .isInstanceOf(AppException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.QUESTION_GROUP_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("getAllQuestionTests Tests")
    class GetAllQuestionTestsTests {

        @Test
        @DisplayName("getAllQuestionTests_Success")
        void getAllQuestionTests_Success() {
            // Given
            List<QuestionTestEntity> questions = List.of(questionTestEntity);
            Page<QuestionTestEntity> page = new PageImpl<>(questions);

            when(questionTestRepository.findAll(any(Pageable.class))).thenReturn(page);
            when(questionTestMapper.toQuestionSummaryResponse(questionTestEntity))
                    .thenReturn(questionTestSummaryResponse);

            // When
            PageResponse<QuestionTestSummaryResponse> response = questionTestService.getAllQuestionTests(1, 10);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getCurrentPage()).isEqualTo(1);
            assertThat(response.getTotalElements()).isEqualTo(1);
            assertThat(response.getData()).hasSize(1);
        }

        @Test
        @DisplayName("getAllQuestionTests_EmptyResult")
        void getAllQuestionTests_EmptyResult() {
            // Given
            Page<QuestionTestEntity> emptyPage = new PageImpl<>(List.of());
            when(questionTestRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            // When
            PageResponse<QuestionTestSummaryResponse> response = questionTestService.getAllQuestionTests(1, 10);

            // Then
            assertThat(response.getData()).isEmpty();
            assertThat(response.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("searchQuestionTests Tests")
    class SearchQuestionTestsTests {

        @Test
        @DisplayName("searchQuestionTests_WithCriteria_Success")
        void searchQuestionTests_WithCriteria_Success() {
            // Given
            QuestionTestSearchRequest searchRequest = QuestionTestSearchRequest.builder()
                    .name("Sample")
                    .type("SINGLE_CHOICE")
                    .page(1)
                    .size(10)
                    .build();

            List<QuestionTestEntity> questions = List.of(questionTestEntity);
            Page<QuestionTestEntity> page = new PageImpl<>(questions);

            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"));

            when(questionTestRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(page);
            when(questionTestMapper.toQuestionSummaryResponse(questionTestEntity))
                    .thenReturn(questionTestSummaryResponse);

            // When
            PageResponse<QuestionTestSummaryResponse> response = 
                    questionTestService.searchQuestionTests(searchRequest, pageable);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData()).hasSize(1);
            assertThat(response.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("searchQuestionTests_NoResults")
        void searchQuestionTests_NoResults() {
            // Given
            QuestionTestSearchRequest searchRequest = QuestionTestSearchRequest.builder()
                    .name("NonExistent")
                    .page(1)
                    .size(10)
                    .build();

            Page<QuestionTestEntity> emptyPage = new PageImpl<>(List.of());
            Pageable pageable = PageRequest.of(0, 10);

            when(questionTestRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(emptyPage);

            // When
            PageResponse<QuestionTestSummaryResponse> response = 
                    questionTestService.searchQuestionTests(searchRequest, pageable);

            // Then
            assertThat(response.getData()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getQuestionTestById Tests")
    class GetQuestionTestByIdTests {

        @Test
        @DisplayName("getQuestionTestById_ValidId_Success")
        void getQuestionTestById_ValidId_Success() {
            // Given
            when(questionTestRepository.findById("question-123")).thenReturn(Optional.of(questionTestEntity));
            when(questionTestMapper.toQuestionDetailResponse(questionTestEntity))
                    .thenReturn(questionTestDetailResponse);

            // When
            QuestionTestDetailResponse response = questionTestService.getQuestionTestById("question-123");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo("question-123");
            assertThat(response.getName()).isEqualTo("Sample Question");
        }

        @Test
        @DisplayName("getQuestionTestById_InvalidId_ThrowException")
        void getQuestionTestById_InvalidId_ThrowException() {
            // Given
            when(questionTestRepository.findById("invalid-id")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> questionTestService.getQuestionTestById("invalid-id"))
                    .isInstanceOf(AppException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.QUESTION_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("update Tests")
    class UpdateTests {

        @Test
        @DisplayName("update_ValidRequest_Success")
        void update_ValidRequest_Success() {
            // Given
            List<AnswerCreateRequest> updatedAnswers = List.of(
                    AnswerCreateRequest.builder().content("Updated Answer A").isCorrect(true).mark("1.0").build(),
                    AnswerCreateRequest.builder().content("Updated Answer B").isCorrect(false).mark("0.0").build()
            );

            QuestionTestUpdateRequest updateRequest = QuestionTestUpdateRequest.builder()
                    .name("Updated Question")
                    .content("Updated Content")
                    .type("MULTIPLE_CHOICE")
                    .answers(updatedAnswers)
                    .build();

            when(questionTestRepository.findById("question-123")).thenReturn(Optional.of(questionTestEntity));
            when(questionTestRepository.save(any(QuestionTestEntity.class))).thenReturn(questionTestEntity);
            when(questionTestMapper.toQuestionDetailResponse(questionTestEntity))
                    .thenReturn(questionTestDetailResponse);

            // When
            QuestionTestDetailResponse response = questionTestService.update("question-123", updateRequest);

            // Then
            assertThat(response).isNotNull();
            verify(questionTestRepository).save(questionTestEntity);
        }

        @Test
        @DisplayName("update_UpdateImageAndAudio_Success")
        void update_UpdateImageAndAudio_Success() {
            // Given
            QuestionTestUpdateRequest updateRequest = QuestionTestUpdateRequest.builder()
                    .name("Updated Question")
                    .imageId("new-image-123")
                    .audioId("new-audio-123")
                    .build();

            FileEntity newImage = FileEntity.builder().id("new-image-123").build();
            FileEntity newAudio = FileEntity.builder().id("new-audio-123").build();

            when(questionTestRepository.findById("question-123")).thenReturn(Optional.of(questionTestEntity));
            when(fileRepository.findById("new-image-123")).thenReturn(Optional.of(newImage));
            when(fileRepository.findById("new-audio-123")).thenReturn(Optional.of(newAudio));
            when(questionTestRepository.save(any(QuestionTestEntity.class))).thenReturn(questionTestEntity);
            when(questionTestMapper.toQuestionDetailResponse(questionTestEntity))
                    .thenReturn(questionTestDetailResponse);

            // When
            QuestionTestDetailResponse response = questionTestService.update("question-123", updateRequest);

            // Then
            assertThat(response).isNotNull();
            verify(fileRepository).findById("new-image-123");
            verify(fileRepository).findById("new-audio-123");
        }

        @Test
        @DisplayName("update_InvalidQuestionId_ThrowException")
        void update_InvalidQuestionId_ThrowException() {
            // Given
            QuestionTestUpdateRequest updateRequest = QuestionTestUpdateRequest.builder()
                    .name("Updated Question")
                    .build();

            when(questionTestRepository.findById("invalid-id")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> questionTestService.update("invalid-id", updateRequest))
                    .isInstanceOf(AppException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.QUESTION_NOT_FOUND);
        }

        @Test
        @DisplayName("update_MoreAnswersThanExisting_AddNewAnswers")
        void update_MoreAnswersThanExisting_AddNewAnswers() {
            // Given
            List<AnswerCreateRequest> updatedAnswers = List.of(
                    AnswerCreateRequest.builder().content("Answer 1").isCorrect(true).mark("1.0").build(),
                    AnswerCreateRequest.builder().content("Answer 2").isCorrect(false).mark("0.0").build(),
                    AnswerCreateRequest.builder().content("Answer 3").isCorrect(false).mark("0.0").build() // New answer
            );

            QuestionTestUpdateRequest updateRequest = QuestionTestUpdateRequest.builder()
                    .name("Updated Question")
                    .answers(updatedAnswers)
                    .build();

            when(questionTestRepository.findById("question-123")).thenReturn(Optional.of(questionTestEntity));
            when(questionTestRepository.save(any(QuestionTestEntity.class))).thenReturn(questionTestEntity);
            when(questionTestMapper.toQuestionDetailResponse(questionTestEntity))
                    .thenReturn(questionTestDetailResponse);

            // When
            QuestionTestDetailResponse response = questionTestService.update("question-123", updateRequest);

            // Then
            assertThat(response).isNotNull();
            verify(questionTestRepository).save(questionTestEntity);
        }

        @Test
        @DisplayName("update_FewerAnswersThanExisting_RemoveExtraAnswers")
        void update_FewerAnswersThanExisting_RemoveExtraAnswers() {
            // Given
            List<AnswerCreateRequest> updatedAnswers = List.of(
                    AnswerCreateRequest.builder().content("Answer 1").isCorrect(true).mark("1.0").build()
                    // Only 1 answer, but entity has 2
            );

            QuestionTestUpdateRequest updateRequest = QuestionTestUpdateRequest.builder()
                    .name("Updated Question")
                    .answers(updatedAnswers)
                    .build();

            when(questionTestRepository.findById("question-123")).thenReturn(Optional.of(questionTestEntity));
            when(questionTestRepository.save(any(QuestionTestEntity.class))).thenReturn(questionTestEntity);
            when(questionTestMapper.toQuestionDetailResponse(questionTestEntity))
                    .thenReturn(questionTestDetailResponse);

            // When
            QuestionTestDetailResponse response = questionTestService.update("question-123", updateRequest);

            // Then
            assertThat(response).isNotNull();
            verify(questionTestRepository).save(questionTestEntity);
        }
    }

    @Nested
    @DisplayName("deleteQuestionTest Tests")
    class DeleteQuestionTestTests {

        @Test
        @DisplayName("deleteQuestionTest_ValidId_Success")
        void deleteQuestionTest_ValidId_Success() {
            // Given
            when(questionTestRepository.existsById("question-123")).thenReturn(true);
            doNothing().when(questionTestRepository).deleteById("question-123");

            // When
            questionTestService.deleteQuestionTest("question-123");

            // Then
            verify(questionTestRepository).deleteById("question-123");
        }

        @Test
        @DisplayName("deleteQuestionTest_InvalidId_ThrowException")
        void deleteQuestionTest_InvalidId_ThrowException() {
            // Given
            when(questionTestRepository.existsById("invalid-id")).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> questionTestService.deleteQuestionTest("invalid-id"))
                    .isInstanceOf(AppException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.QUESTION_NOT_FOUND);

            verify(questionTestRepository, never()).deleteById(anyString());
        }
    }

    @Nested
    @DisplayName("createWithFiles Tests")
    class CreateWithFilesTests {

        @Mock
        private MultipartFile audioFile;

        @Mock
        private MultipartFile imageFile;

        @Test
        @DisplayName("createWithFiles_WithBothFiles_Success")
        void createWithFiles_WithBothFiles_Success() {
            // Given
            QuestionTestCreateRequest request = QuestionTestCreateRequest.builder()
                    .name("Question with files")
                    .content("Content")
                    .type("MULTIPLE_CHOICE")
                    .partId("part-123")
                    .build();

            when(audioFile.isEmpty()).thenReturn(false);
            when(imageFile.isEmpty()).thenReturn(false);

            FileEntity uploadedAudio = FileEntity.builder().id("uploaded-audio").build();
            FileEntity uploadedImage = FileEntity.builder().id("uploaded-image").build();

            when(questionTestMapper.toQuestionTestEntity(request)).thenReturn(questionTestEntity);
            when(partTestRepository.findById("part-123")).thenReturn(Optional.of(partTestEntity));
            when(fileStorageService.storeFile(audioFile, FileType.AUDIO, "questions"))
                    .thenReturn(uploadedAudio);
            when(fileStorageService.storeFile(imageFile, FileType.IMAGE, "questions"))
                    .thenReturn(uploadedImage);
            when(questionTestRepository.save(any(QuestionTestEntity.class))).thenReturn(questionTestEntity);
            when(questionTestMapper.toQuestionDetailResponse(questionTestEntity))
                    .thenReturn(questionTestDetailResponse);

            // When
            QuestionTestDetailResponse response = questionTestService.createWithFiles(request, audioFile, imageFile);

            // Then
            assertThat(response).isNotNull();
            verify(fileStorageService).storeFile(audioFile, FileType.AUDIO, "questions");
            verify(fileStorageService).storeFile(imageFile, FileType.IMAGE, "questions");
        }

        @Test
        @DisplayName("createWithFiles_WithoutFiles_Success")
        void createWithFiles_WithoutFiles_Success() {
            // Given
            QuestionTestCreateRequest request = QuestionTestCreateRequest.builder()
                    .name("Question without files")
                    .content("Content")
                    .type("MULTIPLE_CHOICE")
                    .build();

            when(questionTestMapper.toQuestionTestEntity(request)).thenReturn(questionTestEntity);
            when(questionTestRepository.save(any(QuestionTestEntity.class))).thenReturn(questionTestEntity);
            when(questionTestMapper.toQuestionDetailResponse(questionTestEntity))
                    .thenReturn(questionTestDetailResponse);

            // When
            QuestionTestDetailResponse response = questionTestService.createWithFiles(request, null, null);

            // Then
            assertThat(response).isNotNull();
            verify(fileStorageService, never()).storeFile(any(), any(), anyString());
        }
    }

    @Nested
    @DisplayName("updateQuestionAudio Tests")
    class UpdateQuestionAudioTests {

        @Mock
        private MultipartFile newAudioFile;

        @Test
        @DisplayName("updateQuestionAudio_ReplaceExisting_Success")
        void updateQuestionAudio_ReplaceExisting_Success() {
            // Given
            when(newAudioFile.isEmpty()).thenReturn(false);

            FileEntity newAudio = FileEntity.builder().id("new-audio").build();

            when(questionTestRepository.findById("question-123")).thenReturn(Optional.of(questionTestEntity));
            when(fileStorageService.storeFile(newAudioFile, FileType.AUDIO, "questions"))
                    .thenReturn(newAudio);
            when(questionTestRepository.save(any(QuestionTestEntity.class))).thenReturn(questionTestEntity);
            when(questionTestMapper.toQuestionDetailResponse(questionTestEntity))
                    .thenReturn(questionTestDetailResponse);
            doNothing().when(fileStorageService).deleteFile("audio-123");

            // When
            QuestionTestDetailResponse response = questionTestService.updateQuestionAudio("question-123", newAudioFile);

            // Then
            assertThat(response).isNotNull();
            verify(fileStorageService).deleteFile("audio-123");
            verify(fileStorageService).storeFile(newAudioFile, FileType.AUDIO, "questions");
        }

        @Test
        @DisplayName("updateQuestionAudio_InvalidQuestionId_ThrowException")
        void updateQuestionAudio_InvalidQuestionId_ThrowException() {
            // Given
            when(questionTestRepository.findById("invalid-id")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> questionTestService.updateQuestionAudio("invalid-id", newAudioFile))
                    .isInstanceOf(AppException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.QUESTION_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("updateQuestionImage Tests")
    class UpdateQuestionImageTests {

        @Mock
        private MultipartFile newImageFile;

        @Test
        @DisplayName("updateQuestionImage_ReplaceExisting_Success")
        void updateQuestionImage_ReplaceExisting_Success() {
            // Given
            when(newImageFile.isEmpty()).thenReturn(false);

            FileEntity newImage = FileEntity.builder().id("new-image").build();

            when(questionTestRepository.findById("question-123")).thenReturn(Optional.of(questionTestEntity));
            when(fileStorageService.storeFile(newImageFile, FileType.IMAGE, "questions"))
                    .thenReturn(newImage);
            when(questionTestRepository.save(any(QuestionTestEntity.class))).thenReturn(questionTestEntity);
            when(questionTestMapper.toQuestionDetailResponse(questionTestEntity))
                    .thenReturn(questionTestDetailResponse);
            doNothing().when(fileStorageService).deleteFile("image-123");

            // When
            QuestionTestDetailResponse response = questionTestService.updateQuestionImage("question-123", newImageFile);

            // Then
            assertThat(response).isNotNull();
            verify(fileStorageService).deleteFile("image-123");
            verify(fileStorageService).storeFile(newImageFile, FileType.IMAGE, "questions");
        }

        @Test
        @DisplayName("updateQuestionImage_InvalidQuestionId_ThrowException")
        void updateQuestionImage_InvalidQuestionId_ThrowException() {
            // Given
            when(questionTestRepository.findById("invalid-id")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> questionTestService.updateQuestionImage("invalid-id", newImageFile))
                    .isInstanceOf(AppException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.QUESTION_NOT_FOUND);
        }
    }
}
