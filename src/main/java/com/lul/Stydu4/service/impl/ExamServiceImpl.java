package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.request.Exam.SubmitExamRequest;
import com.lul.Stydu4.dto.request.Exam.UserAnswerSubmit;
import com.lul.Stydu4.dto.response.Exam.*;
import com.lul.Stydu4.dto.response.Question.QuestionTestDetailResponse;
import com.lul.Stydu4.dto.response.QuestionGroupResponse.QuestionGroupDetailResponse;
import com.lul.Stydu4.entity.*;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.enums.PartType;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.mapper.QuestionGroupMapper;
import com.lul.Stydu4.mapper.QuestionTestMapper;
import com.lul.Stydu4.repository.*;
import com.lul.Stydu4.service.IExamService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ExamServiceImpl implements IExamService {

    ITestRepository testRepository;
    IPartTestRepository partTestRepository;
    IQuestionTestRepository questionTestRepository;
    IAnswerRepository answerRepository;
    IResultRepository resultRepository;
    IResultHavePartsRepository resultHavePartsRepository;
    IUserAnswerRepository userAnswerRepository;
    IUserRepository userRepository;
    QuestionTestMapper questionTestMapper;
    QuestionGroupMapper questionGroupMapper;

    @Override
    @Transactional(readOnly = true)
    public ExamQuestionsResponse getExamQuestions(String testId, List<String> partIds) {
        log.info("Fetching exam questions for test: {}, parts: {}", testId, partIds);

        TestEntity test = testRepository.findById(testId)
                .orElseThrow(() -> new AppException(ErrorCode.TEST_NOT_FOUND));

        List<PartTestEntity> parts;
        boolean isFullTest = false;

        if (partIds == null || partIds.isEmpty()) {
            parts = partTestRepository.findByTestEntityIdOrderByCreatedDateAsc(testId);
            isFullTest = true;
            log.info("Loading FULL test with {} parts", parts.size());
        } else {
            parts = partTestRepository.findAllById(partIds);

            boolean allPartsValid = parts.stream()
                    .allMatch(part -> part.getTestEntity().getId().equals(testId));

            if (!allPartsValid || parts.size() != partIds.size()) {
                throw new AppException(ErrorCode.INVALID_PART_SELECTION);
            }

            Map<String, PartTestEntity> partMap = parts.stream()
                    .collect(Collectors.toMap(PartTestEntity::getId, p -> p));
            parts = partIds.stream()
                    .map(partMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            log.info("Loading PARTIAL test with {} selected parts", parts.size());
        }

        if (parts.isEmpty()) {
            throw new AppException(ErrorCode.PART_TEST_NOT_FOUND);
        }

        List<PartQuestionsDetail> partDetails = parts.stream()
                .map(part -> {
                    List<QuestionTestDetailResponse> questions = part.getQuestions().stream()
                            .map(questionTestMapper::toQuestionDetailResponse)
                            .collect(Collectors.toList());

                    List<QuestionGroupDetailResponse> questionGroups = part.getQuestionGroups().stream()
                            .map(questionGroupMapper::toQuestionGroupDetailResponse)
                            .collect(Collectors.toList());

                    return PartQuestionsDetail.builder()
                            .partId(part.getId())
                            .partName(part.getName())
                            .partType(part.getType().name())
                            .description(part.getDescription())
                            .questions(questions)
                            .questionGroups(questionGroups)
                            .build();
                })
                .collect(Collectors.toList());

        Integer totalQuestions = parts.stream()
                .mapToInt(part -> {
                    Integer directQuestions = part.getQuestions().size();
                    Integer groupQuestions = part.getQuestionGroups().stream()
                            .mapToInt(qg -> qg.getQuestions().size())
                            .sum();
                    return directQuestions + groupQuestions;
                })
                .sum();

        return ExamQuestionsResponse.builder()
                .testId(test.getId())
                .testName(test.getName())
                .testType(test.getType().name())
                .description(test.getDescription())
                .isFullTest(isFullTest)
                .selectedPartIds(isFullTest ? null : partIds)
                .totalQuestions(totalQuestions)
                .parts(partDetails)
                .build();
    }

    @Override
    @Transactional
    public ExamResultResponse submitExam(SubmitExamRequest request, String userName) {
        log.info("=== STARTING EXAM SUBMISSION ===");
        log.info("User: {}, Test: {}, Parts: {}", userName, request.getTestId(), request.getPartIds());

        LocalDateTime startTime = LocalDateTime.now();

        // Validate test and user
        TestEntity test = testRepository.findById(request.getTestId())
                .orElseThrow(() -> new AppException(ErrorCode.TEST_NOT_FOUND));

        UserEntity user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Determine full test or partial
        List<PartTestEntity> submittedParts;
        boolean isFullTest;

        if (request.getPartIds() == null || request.getPartIds().isEmpty()) {
            submittedParts = partTestRepository.findByTestEntityIdOrderByCreatedDateAsc(request.getTestId());
            isFullTest = true;
            log.info("Mode: FULL TEST with {} parts", submittedParts.size());
        } else {
            submittedParts = partTestRepository.findAllById(request.getPartIds());
            isFullTest = false;

            boolean allPartsValid = submittedParts.stream()
                    .allMatch(part -> part.getTestEntity().getId().equals(request.getTestId()));

            if (!allPartsValid || submittedParts.size() != request.getPartIds().size()) {
                throw new AppException(ErrorCode.INVALID_PART_SELECTION);
            }
            log.info("Mode: PARTIAL TEST with {} parts", submittedParts.size());
        }

        // Create user answer map và validate
        Map<String, String> userAnswerMap = request.getAnswers().stream()
                .collect(Collectors.toMap(
                        UserAnswerSubmit::getQuestionId,
                        UserAnswerSubmit::getAnswerId,
                        (existing, replacement) -> {
                            log.warn("Duplicate answer for question: {}, keeping first answer", existing);
                            return existing; // Keep first answer if duplicate
                        }
                ));

        log.info("Total user answers submitted: {}", userAnswerMap.size());

        // Initialize counters
        Map<String, PartResultData> partResultsMap = new LinkedHashMap<>();
        List<QuestionResultDetail> questionResults = new ArrayList<>();
        List<UserAnswerEntity> userAnswerEntities = new ArrayList<>();

        int totalCorrect = 0;
        int listeningCorrect = 0;
        int readingCorrect = 0;
        int listeningTotal = 0;
        int readingTotal = 0;

        // Create ResultEntity first
        ResultEntity result = ResultEntity.builder()
                .type(test.getType())
                .isFullTest(isFullTest)
                .user(user)
                .test(test)
                .build();

        ResultEntity savedResult = resultRepository.save(result);
        log.info("Created result entity with ID: {}", savedResult.getId());

        // Process each part
        for (PartTestEntity part : submittedParts) {
            log.info("--- Processing Part: {} ({}) ---", part.getName(), part.getType());

            PartResultData partResult = new PartResultData();
            partResult.setPartId(part.getId());
            partResult.setPartName(part.getName());
            partResult.setPartType(part.getType().name());

            boolean isListening = isListeningPart(part.getType());
            log.info("Part type: {}, Is Listening: {}", part.getType(), isListening);

            // Sử dụng Set để track processed questions và tránh duplicate
            Set<String> processedQuestionIds = new HashSet<>();

            // Process direct questions
            log.info("Processing {} direct questions", part.getQuestions().size());
            for (QuestionTestEntity question : part.getQuestions()) {
                if (processedQuestionIds.contains(question.getId())) {
                    log.warn("Duplicate question detected: {}, skipping", question.getId());
                    continue;
                }

                ProcessedQuestionData data = processQuestionWithUserAnswer(
                        question, userAnswerMap, savedResult, isListening
                );

                questionResults.add(data.getQuestionResult());
                userAnswerEntities.add(data.getUserAnswer());
                processedQuestionIds.add(question.getId());

                partResult.incrementTotal();
                if (data.isCorrect()) {
                    partResult.incrementCorrect();
                    totalCorrect++;
                    if (isListening) {
                        listeningCorrect++;
                    } else {
                        readingCorrect++;
                    }
                }

                if (isListening) {
                    listeningTotal++;
                } else {
                    readingTotal++;
                }
            }

            // Process question groups
            log.info("Processing {} question groups", part.getQuestionGroups().size());
            for (QuestionGroupEntity group : part.getQuestionGroups()) {
                log.info("Group: {}, Questions: {}", group.getId(), group.getQuestions().size());
                for (QuestionTestEntity question : group.getQuestions()) {
                    if (processedQuestionIds.contains(question.getId())) {
                        log.warn("Duplicate question detected in group: {}, skipping", question.getId());
                        continue;
                    }

                    ProcessedQuestionData data = processQuestionWithUserAnswer(
                            question, userAnswerMap, savedResult, isListening
                    );

                    questionResults.add(data.getQuestionResult());
                    userAnswerEntities.add(data.getUserAnswer());
                    processedQuestionIds.add(question.getId());

                    partResult.incrementTotal();
                    if (data.isCorrect()) {
                        partResult.incrementCorrect();
                        totalCorrect++;
                        if (isListening) {
                            listeningCorrect++;
                        } else {
                            readingCorrect++;
                        }
                    }

                    if (isListening) {
                        listeningTotal++;
                    } else {
                        readingTotal++;
                    }
                }
            }

            partResult.calculateAccuracy();
            partResultsMap.put(part.getId(), partResult);

            log.info("Part {} Results: {}/{} correct ({}%)",
                    part.getName(),
                    partResult.getCorrectCount(),
                    partResult.getTotalCount(),
                    String.format("%.2f", partResult.getAccuracy()));
        }

        // Log final counts
        log.info("=== FINAL COUNTS ===");
        log.info("Total Questions: {}", listeningTotal + readingTotal);
        log.info("Listening: {}/{} correct", listeningCorrect, listeningTotal);
        log.info("Reading: {}/{} correct", readingCorrect, readingTotal);
        log.info("Total Correct: {}", totalCorrect);

        // Validate totals
        int calculatedTotal = listeningCorrect + readingCorrect;
        if (calculatedTotal != totalCorrect) {
            log.error("MISMATCH: calculatedTotal={}, totalCorrect={}", calculatedTotal, totalCorrect);
            throw new AppException(ErrorCode.CALCULATION_ERROR);
        }

        // Calculate scores
        Integer listeningScore = 0;
        Integer readingScore = 0;

        if (isFullTest) {
            listeningScore = convertToToeicScore(listeningCorrect, listeningTotal, true);
            readingScore = convertToToeicScore(readingCorrect, readingTotal, false);
        } else {
            if (listeningTotal > 0) {
                listeningScore = convertToToeicScore(listeningCorrect, listeningTotal, true);
            }
            if (readingTotal > 0) {
                readingScore = convertToToeicScore(readingCorrect, readingTotal, false);
            }
        }

        Integer totalScore = listeningScore + readingScore;
        Integer totalQuestions = listeningTotal + readingTotal;

        log.info("=== SCORES ===");
        log.info("Listening Score: {}", listeningScore);
        log.info("Reading Score: {}", readingScore);
        log.info("Total Score: {}", totalScore);

        // Calculate completion time
        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime, endTime);


        // Update result with calculated data
        savedResult.setReadingPoint(readingScore);
        savedResult.setListeningPoint(listeningScore);
        savedResult.setReadingCorrectAnswer(readingCorrect);
        savedResult.setListeningCorrectAnswer(listeningCorrect);
        savedResult.setCompleteTime(duration);
        savedResult.setTotalQuestions(totalQuestions);

        resultRepository.save(savedResult);
        log.info("Updated result entity in database");

        // Save user answers in batch
        userAnswerRepository.saveAll(userAnswerEntities);
        log.info("Saved {} user answers", userAnswerEntities.size());

        // Save part results
        List<ResultHavePartsEntity> resultHavePartsEntities = new ArrayList<>();
        for (PartResultData partData : partResultsMap.values()) {
            PartTestEntity part = partTestRepository.findById(partData.getPartId())
                    .orElseThrow(() -> new AppException(ErrorCode.PART_TEST_NOT_FOUND));

            ResultHavePartsEntity resultHavePart = ResultHavePartsEntity.builder()
                    .result(savedResult)
                    .partTest(part)
                    .correctAnswers(partData.getCorrectCount())
                    .totalQuestions(partData.getTotalCount())
                    .accuracy(partData.getAccuracy())
                    .build();

            resultHavePartsEntities.add(resultHavePart);
        }
        resultHavePartsRepository.saveAll(resultHavePartsEntities);
        log.info("Saved {} part results", resultHavePartsEntities.size());

        // Build response
        List<PartResultDetail> partResultsList = partResultsMap.values().stream()
                .map(data -> PartResultDetail.builder()
                        .partId(data.getPartId())
                        .partName(data.getPartName())
                        .partType(data.getPartType())
                        .correctAnswers(data.getCorrectCount())
                        .totalQuestions(data.getTotalCount())
                        .accuracy(data.getAccuracy())
                        .build())
                .collect(Collectors.toList());

        List<String> completedPartIds = submittedParts.stream()
                .map(PartTestEntity::getId)
                .collect(Collectors.toList());

        log.info("=== EXAM SUBMISSION COMPLETED ===");
        log.info("Mode: {}, Total Score: {}, Result ID: {}",
                isFullTest ? "FULL TEST" : "PARTIAL TEST", totalScore, savedResult.getId());

        return ExamResultResponse.builder()
                .resultId(savedResult.getId())
                .testId(test.getId())
                .testName(test.getName())
                .userId(user.getId())
                .userName(user.getUsername())
                .isFullTest(isFullTest)
                .completedPartIds(completedPartIds)
                .totalScore(totalScore)
                .listeningScore(listeningScore)
                .readingScore(readingScore)
                .totalCorrectAnswers(totalCorrect)
                .listeningCorrectAnswers(listeningCorrect)
                .readingCorrectAnswers(readingCorrect)
                .totalQuestions(totalQuestions)
                .listeningQuestions(listeningTotal)
                .readingQuestions(readingTotal)
                .completeTime((duration.toMillis() / 1000) + "s")
                .partResults(partResultsList)
                .questionResults(questionResults)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ExamResultResponse getExamResult(String resultId, String userName) {
        log.info("Fetching exam result: {} for user: {}", resultId, userName);

        ResultEntity result = resultRepository.findById(resultId)
                .orElseThrow(() -> new AppException(ErrorCode.RESULT_NOT_FOUND));

        if (!result.getUser().getUsername().equals(userName)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        List<UserAnswerEntity> userAnswers = userAnswerRepository.findByResultId(resultId);

        List<QuestionResultDetail> questionResults = userAnswers.stream()
                .map(ua -> {
                    QuestionTestEntity question = ua.getQuestion();
                    AnswerEntity userAnswer = ua.getAnswer();

                    AnswerEntity correctAnswer = question.getAnswers().stream()
                            .filter(AnswerEntity::getIsCorrect)
                            .findFirst()
                            .orElse(null);

                    return QuestionResultDetail.builder()
                            .questionId(question.getId())
                            .questionContent(question.getContent())
                            .userAnswerId(userAnswer != null ? userAnswer.getId() : null)
                            .userAnswerContent(userAnswer != null ? userAnswer.getContent() : "Not answered")
                            .correctAnswerId(correctAnswer != null ? correctAnswer.getId() : null)
                            .correctAnswerContent(correctAnswer != null ? correctAnswer.getContent() : null)
                            .isCorrect(ua.getIsCorrect())
                            .partName(question.getPartEntity() != null ?
                                    question.getPartEntity().getName() : "")
                            .build();
                })
                .collect(Collectors.toList());

        List<ResultHavePartsEntity> resultHaveParts = result.getResultHaveParts();

        List<PartResultDetail> partResults = resultHaveParts.stream()
                .map(rhp -> PartResultDetail.builder()
                        .partId(rhp.getPartTest().getId())
                        .partName(rhp.getPartTest().getName())
                        .partType(rhp.getPartTest().getType().name())
                        .correctAnswers(rhp.getCorrectAnswers())
                        .totalQuestions(rhp.getTotalQuestions())
                        .accuracy(rhp.getAccuracy())
                        .build())
                .collect(Collectors.toList());

        List<String> completedPartIds = resultHaveParts.stream()
                .map(rhp -> rhp.getPartTest().getId())
                .collect(Collectors.toList());

        // Tính toán lại từ database để đảm bảo chính xác
        Integer totalCorrect = result.getListeningCorrectAnswer() + result.getReadingCorrectAnswer();
        Integer totalScore = result.getListeningPoint() + result.getReadingPoint();

        return ExamResultResponse.builder()
                .resultId(result.getId())
                .testId(result.getTest().getId())
                .testName(result.getTest().getName())
                .userId(result.getUser().getId())
                .userName(result.getUser().getUsername())
                .isFullTest(result.getIsFullTest())
                .completedPartIds(completedPartIds)
                .totalScore(totalScore)
                .listeningScore(result.getListeningPoint())
                .readingScore(result.getReadingPoint())
                .totalCorrectAnswers(totalCorrect)
                .listeningCorrectAnswers(result.getListeningCorrectAnswer())
                .readingCorrectAnswers(result.getReadingCorrectAnswer())
                .totalQuestions(result.getTotalQuestions())
                .listeningQuestions(null) // Không lưu trong DB
                .readingQuestions(null) // Không lưu trong DB
                .completeTime(result.getCompleteTime().toString())
                .partResults(partResults)
                .questionResults(questionResults)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamResultResponse> getUserExamResults(String testId, String userName) {
        log.info("Fetching all results for test: {} and user: {}", testId, userName);

        TestEntity test = testRepository.findById(testId)
                .orElseThrow(() -> new AppException(ErrorCode.TEST_NOT_FOUND));

        UserEntity user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        List<ResultEntity> results = resultRepository.findByTestIdAndUserUsernameOrderByCreatedDateDesc(testId, userName);

        return results.stream()
                .map(result -> {
                    List<PartResultDetail> partResults = result.getResultHaveParts().stream()
                            .map(rhp -> PartResultDetail.builder()
                                    .partId(rhp.getPartTest().getId())
                                    .partName(rhp.getPartTest().getName())
                                    .partType(rhp.getPartTest().getType().name())
                                    .correctAnswers(rhp.getCorrectAnswers())
                                    .totalQuestions(rhp.getTotalQuestions())
                                    .accuracy(rhp.getAccuracy())
                                    .build())
                            .collect(Collectors.toList());

                    List<String> completedPartIds = result.getResultHaveParts().stream()
                            .map(rhp -> rhp.getPartTest().getId())
                            .collect(Collectors.toList());

                    Integer totalCorrect = result.getListeningCorrectAnswer() + result.getReadingCorrectAnswer();
                    Integer totalScore = result.getListeningPoint() + result.getReadingPoint();

                    return ExamResultResponse.builder()
                            .resultId(result.getId())
                            .testId(result.getTest().getId())
                            .testName(result.getTest().getName())
                            .userId(result.getUser().getId())
                            .userName(result.getUser().getUsername())
                            .isFullTest(result.getIsFullTest())
                            .completedPartIds(completedPartIds)
                            .totalScore(totalScore)
                            .listeningScore(result.getListeningPoint())
                            .readingScore(result.getReadingPoint())
                            .totalCorrectAnswers(totalCorrect)
                            .listeningCorrectAnswers(result.getListeningCorrectAnswer())
                            .readingCorrectAnswers(result.getReadingCorrectAnswer())
                            .totalQuestions(result.getTotalQuestions())
                            .listeningQuestions(null)
                            .readingQuestions(null)
                            .completeTime(result.getCompleteTime().toString())
                            .partResults(partResults)
                            .questionResults(null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // =============== HELPER METHODS ===============

    private ProcessedQuestionData processQuestionWithUserAnswer(
            QuestionTestEntity question,
            Map<String, String> userAnswerMap,
            ResultEntity result,
            boolean isListening
    ) {
        String userAnswerId = userAnswerMap.get(question.getId());

        AnswerEntity correctAnswer = question.getAnswers().stream()
                .filter(answer -> answer.getIsCorrect() != null && answer.getIsCorrect())
                .findFirst()
                .orElse(null);

        AnswerEntity userAnswer = null;
        if (userAnswerId != null) {
            userAnswer = answerRepository.findById(userAnswerId).orElse(null);
        }

        boolean isCorrect = userAnswer != null &&
                userAnswer.getIsCorrect() != null &&
                userAnswer.getIsCorrect();

        // Create UserAnswerEntity
        UserAnswerEntity userAnswerEntity = UserAnswerEntity.builder()
                .result(result)
                .question(question)
                .answer(userAnswer) // Chỉ lưu câu trả lời của user, có thể null
                .isCorrect(isCorrect)
                .build();

        // Create QuestionResultDetail
        QuestionResultDetail questionResult = QuestionResultDetail.builder()
                .questionId(question.getId())
                .questionContent(question.getContent())
                .userAnswerId(userAnswer != null ? userAnswer.getId() : null)
                .userAnswerContent(userAnswer != null ? userAnswer.getContent() : "Not answered")
                .correctAnswerId(correctAnswer != null ? correctAnswer.getId() : null)
                .correctAnswerContent(correctAnswer != null ? correctAnswer.getContent() : null)
                .isCorrect(isCorrect)
                .partName(question.getPartEntity() != null ? question.getPartEntity().getName() : "")
                .build();

        log.debug("Question {}: User Answer={}, Correct={}, IsCorrect={}",
                question.getId(),
                userAnswer != null ? userAnswer.getId() : "null",
                correctAnswer != null ? correctAnswer.getId() : "null",
                isCorrect);

        return new ProcessedQuestionData(userAnswerEntity, questionResult, isCorrect);
    }

    private boolean isListeningPart(PartType partType) {
        return partType == PartType.PART_1_TOEIC ||
                partType == PartType.PART_2_TOEIC ||
                partType == PartType.PART_3_TOEIC ||
                partType == PartType.PART_4_TOEIC;
    }

    private Integer convertToToeicScore(Integer correctAnswers, Integer totalQuestions, boolean isListening) {
        if (totalQuestions == 0) return 0;

        // Công thức chính xác hơn dựa trên TOEIC scoring scale
        double percentage = (double) correctAnswers / totalQuestions;

        // TOEIC score ranges từ 5 đến 495 cho mỗi phần
        // Linear scaling với floor tại 5
        int baseScore = (int) Math.round(percentage * 495);

        // Minimum score is 5, maximum is 495
        int finalScore = Math.max(5, Math.min(495, baseScore));

        log.debug("Score calculation - Correct: {}/{}, Percentage: {:.2f}%, Score: {}",
                correctAnswers, totalQuestions, percentage * 100, finalScore);

        return finalScore;
    }

    // Helper classes
    @Data
    private static class PartResultData {
        private String partId;
        private String partName;
        private String partType;
        private int correctCount = 0;
        private int totalCount = 0;
        private double accuracy = 0.0;

        public void incrementCorrect() {
            correctCount++;
        }

        public void incrementTotal() {
            totalCount++;
        }

        public void calculateAccuracy() {
            if (totalCount > 0) {
                accuracy = ((double) correctCount / totalCount) * 100.0;
                // Round to 2 decimal places
                accuracy = Math.round(accuracy * 100.0) / 100.0;
            } else {
                accuracy = 0.0;
            }
        }
    }

    @Data
    @AllArgsConstructor
    private static class ProcessedQuestionData {
        private UserAnswerEntity userAnswer;
        private QuestionResultDetail questionResult;
        private boolean isCorrect;
    }
}
