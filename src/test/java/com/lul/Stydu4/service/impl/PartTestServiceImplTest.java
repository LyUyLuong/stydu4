package com.lul.Stydu4.service.impl;


import com.lul.Stydu4.dto.request.PartTest.PartTestCreationRequest;
import com.lul.Stydu4.dto.request.PartTest.PartTestSearchRequest;
import com.lul.Stydu4.dto.request.PartTest.PartTestUpdateRequest;
import com.lul.Stydu4.dto.response.PageResponse;
import com.lul.Stydu4.dto.response.PartTest.PartTestDetailResponse;
import com.lul.Stydu4.dto.response.PartTest.PartTestSummaryResponse;
import com.lul.Stydu4.entity.FileEntity;
import com.lul.Stydu4.entity.PartTestEntity;
import com.lul.Stydu4.entity.QuestionGroupEntity;
import com.lul.Stydu4.entity.QuestionTestEntity;
import com.lul.Stydu4.entity.TestEntity;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.enums.FileType;
import com.lul.Stydu4.enums.PartType;
import com.lul.Stydu4.enums.TestType;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.mapper.PartTestMapper;
import com.lul.Stydu4.repository.IPartTestRepository;
import com.lul.Stydu4.repository.IQuestionGroupRepository;
import com.lul.Stydu4.repository.IQuestionTestRepository;
import com.lul.Stydu4.repository.ITestRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PartTestServiceImplTest {

    @Mock
    private IPartTestRepository partTestRepository;

    @Mock
    private ITestRepository testRepository;

    @Mock
    PartTestMapper partTestMapper;

    @Mock
    private IQuestionTestRepository questionTestRepository;

    @Mock
    private IQuestionGroupRepository questionGroupRepository;


    @InjectMocks
    private PartTestServiceImpl partTestService;

    private PartTestEntity partTestEntity;
    private PartTestEntity savedPartTestEntity;
    private TestEntity testEntity;
    private QuestionTestEntity question1;
    private QuestionTestEntity question2;
    private QuestionGroupEntity group1;
    private QuestionGroupEntity group2;
    private FileEntity audioFile1;
    private FileEntity audioFile2;
    private FileEntity imageFile1;
    private FileEntity imageFile2;
    private PartTestCreationRequest creationRequest;
    private PartTestUpdateRequest updateRequest;
    private PartTestDetailResponse detailResponse;
    private PartTestSummaryResponse summaryResponse;
    private List<PartTestEntity> partTestList;
    private List<QuestionTestEntity> questionList;
    private List<QuestionGroupEntity> groupList;
    private List<Object[]> questionCountList;
    private List<Object[]> groupCountList;

    @BeforeEach
    void setUp() {
        // Create FileEntities for audio and image
        audioFile1 = FileEntity.builder()
                .id("audio-q1")
                .originalFilename("q1.mp3")
                .storedFilename("uuid-q1.mp3")
                .filePath("/audios/questions/q1.mp3")
                .fileUrl("http://localhost:8080/api/v1/files/audio-q1")
                .fileType(FileType.AUDIO)
                .fileSize(2048L)
                .contentType("audio/mpeg")
                .build();

        audioFile2 = FileEntity.builder()
                .id("audio-conv1")
                .originalFilename("conv1.mp3")
                .storedFilename("uuid-conv1.mp3")
                .filePath("/audios/groups/conv1.mp3")
                .fileUrl("http://localhost:8080/api/v1/files/audio-conv1")
                .fileType(FileType.AUDIO)
                .fileSize(3072L)
                .contentType("audio/mpeg")
                .build();

        imageFile1 = FileEntity.builder()
                .id("image-q1")
                .originalFilename("q1.jpg")
                .storedFilename("uuid-q1.jpg")
                .filePath("/images/questions/q1.jpg")
                .fileUrl("http://localhost:8080/api/v1/files/image-q1")
                .fileType(FileType.IMAGE)
                .fileSize(5120L)
                .contentType("image/jpeg")
                .build();

        imageFile2 = FileEntity.builder()
                .id("image-passage1")
                .originalFilename("passage1.jpg")
                .storedFilename("uuid-passage1.jpg")
                .filePath("/images/groups/passage1.jpg")
                .fileUrl("http://localhost:8080/api/v1/files/image-passage1")
                .fileType(FileType.IMAGE)
                .fileSize(6144L)
                .contentType("image/jpeg")
                .build();

        testEntity = TestEntity.builder()
                .id("test-123")
                .name("TOEIC Practice Test")
                .description("Full TOEIC Test")
                .status(1)
                .numberOfParticipants(1000L)
                .audio(null)
                .type(TestType.TOEIC)
                .slug("toeic-practice-test")
                .partTestEntities(new ArrayList<>())
                .build();

        partTestEntity = PartTestEntity.builder()
                .id("part-123")
                .name("Part 1 - Photographs")
                .description("Describe photographs")
                .type(PartType.PART_1_TOEIC)
                .testEntity(testEntity)
                .questions(new ArrayList<>())
                .questionGroups(new ArrayList<>())
                .build();

        savedPartTestEntity = PartTestEntity.builder()
                .id("part-123")
                .name("Part 2 - Question-Response")
                .description("Question and response")
                .type(PartType.PART_2_TOEIC)
                .testEntity(testEntity)
                .questions(new ArrayList<>())
                .questionGroups(new ArrayList<>())
                .build();

        question1 = QuestionTestEntity.builder()
                .id("q1")
                .name("Question 1")
                .content("What is shown in the picture?")
                .audio(audioFile1)
                .image(imageFile1)
                .description("Picture description")
                .partEntity(null)
                .questionGroupEntity(null)
                .answers(new ArrayList<>())
                .build();

        question2 = QuestionTestEntity.builder()
                .id("q2")
                .name("Question 2")
                .content("Where is the meeting?")
                .audio(audioFile1)
                .image(null)
                .partEntity(null)
                .questionGroupEntity(null)
                .answers(new ArrayList<>())
                .build();

        group1 = QuestionGroupEntity.builder()
                .id("g1")
                .name("Reading Passage 1")
                .content("Lorem ipsum dolor sit amet...")
                .type("READING")
                .audio(null)
                .image(imageFile2)
                .partEntity(null)
                .questions(new ArrayList<>())
                .build();

        group2 = QuestionGroupEntity.builder()
                .id("g2")
                .name("Listening Conversation")
                .content("Conversation transcript")
                .type("LISTENING")
                .audio(audioFile2)
                .image(null)
                .partEntity(null)
                .questions(new ArrayList<>())
                .build();

        creationRequest = PartTestCreationRequest.builder()
                .name("Part 2 - Question-Response")
                .description("Question and response")
                .type("PART_2_TOEIC")
                .testId("test-123")
                .build();

        updateRequest = PartTestUpdateRequest.builder()
                .name("Part 1 - Updated")
                .description("Updated description")
                .type("PART_1_TOEIC")
                .questionIds(List.of("q1", "q2"))
                .questionGroupsIds(List.of("g1", "g2"))
                .build();

        detailResponse = PartTestDetailResponse.builder()
                .id("part-123")
                .name("Part 1 - Photographs")
                .description("Describe photographs")
                .type("PART_1_TOEIC")
                .build();

        summaryResponse = PartTestSummaryResponse.builder()
                .id("part-123")
                .name("Part 1 - Photographs")
                .description("Describe photographs")
                .type("PART_1_TOEIC")
                .questionsCount(0)
                .questionGroupsCount(0)
                .build();

        partTestList = List.of(partTestEntity);
        questionList = List.of(question1, question2);
        groupList = List.of(group1, group2);

        questionCountList = new ArrayList<>();
        questionCountList.add(new Object[]{"part-123", 6L});

        groupCountList = new ArrayList<>();
        groupCountList.add(new Object[]{"part-123", 3L});
    }

    @AfterEach
    void tearDown() {
        reset(partTestRepository, testRepository, partTestMapper,
                questionTestRepository, questionGroupRepository);
    }

    @Test
    void create_Success(){

        when(testRepository.findById("test-123")).thenReturn(Optional.of(testEntity));
        when(partTestMapper.toPartTestEntity(creationRequest)).thenReturn(partTestEntity);
        when(partTestRepository.save(partTestEntity)).thenReturn(savedPartTestEntity);
        when(partTestMapper.toPartTestResponse(savedPartTestEntity)).thenReturn(detailResponse);

        PartTestDetailResponse result = partTestService.create(creationRequest);

        assertNotNull(result);
        verify(testRepository).findById("test-123");
        assertEquals("part-123", result.getId());
        assertEquals("PART_1_TOEIC", result.getType());

        verify(partTestMapper).toPartTestEntity(creationRequest);
        verify(partTestRepository).save(partTestEntity);
        verify(partTestMapper).toPartTestResponse(savedPartTestEntity);

    }

    @Test
    void create_TestNotFound_ThrowsException() {
        when(partTestMapper.toPartTestEntity(creationRequest)).thenReturn(partTestEntity);
        when(testRepository.findById("test-123")).thenReturn(Optional.empty());

        AppException appException = assertThrows(AppException.class,
                () -> partTestService.create(creationRequest));

        assertEquals(ErrorCode.TEST_NOT_FOUND, appException.getErrorCode());
        verify(partTestRepository, never()).save(any());
    }

    @Test
    void create_NullTestId_Success() {
        creationRequest.setTestId(null);
        when(partTestMapper.toPartTestEntity(creationRequest)).thenReturn(partTestEntity);
        when(partTestRepository.save(partTestEntity)).thenReturn(savedPartTestEntity);
        when(partTestMapper.toPartTestResponse(savedPartTestEntity)).thenReturn(detailResponse);

        PartTestDetailResponse result = partTestService.create(creationRequest);

        assertNotNull(result);
        verify(testRepository, never()).findById(any());
        verify(partTestRepository).save(partTestEntity);
    }

    @Test
    void update_Success_AllFields() {
        partTestEntity.setQuestions(new ArrayList<>());
        partTestEntity.setQuestionGroups(new ArrayList<>());

        when(partTestRepository.findById("part-123")).thenReturn(Optional.of(partTestEntity));
        when(questionTestRepository.findAllById(any())).thenReturn(questionList);
        when(questionGroupRepository.findAllById(any())).thenReturn(groupList);
        when(partTestRepository.save(partTestEntity)).thenReturn(savedPartTestEntity);
        when(partTestMapper.toPartTestResponse(savedPartTestEntity)).thenReturn(detailResponse);

        PartTestDetailResponse result = partTestService.update("part-123", updateRequest);

        assertNotNull(result);
        assertEquals(2, partTestEntity.getQuestions().size());
        assertEquals(2, partTestEntity.getQuestionGroups().size());
        verify(partTestRepository).findById("part-123");
        verify(questionTestRepository).findAllById(updateRequest.getQuestionIds());
        verify(questionGroupRepository).findAllById(updateRequest.getQuestionGroupsIds());
        verify(partTestRepository).save(partTestEntity);
    }

    @Test
    void update_OnlyBasicFields_NoRelationshipUpdate() {
        updateRequest.setQuestionIds(null);
        updateRequest.setQuestionGroupsIds(null);

        when(partTestRepository.findById("part-123")).thenReturn(Optional.of(partTestEntity));
        when(partTestRepository.save(partTestEntity)).thenReturn(savedPartTestEntity);
        when(partTestMapper.toPartTestResponse(savedPartTestEntity)).thenReturn(detailResponse);

        PartTestDetailResponse result = partTestService.update("part-123", updateRequest);

        assertNotNull(result);
        verify(questionTestRepository, never()).findAllById(any());
        verify(questionGroupRepository, never()).findAllById(any());
        verify(partTestRepository).save(partTestEntity);
    }

    @Test
    void update_PartTestNotFound_ThrowsException() {
        when(partTestRepository.findById("nonexistent")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> partTestService.update("nonexistent", updateRequest));
        assertEquals(ErrorCode.PART_TEST_NOT_FOUND, exception.getErrorCode());
        verify(partTestRepository, never()).save(any());
    }

    @Test
    void update_InvalidType_ThrowsException() {
        updateRequest.setType("INVALID_TYPE");
        when(partTestRepository.findById("part-123")).thenReturn(Optional.of(partTestEntity));

        AppException exception = assertThrows(AppException.class,
                () -> partTestService.update("part-123", updateRequest));
        assertEquals(ErrorCode.INVALID_PART_TYPE, exception.getErrorCode());
        verify(partTestRepository, never()).save(any());
    }

    @Test
    void update_QuestionsNotFound_ThrowsException() {
        partTestEntity.setQuestions(new ArrayList<>());
        partTestEntity.setQuestionGroups(new ArrayList<>());

        when(partTestRepository.findById("part-123")).thenReturn(Optional.of(partTestEntity));
        when(questionTestRepository.findAllById(any())).thenReturn(List.of(question1)); // Only 1, expected 2

        AppException exception = assertThrows(AppException.class,
                () -> partTestService.update("part-123", updateRequest));
        assertEquals(ErrorCode.QUESTION_NOT_FOUND, exception.getErrorCode());
        verify(partTestRepository, never()).save(any());
    }

    @Test
    void update_QuestionGroupsNotFound_ThrowsException() {
        partTestEntity.setQuestions(new ArrayList<>());
        partTestEntity.setQuestionGroups(new ArrayList<>());

        when(partTestRepository.findById("part-123")).thenReturn(Optional.of(partTestEntity));
        when(questionTestRepository.findAllById(any())).thenReturn(questionList);
        when(questionGroupRepository.findAllById(any())).thenReturn(List.of(group1)); // Only 1, expected 2

        AppException exception = assertThrows(AppException.class,
                () -> partTestService.update("part-123", updateRequest));
        assertEquals(ErrorCode.QUESTION_GROUP_NOT_FOUND, exception.getErrorCode());
        verify(partTestRepository, never()).save(any());
    }

    @Test
    void update_ClearQuestionsAndGroups_EmptyList() {
        updateRequest.setQuestionIds(new ArrayList<>());
        updateRequest.setQuestionGroupsIds(new ArrayList<>());

        partTestEntity.setQuestions(new ArrayList<>(List.of(question1)));
        partTestEntity.setQuestionGroups(new ArrayList<>(List.of(group1)));

        when(partTestRepository.findById("part-123")).thenReturn(Optional.of(partTestEntity));
        when(questionTestRepository.findAllById(any())).thenReturn(new ArrayList<>());
        when(questionGroupRepository.findAllById(any())).thenReturn(new ArrayList<>());
        when(partTestRepository.save(partTestEntity)).thenReturn(savedPartTestEntity);
        when(partTestMapper.toPartTestResponse(savedPartTestEntity)).thenReturn(detailResponse);

        PartTestDetailResponse result = partTestService.update("part-123", updateRequest);

        assertNotNull(result);
        assertTrue(partTestEntity.getQuestions().isEmpty());
        assertTrue(partTestEntity.getQuestionGroups().isEmpty());
    }

    @Test
    void update_BlankName_NotUpdated() {
        updateRequest.setName("   ");
        updateRequest.setQuestionIds(null);
        updateRequest.setQuestionGroupsIds(null);
        String originalName = partTestEntity.getName();

        when(partTestRepository.findById("part-123")).thenReturn(Optional.of(partTestEntity));
        when(partTestRepository.save(partTestEntity)).thenReturn(savedPartTestEntity);
        when(partTestMapper.toPartTestResponse(savedPartTestEntity)).thenReturn(detailResponse);

        partTestService.update("part-123", updateRequest);

        assertEquals(originalName, partTestEntity.getName());
    }

    @Test
    void update_NullName_NotUpdated() {
        updateRequest.setName(null);
        updateRequest.setQuestionIds(null);
        updateRequest.setQuestionGroupsIds(null);
        String originalName = partTestEntity.getName();

        when(partTestRepository.findById("part-123")).thenReturn(Optional.of(partTestEntity));
        when(partTestRepository.save(partTestEntity)).thenReturn(savedPartTestEntity);
        when(partTestMapper.toPartTestResponse(savedPartTestEntity)).thenReturn(detailResponse);

        partTestService.update("part-123", updateRequest);

        assertEquals(originalName, partTestEntity.getName());
    }

    @Test
    void update_ReplaceExistingQuestions_Success() {
        QuestionTestEntity oldQuestion = QuestionTestEntity.builder()
                .id("old-q")
                .name("Old Question")
                .partEntity(partTestEntity)
                .answers(new ArrayList<>())
                .build();

        partTestEntity.setQuestions(new ArrayList<>(List.of(oldQuestion)));
        partTestEntity.setQuestionGroups(new ArrayList<>());

        updateRequest.setQuestionGroupsIds(null);

        when(partTestRepository.findById("part-123")).thenReturn(Optional.of(partTestEntity));
        when(questionTestRepository.findAllById(any())).thenReturn(questionList);
        when(partTestRepository.save(partTestEntity)).thenReturn(savedPartTestEntity);
        when(partTestMapper.toPartTestResponse(savedPartTestEntity)).thenReturn(detailResponse);

        partTestService.update("part-123", updateRequest);

        assertEquals(2, partTestEntity.getQuestions().size());
        assertFalse(partTestEntity.getQuestions().contains(oldQuestion));
    }

    @Test
    void update_ReplaceExistingQuestionGroups_Success() {
        QuestionGroupEntity oldGroup = QuestionGroupEntity.builder()
                .id("old-g")
                .name("Old Group")
                .partEntity(partTestEntity)
                .questions(new ArrayList<>())
                .build();

        partTestEntity.setQuestions(new ArrayList<>());
        partTestEntity.setQuestionGroups(new ArrayList<>(List.of(oldGroup)));

        updateRequest.setQuestionIds(null);

        when(partTestRepository.findById("part-123")).thenReturn(Optional.of(partTestEntity));
        when(questionGroupRepository.findAllById(any())).thenReturn(groupList);
        when(partTestRepository.save(partTestEntity)).thenReturn(savedPartTestEntity);
        when(partTestMapper.toPartTestResponse(savedPartTestEntity)).thenReturn(detailResponse);

        partTestService.update("part-123", updateRequest);

        assertEquals(2, partTestEntity.getQuestionGroups().size());
        assertFalse(partTestEntity.getQuestionGroups().contains(oldGroup));
    }


    @Test
    void getPartTestById_Success() {
        when(partTestRepository.findById("part-123")).thenReturn(Optional.of(partTestEntity));
        when(partTestMapper.toPartTestResponse(partTestEntity)).thenReturn(detailResponse);

        PartTestDetailResponse result = partTestService.getPartTestById("part-123");

        assertNotNull(result);
        assertEquals("part-123", result.getId());
        assertEquals("PART_1_TOEIC", result.getType());
        verify(partTestRepository).findById("part-123");
        verify(partTestMapper).toPartTestResponse(partTestEntity);
    }

    @Test
    void getPartTestById_NotFound_ThrowsException() {
        when(partTestRepository.findById("nonexistent")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> partTestService.getPartTestById("nonexistent"));
        assertEquals(ErrorCode.PART_TEST_NOT_FOUND, exception.getErrorCode());
    }

    // ==================== DELETE TESTS ====================

    @Test
    void deletePartTest_Success() {
        partTestService.deletePartTest("part-123");
        verify(partTestRepository).deleteById("part-123");
    }

    // ==================== GET ALL TESTS ====================

    @Test
    void getAllPartTests_Success() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<PartTestEntity> page = new PageImpl<>(partTestList, pageable, 1L);

        when(partTestRepository.findAllBy(any(Pageable.class))).thenReturn(page);
        when(partTestMapper.toPartTestSummary(partTestEntity)).thenReturn(summaryResponse);

        PageResponse<PartTestSummaryResponse> result = partTestService.getAllPartTests(1, 10);

        assertEquals(1, result.getData().size());
        assertEquals(1L, result.getTotalElements());
        assertEquals("PART_1_TOEIC", result.getData().get(0).getType());
        verify(partTestRepository).findAllBy(any(Pageable.class));
        verify(partTestMapper).toPartTestSummary(partTestEntity);
    }

    @Test
    void getAllPartTests_InvalidPage_AdjustsToZero() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<PartTestEntity> page = new PageImpl<>(partTestList, pageable, 1L);

        when(partTestRepository.findAllBy(any(Pageable.class))).thenReturn(page);
        when(partTestMapper.toPartTestSummary(partTestEntity)).thenReturn(summaryResponse);

        PageResponse<PartTestSummaryResponse> result = partTestService.getAllPartTests(-5, 10);

        assertEquals(1, result.getCurrentPage());
        verify(partTestRepository).findAllBy(any(Pageable.class));
    }

    @Test
    void getAllPartTests_EmptyResult() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<PartTestEntity> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0L);

        when(partTestRepository.findAllBy(any(Pageable.class))).thenReturn(emptyPage);

        PageResponse<PartTestSummaryResponse> result = partTestService.getAllPartTests(1, 10);

        assertEquals(0, result.getData().size());
        assertEquals(0L, result.getTotalElements());
    }

    @Test
    void getAllPartTests_LargePageSize() {
        Pageable pageable = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<PartTestEntity> page = new PageImpl<>(partTestList, pageable, 1L);

        when(partTestRepository.findAllBy(any(Pageable.class))).thenReturn(page);
        when(partTestMapper.toPartTestSummary(partTestEntity)).thenReturn(summaryResponse);

        PageResponse<PartTestSummaryResponse> result = partTestService.getAllPartTests(1, 50);

        assertEquals(50, result.getPageSize());
        verify(partTestRepository).findAllBy(any(Pageable.class));
    }

    // ==================== SEARCH TESTS ====================

    @Test
    void searchPartTests_Success_WithBulkCount() {
        PartTestSearchRequest searchReq = PartTestSearchRequest.builder()
                .page(1)
                .size(10)
                .build();
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<PartTestEntity> page = new PageImpl<>(partTestList, pageable, 1L);

        when(partTestRepository.findAll(ArgumentMatchers.<Specification<PartTestEntity>>any(), any(Pageable.class))).thenReturn(page);
        when(questionTestRepository.countQuestionsByPartIds(anyList())).thenReturn(questionCountList);
        when(questionGroupRepository.countQuestionGroupsByPartIds(anyList())).thenReturn(groupCountList);
        when(partTestMapper.toPartTestSummary(partTestEntity)).thenReturn(summaryResponse);

        PageResponse<PartTestSummaryResponse> result = partTestService.searchPartTests(searchReq, pageable);

        assertEquals(1, result.getData().size());
        assertEquals(6, result.getData().get(0).getQuestionsCount());
        assertEquals(3, result.getData().get(0).getQuestionGroupsCount());
        verify(partTestRepository).findAll(ArgumentMatchers.<Specification<PartTestEntity>>any(), any(Pageable.class));
        verify(questionTestRepository).countQuestionsByPartIds(anyList());
        verify(questionGroupRepository).countQuestionGroupsByPartIds(anyList());
    }

    @Test
    void searchPartTests_EmptyPage_NoCountCalled() {
        PartTestSearchRequest searchReq = PartTestSearchRequest.builder()
                .page(1)
                .size(10)
                .build();
        Pageable input = PageRequest.of(0, 10, Sort.unsorted());
        Page<PartTestEntity> emptyPage = Page.empty();

        when(partTestRepository.findAll(ArgumentMatchers.<Specification<PartTestEntity>>any(), any(Pageable.class))).thenReturn(emptyPage);

        PageResponse<PartTestSummaryResponse> result = partTestService.searchPartTests(searchReq, input);

        assertEquals(0, result.getData().size());
        verify(questionTestRepository, never()).countQuestionsByPartIds(anyList());
        verify(questionGroupRepository, never()).countQuestionGroupsByPartIds(anyList());
    }

    @Test
    void searchPartTests_SizeLimitExceeded() {
        PartTestSearchRequest searchReq = PartTestSearchRequest.builder()
                .page(1)
                .size(200)
                .build();
        Pageable limited = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<PartTestEntity> page = new PageImpl<>(partTestList, limited, 1L);

        when(partTestRepository.findAll(ArgumentMatchers.<Specification<PartTestEntity>>any(), any(Pageable.class))).thenReturn(page);
        when(questionTestRepository.countQuestionsByPartIds(anyList())).thenReturn(questionCountList);
        when(questionGroupRepository.countQuestionGroupsByPartIds(anyList())).thenReturn(groupCountList);
        when(partTestMapper.toPartTestSummary(partTestEntity)).thenReturn(summaryResponse);

        PageResponse<PartTestSummaryResponse> result = partTestService.searchPartTests(
                searchReq, PageRequest.of(0, 200, Sort.unsorted()));

        assertEquals(100, result.getPageSize());
        verify(partTestRepository).findAll(ArgumentMatchers.<Specification<PartTestEntity>>any(), any(Pageable.class));
    }

    @Test
    void searchPartTests_InvalidSortFields_UsesDefault() {
        PartTestSearchRequest searchReq = PartTestSearchRequest.builder()
                .page(1)
                .size(10)
                .build();
        Pageable invalid = PageRequest.of(0, 10, Sort.by("invalidField", "anotherInvalid"));
        Page<PartTestEntity> page = new PageImpl<>(partTestList, PageRequest.of(0, 10), 1L);

        when(partTestRepository.findAll(ArgumentMatchers.<Specification<PartTestEntity>>any(), any(Pageable.class))).thenReturn(page);
        when(questionTestRepository.countQuestionsByPartIds(anyList())).thenReturn(new ArrayList<>());
        when(questionGroupRepository.countQuestionGroupsByPartIds(anyList())).thenReturn(new ArrayList<>());
        when(partTestMapper.toPartTestSummary(any(PartTestEntity.class))).thenReturn(summaryResponse);

        partTestService.searchPartTests(searchReq, invalid);

        verify(partTestRepository).findAll(ArgumentMatchers.<Specification<PartTestEntity>>any(), any(Pageable.class));
    }

    @Test
    void searchPartTests_UnsortedProvided_UsesDefault() {
        PartTestSearchRequest searchReq = PartTestSearchRequest.builder()
                .page(1)
                .size(10)
                .build();
        Pageable unsorted = PageRequest.of(0, 10, Sort.unsorted());
        Page<PartTestEntity> page = new PageImpl<>(partTestList, PageRequest.of(0, 10), 1L);

        when(partTestRepository.findAll(ArgumentMatchers.<Specification<PartTestEntity>>any(), any(Pageable.class))).thenReturn(page);
        when(questionTestRepository.countQuestionsByPartIds(anyList())).thenReturn(questionCountList);
        when(questionGroupRepository.countQuestionGroupsByPartIds(anyList())).thenReturn(groupCountList);
        when(partTestMapper.toPartTestSummary(partTestEntity)).thenReturn(summaryResponse);

        partTestService.searchPartTests(searchReq, unsorted);

        verify(partTestRepository).findAll(ArgumentMatchers.<Specification<PartTestEntity>>any(), any(Pageable.class));
    }

    @Test
    void searchPartTests_NameFilter() {
        PartTestSearchRequest searchReq = PartTestSearchRequest.builder()
                .page(1)
                .size(10)
                .name("Part 1")
                .build();
        Page<PartTestEntity> page = new PageImpl<>(partTestList, PageRequest.of(0, 10), 1L);

        when(partTestRepository.findAll(ArgumentMatchers.<Specification<PartTestEntity>>any(), any(Pageable.class))).thenReturn(page);
        when(questionTestRepository.countQuestionsByPartIds(anyList())).thenReturn(questionCountList);
        when(questionGroupRepository.countQuestionGroupsByPartIds(anyList())).thenReturn(groupCountList);
        when(partTestMapper.toPartTestSummary(partTestEntity)).thenReturn(summaryResponse);

        PageResponse<PartTestSummaryResponse> result = partTestService.searchPartTests(
                searchReq, PageRequest.of(0, 10));

        assertEquals(1, result.getData().size());
        verify(partTestRepository).findAll(ArgumentMatchers.<Specification<PartTestEntity>>any(), any(Pageable.class));
    }

    @Test
    void searchPartTests_TypeAndStatusFilter() {
        PartTestSearchRequest searchReq = PartTestSearchRequest.builder()
                .page(1)
                .size(10)
                .type("PART_1_TOEIC")
                .build();
        Page<PartTestEntity> page = new PageImpl<>(partTestList, PageRequest.of(0, 10), 1L);

        when(partTestRepository.findAll(ArgumentMatchers.<Specification<PartTestEntity>>any(), any(Pageable.class))).thenReturn(page);
        when(questionTestRepository.countQuestionsByPartIds(anyList())).thenReturn(questionCountList);
        when(questionGroupRepository.countQuestionGroupsByPartIds(anyList())).thenReturn(groupCountList);
        when(partTestMapper.toPartTestSummary(partTestEntity)).thenReturn(summaryResponse);

        PageResponse<PartTestSummaryResponse> result = partTestService.searchPartTests(
                searchReq, PageRequest.of(0, 10));

        assertEquals(1, result.getData().size());
        verify(partTestRepository).findAll(ArgumentMatchers.<Specification<PartTestEntity>>any(), any(Pageable.class));
    }

    @Test
    void searchPartTests_DateRangeFilter() {
        PartTestSearchRequest searchReq = PartTestSearchRequest.builder()
                .page(1)
                .size(10)
                .createdFrom(LocalDate.now().minusDays(30))
                .createdTo(LocalDate.now())
                .build();
        Page<PartTestEntity> page = new PageImpl<>(partTestList, PageRequest.of(0, 10), 1L);

        when(partTestRepository.findAll(ArgumentMatchers.<Specification<PartTestEntity>>any(), any(Pageable.class))).thenReturn(page);
        when(questionTestRepository.countQuestionsByPartIds(anyList())).thenReturn(questionCountList);
        when(questionGroupRepository.countQuestionGroupsByPartIds(anyList())).thenReturn(groupCountList);
        when(partTestMapper.toPartTestSummary(partTestEntity)).thenReturn(summaryResponse);

        PageResponse<PartTestSummaryResponse> result = partTestService.searchPartTests(
                searchReq, PageRequest.of(0, 10));

        assertEquals(1, result.getData().size());
        verify(partTestRepository).findAll(ArgumentMatchers.<Specification<PartTestEntity>>any(), any(Pageable.class));
    }

    @Test
    void searchPartTests_ValidSortFields() {
        PartTestSearchRequest searchReq = PartTestSearchRequest.builder()
                .page(1)
                .size(10)
                .build();
        Pageable sortByName = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.ASC, "name", "type"));
        Page<PartTestEntity> page = new PageImpl<>(partTestList, sortByName, 1L);

        when(partTestRepository.findAll(ArgumentMatchers.<Specification<PartTestEntity>>any(), any(Pageable.class))).thenReturn(page);
        when(questionTestRepository.countQuestionsByPartIds(anyList())).thenReturn(questionCountList);
        when(questionGroupRepository.countQuestionGroupsByPartIds(anyList())).thenReturn(groupCountList);
        when(partTestMapper.toPartTestSummary(partTestEntity)).thenReturn(summaryResponse);

        partTestService.searchPartTests(searchReq, sortByName);

        verify(partTestRepository).findAll(ArgumentMatchers.<Specification<PartTestEntity>>any(), any(Pageable.class));
    }

    @Test
    void searchPartTests_MixedValidInvalidSort_FiltersInvalid() {
        PartTestSearchRequest searchReq = PartTestSearchRequest.builder()
                .page(1)
                .size(10)
                .build();
        Pageable mixed = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.ASC, "name", "invalidField", "status"));
        Page<PartTestEntity> page = new PageImpl<>(partTestList, PageRequest.of(0, 10), 1L);

        when(partTestRepository.findAll(ArgumentMatchers.<Specification<PartTestEntity>>any(), any(Pageable.class))).thenReturn(page);
        when(questionTestRepository.countQuestionsByPartIds(anyList())).thenReturn(questionCountList);
        when(questionGroupRepository.countQuestionGroupsByPartIds(anyList())).thenReturn(groupCountList);
        when(partTestMapper.toPartTestSummary(partTestEntity)).thenReturn(summaryResponse);

        partTestService.searchPartTests(searchReq, mixed);

        verify(partTestRepository).findAll(ArgumentMatchers.<Specification<PartTestEntity>>any(), any(Pageable.class));
    }

    @Test
    void searchPartTests_MissingCountData_DefaultsToZero() {
        PartTestSearchRequest searchReq = PartTestSearchRequest.builder()
                .page(1)
                .size(10)
                .build();
        Page<PartTestEntity> page = new PageImpl<>(partTestList, PageRequest.of(0, 10), 1L);

        when(partTestRepository.findAll(ArgumentMatchers.<Specification<PartTestEntity>>any(), any(Pageable.class))).thenReturn(page);
        when(questionTestRepository.countQuestionsByPartIds(anyList())).thenReturn(new ArrayList<>());
        when(questionGroupRepository.countQuestionGroupsByPartIds(anyList())).thenReturn(new ArrayList<>());
        when(partTestMapper.toPartTestSummary(partTestEntity)).thenReturn(summaryResponse);

        PageResponse<PartTestSummaryResponse> result = partTestService.searchPartTests(
                searchReq, PageRequest.of(0, 10));

        assertEquals(1, result.getData().size());
        assertEquals(0, result.getData().get(0).getQuestionsCount());
        assertEquals(0, result.getData().get(0).getQuestionGroupsCount());
    }

    @Test
    void searchPartTests_MultipleFilters_CombinedSearch() {
        PartTestSearchRequest searchReq = PartTestSearchRequest.builder()
                .page(1)
                .size(10)
                .name("Part")
                .type("PART_1_TOEIC")
                .createdFrom(LocalDate.now().minusMonths(1))
                .build();
        Page<PartTestEntity> page = new PageImpl<>(partTestList, PageRequest.of(0, 10), 1L);

        when(partTestRepository.findAll(ArgumentMatchers.<Specification<PartTestEntity>>any(), any(Pageable.class))).thenReturn(page);
        when(questionTestRepository.countQuestionsByPartIds(anyList())).thenReturn(questionCountList);
        when(questionGroupRepository.countQuestionGroupsByPartIds(anyList())).thenReturn(groupCountList);
        when(partTestMapper.toPartTestSummary(partTestEntity)).thenReturn(summaryResponse);

        PageResponse<PartTestSummaryResponse> result = partTestService.searchPartTests(
                searchReq, PageRequest.of(0, 10));

        assertEquals(1, result.getData().size());
        verify(partTestRepository).findAll(ArgumentMatchers.<Specification<PartTestEntity>>any(), any(Pageable.class));
    }

    @Test
    void searchPartTests_PageZero_AdjustsToZero() {
        PartTestSearchRequest searchReq = PartTestSearchRequest.builder()
                .page(0)
                .size(10)
                .build();
        Page<PartTestEntity> page = new PageImpl<>(partTestList, PageRequest.of(0, 10), 1L);

        when(partTestRepository.findAll(ArgumentMatchers.<Specification<PartTestEntity>>any(), any(Pageable.class))).thenReturn(page);
        when(questionTestRepository.countQuestionsByPartIds(anyList())).thenReturn(questionCountList);
        when(questionGroupRepository.countQuestionGroupsByPartIds(anyList())).thenReturn(groupCountList);
        when(partTestMapper.toPartTestSummary(partTestEntity)).thenReturn(summaryResponse);

        PageResponse<PartTestSummaryResponse> result = partTestService.searchPartTests(
                searchReq, PageRequest.of(0, 10));

        assertEquals(1, result.getCurrentPage());
        verify(partTestRepository).findAll(ArgumentMatchers.<Specification<PartTestEntity>>any(), any(Pageable.class));
    }

    @Test
    void searchPartTests_MultiplePartTests_BulkCountCorrect() {
        PartTestEntity part2 = PartTestEntity.builder()
                .id("part-456")
                .name("Part 2")
                .type(PartType.PART_2_TOEIC)
                .questions(new ArrayList<>())
                .questionGroups(new ArrayList<>())
                .build();

        List<PartTestEntity> multipleParts = List.of(partTestEntity, part2);
        PartTestSearchRequest searchReq = PartTestSearchRequest.builder()
                .page(1)
                .size(10)
                .build();
        Page<PartTestEntity> page = new PageImpl<>(multipleParts, PageRequest.of(0, 10), 2L);

        List<Object[]> multiCountList = new ArrayList<>();
        multiCountList.add(new Object[]{"part-123", 6L});
        multiCountList.add(new Object[]{"part-456", 4L});

        List<Object[]> multiGroupList = new ArrayList<>();
        multiGroupList.add(new Object[]{"part-123", 3L});
        multiGroupList.add(new Object[]{"part-456", 2L});

        PartTestSummaryResponse summary2 = PartTestSummaryResponse.builder()
                .id("part-456")
                .name("Part 2")
                .questionsCount(0)
                .questionGroupsCount(0)
                .build();

        when(partTestRepository.findAll(ArgumentMatchers.<Specification<PartTestEntity>>any(), any(Pageable.class))).thenReturn(page);
        when(questionTestRepository.countQuestionsByPartIds(anyList())).thenReturn(multiCountList);
        when(questionGroupRepository.countQuestionGroupsByPartIds(anyList())).thenReturn(multiGroupList);
        when(partTestMapper.toPartTestSummary(partTestEntity)).thenReturn(summaryResponse);
        when(partTestMapper.toPartTestSummary(part2)).thenReturn(summary2);

        PageResponse<PartTestSummaryResponse> result = partTestService.searchPartTests(
                searchReq, PageRequest.of(0, 10));

        assertEquals(2, result.getData().size());
        assertEquals(6, result.getData().get(0).getQuestionsCount());
        assertEquals(4, result.getData().get(1).getQuestionsCount());
        assertEquals(3, result.getData().get(0).getQuestionGroupsCount());
        assertEquals(2, result.getData().get(1).getQuestionGroupsCount());
    }
}

