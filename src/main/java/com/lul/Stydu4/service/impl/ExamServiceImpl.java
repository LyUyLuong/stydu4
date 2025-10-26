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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class ExamServiceImpl implements IExamService {

    final ITestRepository testRepository;
    final IPartTestRepository partTestRepository;
    final IQuestionTestRepository questionTestRepository;
    final IAnswerRepository answerRepository;
    final IResultRepository resultRepository;
    final IResultHavePartsRepository resultHavePartsRepository;
    final IUserAnswerRepository userAnswerRepository;
    final IUserRepository userRepository;
    final QuestionTestMapper questionTestMapper;
    final QuestionGroupMapper questionGroupMapper;

    // ✅ NEW - Base URL for file access
    @Value("${app.base-url:http://localhost:8080}")
    String baseUrl;

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
                .map(this::mapPartToDetail)
                .collect(Collectors.toList());

        Integer totalQuestions = calculateTotalQuestions(parts);

        return ExamQuestionsResponse.builder()
                .testId(test.getId())
                .testName(test.getName())
                .testType(test.getType().name())
                .description(test.getDescription())
                // ✅ NEW - Add test audio URL
                .audioId(test.getAudio() != null ? test.getAudio().getId() : null)
                .audioUrl(test.getAudio() != null ? buildFileUrl(test.getAudio().getId()) : null)
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

        // 1. Load and validate exam context
        ExamContext context = loadExamContext(request, userName);

        // 2. Create result entity to get ID
        ResultEntity savedResult = createInitialResult(context);

        // 3. Load answer cache for batch processing
        Map<String, AnswerEntity> answerCache = loadAnswerCache(request.getAnswers());

        // 4. Process all questions
        ExamProcessingResult processingResult = processAllQuestions(
                context, savedResult, answerCache, request.getAnswers()
        );

        // 5. Calculate scores
        ExamScores scores = calculateScores(processingResult, context.isFullTest());

        // 6. Update and save results
        Duration duration = Duration.between(startTime, LocalDateTime.now());
        updateAndSaveResults(savedResult, processingResult, scores, duration);

        // 7. Save part results
        savePartResults(savedResult, processingResult.getPartResultsMap());

        // 8. Build and return response
        log.info("=== EXAM SUBMISSION COMPLETED ===");
        log.info("Mode: {}, Total Score: {}, Result ID: {}",
                context.isFullTest() ? "FULL TEST" : "PARTIAL TEST",
                scores.getTotalScore(),
                savedResult.getId());

        return buildExamResponse(context, savedResult, processingResult, scores, duration);
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
        List<QuestionResultDetail> questionResults = mapToQuestionResults(userAnswers);
        List<PartResultDetail> partResults = mapToPartResults(result.getResultHaveParts());
        List<String> completedPartIds = extractCompletedPartIds(result.getResultHaveParts());

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
                .completeTime(formatDuration(result.getCompleteTime()))
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

        List<ResultEntity> results = resultRepository
                .findByTestIdAndUserUsernameOrderByCreatedDateDesc(testId, userName);

        return results.stream()
                .map(this::mapToExamResultResponse)
                .collect(Collectors.toList());
    }

    // =============== PRIVATE HELPER METHODS ===============

    // ✅ NEW - Build file URL helper
    private String buildFileUrl(String fileId) {
        if (fileId == null) return null;
        return baseUrl + "/api/v1/files/" + fileId;
    }

    private PartQuestionsDetail mapPartToDetail(PartTestEntity part) {
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
    }

    private Integer calculateTotalQuestions(List<PartTestEntity> parts) {
        return parts.stream()
                .mapToInt(part -> {
                    int directQuestions = part.getQuestions().size();
                    int groupQuestions = part.getQuestionGroups().stream()
                            .mapToInt(qg -> qg.getQuestions().size())
                            .sum();
                    return directQuestions + groupQuestions;
                })
                .sum();
    }

    private ExamContext loadExamContext(SubmitExamRequest request, String userName) {
        TestEntity test = testRepository.findById(request.getTestId())
                .orElseThrow(() -> new AppException(ErrorCode.TEST_NOT_FOUND));

        UserEntity user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

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

        if (submittedParts.isEmpty()) {
            throw new AppException(ErrorCode.PART_TEST_NOT_FOUND);
        }

        return new ExamContext(test, user, submittedParts, isFullTest);
    }

    private ResultEntity createInitialResult(ExamContext context) {
        ResultEntity result = ResultEntity.builder()
                .type(context.getTest().getType())
                .isFullTest(context.isFullTest())
                .user(context.getUser())
                .test(context.getTest())
                .build();

        ResultEntity savedResult = resultRepository.save(result);
        log.info("Created result entity with ID: {}", savedResult.getId());
        return savedResult;
    }

    private Map<String, AnswerEntity> loadAnswerCache(List<UserAnswerSubmit> answers) {
        Set<String> answerIds = answers.stream()
                .map(UserAnswerSubmit::getAnswerId)
                .collect(Collectors.toSet());

        return answerRepository.findAllById(answerIds)
                .stream()
                .collect(Collectors.toMap(AnswerEntity::getId, a -> a));
    }

    private ExamProcessingResult processAllQuestions(
            ExamContext context,
            ResultEntity savedResult,
            Map<String, AnswerEntity> answerCache,
            List<UserAnswerSubmit> userAnswers
    ) {
        Map<String, String> userAnswerMap = userAnswers.stream()
                .collect(Collectors.toMap(
                        UserAnswerSubmit::getQuestionId,
                        UserAnswerSubmit::getAnswerId,
                        (existing, replacement) -> {
                            log.warn("Duplicate answer for question, keeping first");
                            return existing;
                        }
                ));

        log.info("Total user answers submitted: {}", userAnswerMap.size());

        Map<String, PartResultData> partResultsMap = new LinkedHashMap<>();
        List<QuestionResultDetail> questionResults = new ArrayList<>();
        List<UserAnswerEntity> userAnswerEntities = new ArrayList<>();

        int totalCorrect = 0;
        int listeningCorrect = 0;
        int readingCorrect = 0;
        int listeningTotal = 0;
        int readingTotal = 0;

        for (PartTestEntity part : context.getSubmittedParts()) {
            log.info("--- Processing Part: {} ({}) ---", part.getName(), part.getType());

            PartResultData partResult = new PartResultData(part);
            boolean isListening = isListeningPart(part.getType());
            log.info("Part type: {}, Is Listening: {}", part.getType(), isListening);

            Set<String> processedQuestionIds = new HashSet<>();

            // Process direct questions
            log.info("Processing {} direct questions", part.getQuestions().size());
            for (QuestionTestEntity question : part.getQuestions()) {
                if (!processedQuestionIds.add(question.getId())) {
                    log.warn("Duplicate question detected: {}, skipping", question.getId());
                    continue;
                }

                ProcessedQuestionData data = processQuestionWithUserAnswer(
                        question, userAnswerMap, answerCache, savedResult
                );

                questionResults.add(data.getQuestionResult());
                userAnswerEntities.add(data.getUserAnswer());
                partResult.incrementTotal();

                if (data.isCorrect()) {
                    partResult.incrementCorrect();
                    totalCorrect++;
                    if (isListening) listeningCorrect++;
                    else readingCorrect++;
                }

                if (isListening) listeningTotal++;
                else readingTotal++;
            }

            // Process question groups
            log.info("Processing {} question groups", part.getQuestionGroups().size());
            for (QuestionGroupEntity group : part.getQuestionGroups()) {
                for (QuestionTestEntity question : group.getQuestions()) {
                    if (!processedQuestionIds.add(question.getId())) {
                        log.warn("Duplicate question in group: {}, skipping", question.getId());
                        continue;
                    }

                    ProcessedQuestionData data = processQuestionWithUserAnswer(
                            question, userAnswerMap, answerCache, savedResult
                    );

                    questionResults.add(data.getQuestionResult());
                    userAnswerEntities.add(data.getUserAnswer());
                    partResult.incrementTotal();

                    if (data.isCorrect()) {
                        partResult.incrementCorrect();
                        totalCorrect++;
                        if (isListening) listeningCorrect++;
                        else readingCorrect++;
                    }

                    if (isListening) listeningTotal++;
                    else readingTotal++;
                }
            }

            partResult.calculateAccuracy();
            partResultsMap.put(part.getId(), partResult);
            log.info("Part {} Results: {}/{} correct ({:.2f}%)",
                    part.getName(), partResult.getCorrectCount(),
                    partResult.getTotalCount(), partResult.getAccuracy());
        }

        log.info("=== FINAL COUNTS ===");
        log.info("Listening: {}/{} correct", listeningCorrect, listeningTotal);
        log.info("Reading: {}/{} correct", readingCorrect, readingTotal);
        log.info("Total Correct: {}", totalCorrect);

        // Save user answers in batch
        userAnswerRepository.saveAll(userAnswerEntities);
        log.info("Saved {} user answers", userAnswerEntities.size());

        return new ExamProcessingResult(
                partResultsMap, questionResults, totalCorrect,
                listeningCorrect, readingCorrect, listeningTotal, readingTotal
        );
    }

    private ProcessedQuestionData processQuestionWithUserAnswer(
            QuestionTestEntity question,
            Map<String, String> userAnswerMap,
            Map<String, AnswerEntity> answerCache,
            ResultEntity result
    ) {
        String userAnswerId = userAnswerMap.get(question.getId());
        AnswerEntity correctAnswer = question.getAnswers().stream()
                .filter(answer -> Boolean.TRUE.equals(answer.getIsCorrect()))
                .findFirst()
                .orElse(null);

        AnswerEntity userAnswer = userAnswerId != null ? answerCache.get(userAnswerId) : null;
        boolean isCorrect = userAnswer != null && Boolean.TRUE.equals(userAnswer.getIsCorrect());

        UserAnswerEntity userAnswerEntity = UserAnswerEntity.builder()
                .result(result)
                .question(question)
                .answer(userAnswer)
                .isCorrect(isCorrect)
                .build();

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

        return new ProcessedQuestionData(userAnswerEntity, questionResult, isCorrect);
    }

    private ExamScores calculateScores(ExamProcessingResult result, boolean isFullTest) {
        int listeningScore = 0;
        int readingScore = 0;

        if (isFullTest) {
            listeningScore = convertToToeicScore(result.getListeningCorrect(), result.getListeningTotal());
            readingScore = convertToToeicScore(result.getReadingCorrect(), result.getReadingTotal());
        } else {
            if (result.getListeningTotal() > 0) {
                listeningScore = convertToToeicScore(result.getListeningCorrect(), result.getListeningTotal());
            }
            if (result.getReadingTotal() > 0) {
                readingScore = convertToToeicScore(result.getReadingCorrect(), result.getReadingTotal());
            }
        }

        int totalScore = listeningScore + readingScore;

        log.info("=== SCORES ===");
        log.info("Listening Score: {}", listeningScore);
        log.info("Reading Score: {}", readingScore);
        log.info("Total Score: {}", totalScore);

        return new ExamScores(listeningScore, readingScore, totalScore);
    }

    private void updateAndSaveResults(
            ResultEntity result,
            ExamProcessingResult processingResult,
            ExamScores scores,
            Duration duration
    ) {
        result.setReadingPoint(scores.getReadingScore());
        result.setListeningPoint(scores.getListeningScore());
        result.setReadingCorrectAnswer(processingResult.getReadingCorrect());
        result.setListeningCorrectAnswer(processingResult.getListeningCorrect());
        result.setCompleteTime(duration);
        result.setTotalQuestions(processingResult.getListeningTotal() + processingResult.getReadingTotal());

        resultRepository.save(result);
        log.info("Updated result entity in database");
    }

    private void savePartResults(ResultEntity result, Map<String, PartResultData> partResultsMap) {
        List<ResultHavePartsEntity> resultHavePartsEntities = partResultsMap.values().stream()
                .map(partData -> ResultHavePartsEntity.builder()
                        .result(result)
                        .partTest(partData.getPartEntity())
                        .correctAnswers(partData.getCorrectCount())
                        .totalQuestions(partData.getTotalCount())
                        .accuracy(partData.getAccuracy())
                        .build())
                .collect(Collectors.toList());

        resultHavePartsRepository.saveAll(resultHavePartsEntities);
        log.info("Saved {} part results", resultHavePartsEntities.size());
    }

    private ExamResultResponse buildExamResponse(
            ExamContext context,
            ResultEntity savedResult,
            ExamProcessingResult processingResult,
            ExamScores scores,
            Duration duration
    ) {
        List<PartResultDetail> partResultsList = processingResult.getPartResultsMap().values().stream()
                .map(data -> PartResultDetail.builder()
                        .partId(data.getPartId())
                        .partName(data.getPartName())
                        .partType(data.getPartType())
                        .correctAnswers(data.getCorrectCount())
                        .totalQuestions(data.getTotalCount())
                        .accuracy(data.getAccuracy())
                        .build())
                .collect(Collectors.toList());

        List<String> completedPartIds = context.getSubmittedParts().stream()
                .map(PartTestEntity::getId)
                .collect(Collectors.toList());

        return ExamResultResponse.builder()
                .resultId(savedResult.getId())
                .testId(context.getTest().getId())
                .testName(context.getTest().getName())
                .userId(context.getUser().getId())
                .userName(context.getUser().getUsername())
                .isFullTest(context.isFullTest())
                .completedPartIds(completedPartIds)
                .totalScore(scores.getTotalScore())
                .listeningScore(scores.getListeningScore())
                .readingScore(scores.getReadingScore())
                .totalCorrectAnswers(processingResult.getTotalCorrect())
                .listeningCorrectAnswers(processingResult.getListeningCorrect())
                .readingCorrectAnswers(processingResult.getReadingCorrect())
                .totalQuestions(processingResult.getListeningTotal() + processingResult.getReadingTotal())
                .listeningQuestions(processingResult.getListeningTotal())
                .readingQuestions(processingResult.getReadingTotal())
                .completeTime((duration.toMillis() / 1000) + "s")
                .partResults(partResultsList)
                .questionResults(processingResult.getQuestionResults())
                .build();
    }

    private List<QuestionResultDetail> mapToQuestionResults(List<UserAnswerEntity> userAnswers) {
        return userAnswers.stream()
                .map(ua -> {
                    QuestionTestEntity question = ua.getQuestion();
                    AnswerEntity userAnswer = ua.getAnswer();
                    AnswerEntity correctAnswer = question.getAnswers().stream()
                            .filter(answer -> Boolean.TRUE.equals(answer.getIsCorrect()))
                            .findFirst()
                            .orElse(null);

                    // Map all answers for this question
                    List<QuestionResultDetail.AnswerDetail> allAnswers = question.getAnswers().stream()
                            .map(answer -> QuestionResultDetail.AnswerDetail.builder()
                                    .answerId(answer.getId())
                                    .mark(answer.getMark())
                                    .content(answer.getContent())
                                    .isCorrect(Boolean.TRUE.equals(answer.getIsCorrect()))
                                    .build())
                            .sorted(Comparator.comparing(QuestionResultDetail.AnswerDetail::getMark,
                                    Comparator.nullsLast(Comparator.naturalOrder())))
                            .collect(Collectors.toList());

                    return QuestionResultDetail.builder()
                            .questionId(question.getId())
                            .questionContent(question.getContent())
                            // ✅ NEW - Map audio
                            .audioId(question.getAudio() != null ? question.getAudio().getId() : null)
                            .audioUrl(question.getAudio() != null ? buildFileUrl(question.getAudio().getId()) : null)
                            // ✅ NEW - Map image
                            .imageId(question.getImage() != null ? question.getImage().getId() : null)
                            .imageUrl(question.getImage() != null ? buildFileUrl(question.getImage().getId()) : null)
                            .userAnswerId(userAnswer != null ? userAnswer.getId() : null)
                            .userAnswerContent(userAnswer != null ? userAnswer.getContent() : "Not answered")
                            .correctAnswerId(correctAnswer != null ? correctAnswer.getId() : null)
                            .correctAnswerContent(correctAnswer != null ? correctAnswer.getContent() : null)
                            .isCorrect(ua.getIsCorrect())
                            .partName(question.getPartEntity() != null ?
                                    question.getPartEntity().getName() : "")
                            .allAnswers(allAnswers)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<PartResultDetail> mapToPartResults(List<ResultHavePartsEntity> resultHaveParts) {
        return resultHaveParts.stream()
                .map(rhp -> PartResultDetail.builder()
                        .partId(rhp.getPartTest().getId())
                        .partName(rhp.getPartTest().getName())
                        .partType(rhp.getPartTest().getType().name())
                        .correctAnswers(rhp.getCorrectAnswers())
                        .totalQuestions(rhp.getTotalQuestions())
                        .accuracy(rhp.getAccuracy())
                        .build())
                .collect(Collectors.toList());
    }

    private List<String> extractCompletedPartIds(List<ResultHavePartsEntity> resultHaveParts) {
        return resultHaveParts.stream()
                .map(rhp -> rhp.getPartTest().getId())
                .collect(Collectors.toList());
    }

    private ExamResultResponse mapToExamResultResponse(ResultEntity result) {
        List<PartResultDetail> partResults = mapToPartResults(result.getResultHaveParts());
        List<String> completedPartIds = extractCompletedPartIds(result.getResultHaveParts());

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
                .completeTime(formatDuration(result.getCompleteTime()))
                .partResults(partResults)
                .questionResults(null)
                .build();
    }

    private static boolean isListeningPart(PartType partType) {
        return partType == PartType.PART_1_TOEIC ||
                partType == PartType.PART_2_TOEIC ||
                partType == PartType.PART_3_TOEIC ||
                partType == PartType.PART_4_TOEIC;
    }

    private Integer convertToToeicScore(int correctAnswers, int totalQuestions) {
        if (totalQuestions == 0) return 5; // Minimum score

        double percentage = (double) correctAnswers / totalQuestions;
        int rawScore = (int) Math.round(percentage * 495);

        int scaledScore = (rawScore / 5) * 5;

        if (scaledScore < 5) scaledScore = 5;
        if (scaledScore > 495) scaledScore = 495;

        return scaledScore;
    }

    private String formatDuration(Object completeTime) {
        if (completeTime instanceof Duration) {
            return ((Duration) completeTime).toMillis() / 1000 + "s";
        } else if (completeTime instanceof Long) {
            return ((Long) completeTime) / 1000 + "s";
        }
        return completeTime.toString();
    }

    // =============== HELPER CLASSES ===============

    @Data
    @AllArgsConstructor
    private static class ExamContext {
        private TestEntity test;
        private UserEntity user;
        private List<PartTestEntity> submittedParts;
        private boolean isFullTest;
    }

    @Data
    @AllArgsConstructor
    private static class ExamProcessingResult {
        private Map<String, PartResultData> partResultsMap;
        private List<QuestionResultDetail> questionResults;
        private int totalCorrect;
        private int listeningCorrect;
        private int readingCorrect;
        private int listeningTotal;
        private int readingTotal;
    }

    @Data
    @AllArgsConstructor
    private static class ExamScores {
        private int listeningScore;
        private int readingScore;
        private int totalScore;
    }

    @Data
    private static class PartResultData {
        private String partId;
        private String partName;
        private String partType;
        private PartTestEntity partEntity;
        private int correctCount = 0;
        private int totalCount = 0;
        private double accuracy = 0.0;

        public PartResultData(PartTestEntity part) {
            this.partId = part.getId();
            this.partName = part.getName();
            this.partType = part.getType().name();
            this.partEntity = part;
        }

        public void incrementCorrect() {
            correctCount++;
        }

        public void incrementTotal() {
            totalCount++;
        }

        public void calculateAccuracy() {
            if (totalCount > 0) {
                accuracy = Math.round(((double) correctCount / totalCount) * 10000.0) / 100.0;
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
