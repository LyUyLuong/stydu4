package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.request.Exam.SubmitExamRequest;
import com.lul.Stydu4.dto.request.Exam.UserAnswerSubmit;
import com.lul.Stydu4.dto.response.Exam.*;
import com.lul.Stydu4.dto.response.Question.QuestionTestDetailResponse;
import com.lul.Stydu4.dto.response.QuestionGroupResponse.QuestionGroupDetailResponse;
import com.lul.Stydu4.entity.*;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.enums.PartType;
import com.lul.Stydu4.enums.TestType;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.mapper.QuestionGroupMapper;
import com.lul.Stydu4.mapper.QuestionTestMapper;
import com.lul.Stydu4.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExamServiceImpl Tests")
class ExamServiceImplTest {

    @Mock
    private ITestRepository testRepository;

    @Mock
    private IPartTestRepository partTestRepository;

    @Mock
    private IQuestionTestRepository questionTestRepository;

    @Mock
    private IAnswerRepository answerRepository;

    @Mock
    private IResultRepository resultRepository;

    @Mock
    private IResultHavePartsRepository resultHavePartsRepository;

    @Mock
    private IUserAnswerRepository userAnswerRepository;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private QuestionTestMapper questionTestMapper;

    @Mock
    private QuestionGroupMapper questionGroupMapper;

    @InjectMocks
    private ExamServiceImpl examService;

    private static final String BASE_URL = "http://localhost:8080";

    private TestEntity toeicTest;
    private TestEntity ieltsTest;
    private UserEntity userEntity;
    private PartTestEntity listeningPart;
    private PartTestEntity readingPart;
    private QuestionTestEntity question1;
    private QuestionTestEntity question2;
    private AnswerEntity correctAnswer;
    private AnswerEntity correctAnswer2;  // For question 2
    private AnswerEntity wrongAnswer;
    private QuestionGroupEntity questionGroup;

    @BeforeEach
    void setUp() {
        // Set base URL using reflection
        try {
            var field = ExamServiceImpl.class.getDeclaredField("baseUrl");
            field.setAccessible(true);
            field.set(examService, BASE_URL);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set baseUrl", e);
        }

        // Setup TOEIC test
        toeicTest = TestEntity.builder()
                .id("test-toeic-1")
                .name("TOEIC Full Test")
                .type(TestType.TOEIC)
                .description("Complete TOEIC test")
                .build();

        // Setup IELTS test
        ieltsTest = TestEntity.builder()
                .id("test-ielts-1")
                .name("IELTS Listening Test")
                .type(TestType.IELTS)
                .description("IELTS Listening module")
                .build();

        // Setup user
        userEntity = UserEntity.builder()
                .id("user-1")
                .username("testuser")
                .email("test@example.com")
                .build();

        // Setup listening part (Part 1)
        listeningPart = PartTestEntity.builder()
                .id("part-1")
                .name("Part 1")
                .type(PartType.PART_1_TOEIC)
                .description("Photographs")
                .testEntity(toeicTest)
                .questions(new ArrayList<>())
                .questionGroups(new ArrayList<>())
                .build();

        // Setup reading part (Part 5)
        readingPart = PartTestEntity.builder()
                .id("part-5")
                .name("Part 5")
                .type(PartType.PART_5_TOEIC)
                .description("Incomplete Sentences")
                .testEntity(toeicTest)
                .questions(new ArrayList<>())
                .questionGroups(new ArrayList<>())
                .build();

        // Setup answers
        correctAnswer = AnswerEntity.builder()
                .id("answer-correct")
                .content("Correct answer")
                .isCorrect(true)
                .build();

        correctAnswer2 = AnswerEntity.builder()
                .id("answer-correct-2")
                .content("Correct answer 2")
                .isCorrect(true)
                .build();

        wrongAnswer = AnswerEntity.builder()
                .id("answer-wrong")
                .content("Wrong answer")
                .isCorrect(false)
                .build();

        // Setup questions
        question1 = QuestionTestEntity.builder()
                .id("question-1")
                .content("Listening question")
                .partEntity(listeningPart)
                .answers(Arrays.asList(correctAnswer, wrongAnswer))
                .build();

        question2 = QuestionTestEntity.builder()
                .id("question-2")
                .content("Reading question")
                .partEntity(readingPart)
                .answers(Arrays.asList(correctAnswer2, wrongAnswer))
                .build();

        listeningPart.getQuestions().add(question1);
        readingPart.getQuestions().add(question2);

        correctAnswer.setQuestion(question1);
        wrongAnswer.setQuestion(question1);

        // Setup question group
        questionGroup = QuestionGroupEntity.builder()
                .id("group-1")
                .content("Conversation audio")
                .partEntity(listeningPart)
                .questions(new ArrayList<>())
                .build();
    }

    @Nested
    @DisplayName("getExamQuestions Tests")
    class GetExamQuestionsTests {

        @Test
        @DisplayName("Should get full TOEIC test when partIds is null")
        void getExamQuestions_ToeicFullTest_Success() {
            // GIVEN
            when(testRepository.findById("test-toeic-1")).thenReturn(Optional.of(toeicTest));
            when(partTestRepository.findByTestEntityIdOrderByCreatedDateAsc("test-toeic-1"))
                    .thenReturn(List.of(listeningPart, readingPart));

            // WHEN
            ExamQuestionsResponse response = examService.getExamQuestions("test-toeic-1", null);

            // THEN
            assertThat(response).isNotNull();
            assertThat(response.getTestId()).isEqualTo("test-toeic-1");
            assertThat(response.getTestName()).isEqualTo("TOEIC Full Test");
            assertThat(response.getTestType()).isEqualTo("TOEIC");
            assertThat(response.getIsFullTest()).isTrue();
            assertThat(response.getParts()).hasSize(2);

            verify(testRepository).findById("test-toeic-1");
            verify(partTestRepository).findByTestEntityIdOrderByCreatedDateAsc("test-toeic-1");
        }

        @Test
        @DisplayName("Should get partial test when specific parts selected")
        void getExamQuestions_PartialTest_Success() {
            // GIVEN
            List<String> selectedParts = List.of("part-1");
            when(testRepository.findById("test-toeic-1")).thenReturn(Optional.of(toeicTest));
            when(partTestRepository.findAllById(selectedParts)).thenReturn(List.of(listeningPart));

            // WHEN
            ExamQuestionsResponse response = examService.getExamQuestions("test-toeic-1", selectedParts);

            // THEN
            assertThat(response.getIsFullTest()).isFalse();
            assertThat(response.getSelectedPartIds()).isEqualTo(selectedParts);
            assertThat(response.getParts()).hasSize(1);
            assertThat(response.getParts().get(0).getPartName()).isEqualTo("Part 1");

            verify(partTestRepository).findAllById(selectedParts);
        }

        @Test
        @DisplayName("Should get IELTS test successfully")
        void getExamQuestions_IeltsTest_Success() {
            // GIVEN
            PartTestEntity ieltsPart = PartTestEntity.builder()
                    .id("ielts-part-1")
                    .name("IELTS Listening Part 1")
                    .type(PartType.PART_1_IELTS)
                    .testEntity(ieltsTest)
                    .questions(new ArrayList<>())
                    .questionGroups(new ArrayList<>())
                    .build();

            when(testRepository.findById("test-ielts-1")).thenReturn(Optional.of(ieltsTest));
            when(partTestRepository.findByTestEntityIdOrderByCreatedDateAsc("test-ielts-1"))
                    .thenReturn(List.of(ieltsPart));

            // WHEN
            ExamQuestionsResponse response = examService.getExamQuestions("test-ielts-1", null);

            // THEN
            assertThat(response.getTestType()).isEqualTo("IELTS");
            assertThat(response.getTestName()).isEqualTo("IELTS Listening Test");
        }

        @Test
        @DisplayName("Should throw exception when test not found")
        void getExamQuestions_TestNotFound_ThrowsException() {
            // GIVEN
            when(testRepository.findById("invalid-test")).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> examService.getExamQuestions("invalid-test", null))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TEST_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw exception when selected parts not found")
        void getExamQuestions_PartsNotFound_ThrowsException() {
            // GIVEN
            when(testRepository.findById("test-toeic-1")).thenReturn(Optional.of(toeicTest));
            when(partTestRepository.findAllById(anyList())).thenReturn(List.of());

            // WHEN & THEN
            assertThatThrownBy(() -> examService.getExamQuestions("test-toeic-1", List.of("invalid-part")))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PART_SELECTION);
        }

        @Test
        @DisplayName("Should throw exception when selected parts belong to different test")
        void getExamQuestions_InvalidPartSelection_ThrowsException() {
            // GIVEN
            PartTestEntity otherTestPart = PartTestEntity.builder()
                    .id("other-part")
                    .testEntity(ieltsTest)
                    .build();

            when(testRepository.findById("test-toeic-1")).thenReturn(Optional.of(toeicTest));
            when(partTestRepository.findAllById(anyList())).thenReturn(List.of(otherTestPart));

            // WHEN & THEN
            assertThatThrownBy(() -> examService.getExamQuestions("test-toeic-1", List.of("other-part")))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PART_SELECTION);
        }

        @Test
        @DisplayName("Should calculate total questions correctly")
        void getExamQuestions_CalculatesTotalQuestions() {
            // GIVEN
            QuestionTestEntity groupQuestion = QuestionTestEntity.builder()
                    .id("question-3")
                    .content("Group question")
                    .build();

            questionGroup.getQuestions().add(groupQuestion);
            listeningPart.getQuestionGroups().add(questionGroup);

            when(testRepository.findById("test-toeic-1")).thenReturn(Optional.of(toeicTest));
            when(partTestRepository.findByTestEntityIdOrderByCreatedDateAsc("test-toeic-1"))
                    .thenReturn(List.of(listeningPart, readingPart));
            when(questionTestMapper.toQuestionDetailResponse(any())).thenReturn(new QuestionTestDetailResponse());
            when(questionGroupMapper.toQuestionGroupDetailResponse(any())).thenReturn(new QuestionGroupDetailResponse());

            // WHEN
            ExamQuestionsResponse response = examService.getExamQuestions("test-toeic-1", null);

            // THEN
            assertThat(response.getTotalQuestions()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should include audio URL when test has audio file")
        void getExamQuestions_WithAudioFile_Success() {
            // GIVEN
            FileEntity audioFile = FileEntity.builder()
                    .id("audio-1")
                    .originalFilename("test-audio.mp3")
                    .build();

            toeicTest.setAudio(audioFile);

            when(testRepository.findById("test-toeic-1")).thenReturn(Optional.of(toeicTest));
            when(partTestRepository.findByTestEntityIdOrderByCreatedDateAsc("test-toeic-1"))
                    .thenReturn(List.of(listeningPart));

            // WHEN
            ExamQuestionsResponse response = examService.getExamQuestions("test-toeic-1", null);

            // THEN
            assertThat(response.getAudioId()).isEqualTo("audio-1");
            assertThat(response.getAudioUrl()).isEqualTo(BASE_URL + "/api/v1/files/audio-1");
        }
    }

    @Nested
    @DisplayName("submitExam Tests")
    class SubmitExamTests {

        @Test
        @DisplayName("Should submit full TOEIC test successfully")
        void submitExam_FullToeicTest_Success() {
            // GIVEN
            SubmitExamRequest request = SubmitExamRequest.builder()
                    .testId("test-toeic-1")
                    .partIds(null)
                    .answers(List.of(
                            UserAnswerSubmit.builder()
                                    .questionId("question-1")
                                    .answerId("answer-correct")
                                    .build(),
                            UserAnswerSubmit.builder()
                                    .questionId("question-2")
                                    .answerId("answer-correct-2")
                                    .build()
                    ))
                    .build();

            ResultEntity savedResult = ResultEntity.builder()
                    .id("result-1")
                    .test(toeicTest)
                    .user(userEntity)
                    .isFullTest(true)
                    .build();

            when(testRepository.findById("test-toeic-1")).thenReturn(Optional.of(toeicTest));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));
            when(partTestRepository.findByTestEntityIdOrderByCreatedDateAsc("test-toeic-1"))
                    .thenReturn(List.of(listeningPart, readingPart));
            when(answerRepository.findAllById(anySet()))
                    .thenReturn(List.of(correctAnswer, correctAnswer2));
            when(resultRepository.save(any(ResultEntity.class))).thenReturn(savedResult);
            when(userAnswerRepository.saveAll(anyList())).thenReturn(List.of());
            when(resultHavePartsRepository.saveAll(anyList())).thenReturn(List.of());

            // WHEN
            ExamResultResponse response = examService.submitExam(request, "testuser");

            // THEN
            assertThat(response).isNotNull();
            assertThat(response.getResultId()).isEqualTo("result-1");
            assertThat(response.getIsFullTest()).isTrue();
            assertThat(response.getTotalCorrectAnswers()).isEqualTo(2);
            assertThat(response.getTotalScore()).isGreaterThan(0);
            assertThat(response.getListeningCorrectAnswers()).isEqualTo(1);
            assertThat(response.getReadingCorrectAnswers()).isEqualTo(1);

            verify(resultRepository, times(2)).save(any(ResultEntity.class));
        }

        @Test
        @DisplayName("Should submit partial test (listening only)")
        void submitExam_PartialTest_ListeningOnly() {
            // GIVEN
            SubmitExamRequest request = SubmitExamRequest.builder()
                    .testId("test-toeic-1")
                    .partIds(List.of("part-1"))
                    .answers(List.of(
                            UserAnswerSubmit.builder()
                                    .questionId("question-1")
                                    .answerId("answer-correct")
                                    .build()
                    ))
                    .build();

            ResultEntity savedResult = ResultEntity.builder()
                    .id("result-2")
                    .test(toeicTest)
                    .user(userEntity)
                    .isFullTest(false)
                    .build();

            when(testRepository.findById("test-toeic-1")).thenReturn(Optional.of(toeicTest));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));
            when(partTestRepository.findAllById(anyList())).thenReturn(List.of(listeningPart));
            when(answerRepository.findAllById(anySet()))
                    .thenReturn(List.of(correctAnswer));
            when(resultRepository.save(any(ResultEntity.class))).thenReturn(savedResult);
            when(userAnswerRepository.saveAll(anyList())).thenReturn(List.of());
            when(resultHavePartsRepository.saveAll(anyList())).thenReturn(List.of());

            // WHEN
            ExamResultResponse response = examService.submitExam(request, "testuser");

            // THEN
            assertThat(response.getIsFullTest()).isFalse();
            assertThat(response.getCompletedPartIds()).contains("part-1");
            assertThat(response.getListeningCorrectAnswers()).isEqualTo(1);
            assertThat(response.getReadingCorrectAnswers()).isEqualTo(0);

            verify(resultRepository, times(2)).save(any(ResultEntity.class));
        }

        @Test
        @DisplayName("Should handle wrong answers correctly")
        void submitExam_WrongAnswers_CalculatesZero() {
            // GIVEN
            SubmitExamRequest request = SubmitExamRequest.builder()
                    .testId("test-toeic-1")
                    .partIds(null)
                    .answers(List.of(
                            UserAnswerSubmit.builder()
                                    .questionId("question-1")
                                    .answerId("answer-wrong")
                                    .build()
                    ))
                    .build();

            ResultEntity savedResult = ResultEntity.builder()
                    .id("result-3")
                    .test(toeicTest)
                    .user(userEntity)
                    .build();

            when(testRepository.findById("test-toeic-1")).thenReturn(Optional.of(toeicTest));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));
            when(partTestRepository.findByTestEntityIdOrderByCreatedDateAsc("test-toeic-1"))
                    .thenReturn(List.of(listeningPart, readingPart));
            when(answerRepository.findAllById(anySet()))
                    .thenReturn(List.of(wrongAnswer));
            when(resultRepository.save(any(ResultEntity.class))).thenReturn(savedResult);
            when(userAnswerRepository.saveAll(anyList())).thenReturn(List.of());
            when(resultHavePartsRepository.saveAll(anyList())).thenReturn(List.of());

            // WHEN
            ExamResultResponse response = examService.submitExam(request, "testuser");

            // THEN
            assertThat(response.getTotalCorrectAnswers()).isEqualTo(0);
            assertThat(response.getQuestionResults().get(0).getIsCorrect()).isFalse();

            verify(resultRepository, times(2)).save(any(ResultEntity.class));
        }

        @Test
        @DisplayName("Should handle unanswered questions")
        void submitExam_UnansweredQuestions_Success() {
            // GIVEN
            SubmitExamRequest request = SubmitExamRequest.builder()
                    .testId("test-toeic-1")
                    .partIds(null)
                    .answers(List.of())
                    .build();

            ResultEntity savedResult = ResultEntity.builder()
                    .id("result-4")
                    .test(toeicTest)
                    .user(userEntity)
                    .build();

            when(testRepository.findById("test-toeic-1")).thenReturn(Optional.of(toeicTest));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));
            when(partTestRepository.findByTestEntityIdOrderByCreatedDateAsc("test-toeic-1"))
                    .thenReturn(List.of(listeningPart, readingPart));
            when(answerRepository.findAllById(anySet()))
                    .thenReturn(List.of());
            when(resultRepository.save(any(ResultEntity.class))).thenReturn(savedResult);
            when(userAnswerRepository.saveAll(anyList())).thenReturn(List.of());
            when(resultHavePartsRepository.saveAll(anyList())).thenReturn(List.of());

            // WHEN
            ExamResultResponse response = examService.submitExam(request, "testuser");

            // THEN
            assertThat(response.getTotalCorrectAnswers()).isEqualTo(0);
            assertThat(response.getQuestionResults())
                    .allMatch(qr -> qr.getUserAnswerContent().equals("Not answered"));

            verify(resultRepository, times(2)).save(any(ResultEntity.class));
        }

        @Test
        @DisplayName("Should throw exception when test not found")
        void submitExam_TestNotFound_ThrowsException() {
            // GIVEN
            SubmitExamRequest request = SubmitExamRequest.builder()
                    .testId("invalid-test")
                    .answers(List.of())
                    .build();

            when(testRepository.findById("invalid-test")).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> examService.submitExam(request, "testuser"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TEST_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void submitExam_UserNotFound_ThrowsException() {
            // GIVEN
            SubmitExamRequest request = SubmitExamRequest.builder()
                    .testId("test-toeic-1")
                    .answers(List.of())
                    .build();

            when(testRepository.findById("test-toeic-1")).thenReturn(Optional.of(toeicTest));
            when(userRepository.findByUsername("invalid-user")).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> examService.submitExam(request, "invalid-user"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_EXISTED);
        }

        @Test
        @DisplayName("Should calculate TOEIC score correctly for full test")
        void submitExam_ToeicScoreCalculation_Success() {
            // GIVEN
            SubmitExamRequest request = SubmitExamRequest.builder()
                    .testId("test-toeic-1")
                    .partIds(null)
                    .answers(List.of(
                            UserAnswerSubmit.builder()
                                    .questionId("question-1")
                                    .answerId("answer-correct")
                                    .build(),
                            UserAnswerSubmit.builder()
                                    .questionId("question-2")
                                    .answerId("answer-correct-2")
                                    .build()
                    ))
                    .build();

            ResultEntity savedResult = ResultEntity.builder()
                    .id("result-5")
                    .test(toeicTest)
                    .user(userEntity)
                    .isFullTest(true)
                    .build();

            when(testRepository.findById("test-toeic-1")).thenReturn(Optional.of(toeicTest));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));
            when(partTestRepository.findByTestEntityIdOrderByCreatedDateAsc("test-toeic-1"))
                    .thenReturn(List.of(listeningPart, readingPart));
            when(answerRepository.findAllById(anySet()))
                    .thenReturn(List.of(correctAnswer, correctAnswer2));
            when(resultRepository.save(any(ResultEntity.class))).thenReturn(savedResult);
            when(userAnswerRepository.saveAll(anyList())).thenReturn(List.of());
            when(resultHavePartsRepository.saveAll(anyList())).thenReturn(List.of());

            // WHEN
            ExamResultResponse response = examService.submitExam(request, "testuser");

            // THEN
            assertThat(response.getTotalScore()).isGreaterThanOrEqualTo(5);
            assertThat(response.getTotalScore()).isLessThanOrEqualTo(990);
            assertThat(response.getListeningScore()).isGreaterThanOrEqualTo(5);
            assertThat(response.getReadingScore()).isGreaterThanOrEqualTo(5);
        }
    }

    @Nested
    @DisplayName("getExamResult Tests")
    class GetExamResultTests {

        @Test
        @DisplayName("Should get exam result successfully")
        void getExamResult_ValidId_Success() {
            // GIVEN
            ResultEntity resultEntity = ResultEntity.builder()
                    .id("result-1")
                    .test(toeicTest)
                    .user(userEntity)
                    .isFullTest(true)
                    .listeningPoint(250)
                    .readingPoint(300)
                    .listeningCorrectAnswer(50)
                    .readingCorrectAnswer(60)
                    .totalQuestions(110)
                    .completeTime(Duration.ofMinutes(120).toMillis())
                    .resultHaveParts(new ArrayList<>())
                    .build();

            UserAnswerEntity userAnswer = UserAnswerEntity.builder()
                    .result(resultEntity)
                    .question(question1)
                    .answer(correctAnswer)
                    .isCorrect(true)
                    .build();

            when(resultRepository.findById("result-1")).thenReturn(Optional.of(resultEntity));
            when(userAnswerRepository.findByResultId("result-1")).thenReturn(List.of(userAnswer));

            // WHEN
            ExamResultResponse response = examService.getExamResult("result-1", "testuser");

            // THEN
            assertThat(response).isNotNull();
            assertThat(response.getResultId()).isEqualTo("result-1");
            assertThat(response.getTotalScore()).isEqualTo(550);
            assertThat(response.getListeningScore()).isEqualTo(250);
            assertThat(response.getReadingScore()).isEqualTo(300);

            verify(resultRepository).findById("result-1");
        }

        @Test
        @DisplayName("Should throw exception when result not found")
        void getExamResult_NotFound_ThrowsException() {
            // GIVEN
            when(resultRepository.findById("invalid-result")).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> examService.getExamResult("invalid-result", "testuser"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESULT_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw exception when unauthorized user")
        void getExamResult_Unauthorized_ThrowsException() {
            // GIVEN
            ResultEntity resultEntity = ResultEntity.builder()
                    .id("result-1")
                    .user(userEntity)
                    .build();

            when(resultRepository.findById("result-1")).thenReturn(Optional.of(resultEntity));

            // WHEN & THEN
            assertThatThrownBy(() -> examService.getExamResult("result-1", "other-user"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should include audio and image URLs in question results")
        void getExamResult_WithMediaFiles_Success() {
            // GIVEN
            FileEntity audioFile = FileEntity.builder()
                    .id("audio-1")
                    .originalFilename("question-audio.mp3")
                    .build();

            FileEntity imageFile = FileEntity.builder()
                    .id("image-1")
                    .originalFilename("question-image.jpg")
                    .build();

            question1.setAudio(audioFile);
            question1.setImage(imageFile);

            ResultEntity resultEntity = ResultEntity.builder()
                    .id("result-1")
                    .test(toeicTest)
                    .user(userEntity)
                    .isFullTest(true)
                    .listeningPoint(250)
                    .readingPoint(300)
                    .listeningCorrectAnswer(50)
                    .readingCorrectAnswer(60)
                    .totalQuestions(110)
                    .completeTime(Duration.ofMinutes(120).toMillis())
                    .resultHaveParts(new ArrayList<>())
                    .build();

            UserAnswerEntity userAnswer = UserAnswerEntity.builder()
                    .result(resultEntity)
                    .question(question1)
                    .answer(correctAnswer)
                    .isCorrect(true)
                    .build();

            when(resultRepository.findById("result-1")).thenReturn(Optional.of(resultEntity));
            when(userAnswerRepository.findByResultId("result-1")).thenReturn(List.of(userAnswer));

            // WHEN
            ExamResultResponse response = examService.getExamResult("result-1", "testuser");

            // THEN
            assertThat(response).isNotNull();
            assertThat(response.getQuestionResults()).hasSize(1);

            QuestionResultDetail questionResult = response.getQuestionResults().get(0);
            assertThat(questionResult.getAudioId()).isEqualTo("audio-1");
            assertThat(questionResult.getAudioUrl()).isEqualTo(BASE_URL + "/api/v1/files/audio-1");
            assertThat(questionResult.getImageId()).isEqualTo("image-1");
            assertThat(questionResult.getImageUrl()).isEqualTo(BASE_URL + "/api/v1/files/image-1");
        }
    }

    @Nested
    @DisplayName("getUserExamResults Tests")
    class GetUserExamResultsTests {

        @Test
        @DisplayName("Should get all user results for a test")
        void getUserExamResults_Success() {
            // GIVEN
            ResultEntity result1 = ResultEntity.builder()
                    .id("result-1")
                    .test(toeicTest)
                    .user(userEntity)
                    .isFullTest(true)
                    .listeningPoint(250)
                    .readingPoint(300)
                    .listeningCorrectAnswer(50)
                    .readingCorrectAnswer(60)
                    .totalQuestions(110)
                    .completeTime(Duration.ofMinutes(120).toMillis())
                    .resultHaveParts(new ArrayList<>())
                    .build();

            when(testRepository.findById("test-toeic-1")).thenReturn(Optional.of(toeicTest));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));
            when(resultRepository.findByTestIdAndUserUsernameOrderByCreatedDateDesc("test-toeic-1", "testuser"))
                    .thenReturn(List.of(result1));

            // WHEN
            List<ExamResultResponse> responses = examService.getUserExamResults("test-toeic-1", "testuser");

            // THEN
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getResultId()).isEqualTo("result-1");
        }

        @Test
        @DisplayName("Should return empty list when no results")
        void getUserExamResults_NoResults_EmptyList() {
            // GIVEN
            when(testRepository.findById("test-toeic-1")).thenReturn(Optional.of(toeicTest));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));
            when(resultRepository.findByTestIdAndUserUsernameOrderByCreatedDateDesc(anyString(), anyString()))
                    .thenReturn(List.of());

            // WHEN
            List<ExamResultResponse> responses = examService.getUserExamResults("test-toeic-1", "testuser");

            // THEN
            assertThat(responses).isEmpty();
        }
    }
}
