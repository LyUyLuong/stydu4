package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.request.Test.TestCreationRequest;
import com.lul.Stydu4.dto.request.Test.TestSearchRequest;
import com.lul.Stydu4.dto.request.Test.TestUpdateRequest;
import com.lul.Stydu4.dto.response.PageResponse;
import com.lul.Stydu4.dto.response.PartTest.PartTestDetailResponse;
import com.lul.Stydu4.dto.response.Test.TestDetailResponse;
import com.lul.Stydu4.dto.response.Test.TestSummaryResponse;
import com.lul.Stydu4.entity.FileEntity;
import com.lul.Stydu4.entity.PartTestEntity;
import com.lul.Stydu4.entity.TestEntity;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.enums.FileType;
import com.lul.Stydu4.enums.PartType;
import com.lul.Stydu4.enums.TestType;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.mapper.PartTestMapper;
import com.lul.Stydu4.mapper.TestMapper;
import com.lul.Stydu4.repository.IFileRepository;
import com.lul.Stydu4.repository.IPartTestRepository;
import com.lul.Stydu4.repository.ITestRepository;
import com.lul.Stydu4.service.IFileStorageService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestServiceImplTest {

    @Mock
    private ITestRepository testRepository;

    @Mock
    private IPartTestRepository partTestRepository;

    @Mock
    private TestMapper testMapper;

    @Mock
    private PartTestMapper partTestMapper;

    @Mock
    private IFileStorageService fileStorageService;

    @Mock
    private IFileRepository fileRepository;

    @InjectMocks
    private TestServiceImpl testService;

    private TestEntity testEntity;
    private TestEntity savedTestEntity;
    private TestCreationRequest creationRequest;
    private TestUpdateRequest updateRequest;
    private TestDetailResponse detailResponse;
    private TestSummaryResponse summaryResponse;
    private PartTestEntity partTestEntity;
    private FileEntity audioFile;
    private MultipartFile mockAudioFile;
    private List<TestEntity> testList;
    private List<PartTestEntity> partList;
    private List<Object[]> countList;

    @BeforeEach
    void setUp() {
        // Create FileEntity for audio
        audioFile = FileEntity.builder()
                .id("audio-123")
                .originalFilename("audio.mp3")
                .storedFilename("uuid-audio.mp3")
                .filePath("/audios/tests/2025/10/22/uuid-audio.mp3")
                .fileUrl("http://localhost:8080/api/v1/files/audio-123")
                .fileType(FileType.AUDIO)
                .fileSize(1024L)
                .contentType("audio/mpeg")
                .build();

        // Create MockMultipartFile
        mockAudioFile = new MockMultipartFile(
                "audio",
                "audio.mp3",
                "audio/mpeg",
                "test audio content".getBytes()
        );

        testEntity = TestEntity.builder()
                .id("test-123")
                .name("Sample Test")
                .description("Description")
                .status(1)
                .numberOfParticipants(100L)
                .audio(audioFile)
                .type(TestType.TOEIC)
                .slug("sample-slug")
                .partTestEntities(new ArrayList<>())
                .build();

        savedTestEntity = TestEntity.builder()
                .id("test-123")
                .name("New Test")
                .description("Description")
                .status(1)
                .numberOfParticipants(100L)
                .audio(audioFile)
                .type(TestType.TOEIC)
                .slug("new-slug")
                .partTestEntities(new ArrayList<>())
                .build();

        partTestEntity = PartTestEntity.builder()
                .id("part-456")
                .name("Sample Part")
                .description("Part Desc")
                .type(PartType.PART_1_TOEIC)
                .testEntity(null)
                .questions(new ArrayList<>())
                .questionGroups(new ArrayList<>())
                .build();

        creationRequest = TestCreationRequest.builder()
                .name("New Test")
                .description("Description")
                .status(1)
                .numberOfParticipants(100L)
                .type("TOEIC")
                .audioId("audio-123")
                .build();

        updateRequest = TestUpdateRequest.builder()
                .name("Updated Test")
                .description("Updated Desc")
                .status(2)
                .numberOfParticipants(200L)
                .audioId("audio-123")
                .type("IELTS")
                .partTestIds(List.of("part-456"))
                .build();

        detailResponse = TestDetailResponse.builder()
                .id("test-123")
                .name("Sample Test")
                .description("Description")
                .status(1)
                .numberOfParticipants(100L)
                .audioId("audio-123")
                .audioUrl("http://localhost:8080/api/v1/files/audio-123")
                .type("TOEIC")
                .slug("sample-slug")
                .parts(new ArrayList<>())
                .build();

        summaryResponse = TestSummaryResponse.builder()
                .id("test-123")
                .name("Sample Test")
                .description("Description")
                .status(1)
                .numberOfParticipants(100L)
                .audioId("audio-123")
                .audioUrl("http://localhost:8080/api/v1/files/audio-123")
                .type("TOEIC")
                .slug("sample-slug")
                .partsCount(0)
                .build();

        testList = List.of(testEntity);
        partList = List.of(partTestEntity);

        countList = new ArrayList<>();
        countList.add(new Object[]{"test-123", 1L});
    }

    @AfterEach
    void tearDown() {
        reset(testRepository, partTestRepository, testMapper, partTestMapper, fileStorageService, fileRepository);
    }

    @Test
    void create_Success() {
        when(testMapper.toTestEntity(creationRequest)).thenReturn(testEntity);
        when(testRepository.save(testEntity)).thenReturn(savedTestEntity);
        when(testMapper.toTestResponse(savedTestEntity)).thenReturn(detailResponse);

        TestDetailResponse result = testService.create(creationRequest);

        assertNotNull(result);
        assertEquals("TOEIC", result.getType());
        assertEquals("test-123", result.getId());
        verify(testMapper).toTestEntity(creationRequest);
        verify(testRepository).save(testEntity);
        verify(testMapper).toTestResponse(savedTestEntity);
    }

    @Test
    void create_InvalidType_ThrowsException() {
        creationRequest.setType("INVALID");
        when(testMapper.toTestEntity(creationRequest)).thenReturn(testEntity);

        AppException exception = assertThrows(AppException.class, () -> testService.create(creationRequest));
        assertEquals(ErrorCode.INVALID_TEST_TYPE, exception.getErrorCode());
        verify(testRepository, never()).save(any());
    }

    @Test
    void createWithAudio_Success() {
        when(testMapper.toTestEntity(creationRequest)).thenReturn(testEntity);
        when(fileStorageService.storeFile(eq(mockAudioFile), eq(FileType.AUDIO), anyString())).thenReturn(audioFile);
        when(testRepository.save(testEntity)).thenReturn(savedTestEntity);
        when(testMapper.toTestResponse(savedTestEntity)).thenReturn(detailResponse);

        TestDetailResponse result = testService.createWithAudio(creationRequest, mockAudioFile);

        assertNotNull(result);
        assertEquals("TOEIC", result.getType());
        assertEquals("test-123", result.getId());
        verify(testMapper).toTestEntity(creationRequest);
        verify(fileStorageService).storeFile(eq(mockAudioFile), eq(FileType.AUDIO), anyString());
        verify(testRepository).save(testEntity);
        verify(testMapper).toTestResponse(savedTestEntity);
    }

    @Test
    void createWithAudio_NullAudio_Success() {
        when(testMapper.toTestEntity(creationRequest)).thenReturn(testEntity);
        when(testRepository.save(testEntity)).thenReturn(savedTestEntity);
        when(testMapper.toTestResponse(savedTestEntity)).thenReturn(detailResponse);

        TestDetailResponse result = testService.createWithAudio(creationRequest, null);

        assertNotNull(result);
        verify(testMapper).toTestEntity(creationRequest);
        verify(fileStorageService, never()).storeFile(any(), any(), any());
        verify(testRepository).save(testEntity);
    }

    @Test
    void createWithAudio_EmptyAudio_Success() {
        MultipartFile emptyFile = new MockMultipartFile("audio", "", "audio/mpeg", new byte[0]);
        when(testMapper.toTestEntity(creationRequest)).thenReturn(testEntity);
        when(testRepository.save(testEntity)).thenReturn(savedTestEntity);
        when(testMapper.toTestResponse(savedTestEntity)).thenReturn(detailResponse);

        TestDetailResponse result = testService.createWithAudio(creationRequest, emptyFile);

        assertNotNull(result);
        verify(fileStorageService, never()).storeFile(any(), any(), any());
        verify(testRepository).save(testEntity);
    }

    @Test
    void update_Success_NoPartChange() {
        updateRequest.setPartTestIds(null);
        when(testRepository.findById("test-123")).thenReturn(Optional.of(testEntity));
        doNothing().when(testMapper).updateTestEntityFromRequest(updateRequest, testEntity);
        when(testRepository.save(testEntity)).thenReturn(savedTestEntity);
        when(testMapper.toTestResponse(savedTestEntity)).thenReturn(detailResponse);

        TestDetailResponse result = testService.update("test-123", updateRequest);

        assertNotNull(result);
        verify(testRepository).findById("test-123");
        verify(testMapper).updateTestEntityFromRequest(updateRequest, testEntity);
        verify(testRepository).save(testEntity);
        verify(partTestRepository, never()).findAllById(any());
    }

    @Test
    void update_AddParts_Success() {
        testEntity.setPartTestEntities(new ArrayList<>());

        when(testRepository.findById("test-123")).thenReturn(Optional.of(testEntity));
        when(partTestRepository.findAllById(any())).thenReturn(partList);
        doNothing().when(testMapper).updateTestEntityFromRequest(updateRequest, testEntity);
        when(testRepository.save(testEntity)).thenReturn(savedTestEntity);
        when(testMapper.toTestResponse(savedTestEntity)).thenReturn(detailResponse);

        TestDetailResponse result = testService.update("test-123", updateRequest);

        assertNotNull(result);
        verify(partTestRepository).findAllById(any());
        assertEquals(1, testEntity.getPartTestEntities().size());
        assertSame(testEntity, partTestEntity.getTestEntity());
    }

    @Test
    void update_ClearParts_EmptyList() {
        List<String> emptyIds = new ArrayList<>();
        updateRequest.setPartTestIds(emptyIds);
        testEntity.setPartTestEntities(new ArrayList<>(List.of(partTestEntity)));

        when(testRepository.findById("test-123")).thenReturn(Optional.of(testEntity));
        when(partTestRepository.findAllById(any())).thenReturn(new ArrayList<>());
        doNothing().when(testMapper).updateTestEntityFromRequest(updateRequest, testEntity);
        when(testRepository.save(testEntity)).thenReturn(savedTestEntity);
        when(testMapper.toTestResponse(savedTestEntity)).thenReturn(detailResponse);

        TestDetailResponse result = testService.update("test-123", updateRequest);

        assertNotNull(result);
        assertTrue(testEntity.getPartTestEntities().isEmpty());
        assertNull(partTestEntity.getTestEntity());
    }

    @Test
    void update_TestNotFound_ThrowsException() {
        when(testRepository.findById("nonexistent")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> testService.update("nonexistent", updateRequest));
        assertEquals(ErrorCode.TEST_NOT_FOUND, exception.getErrorCode());
        verify(testRepository, never()).save(any());
    }

    @Test
    void update_InvalidTypeInRequest_ThrowsException() {
        updateRequest.setType("INVALID");
        when(testRepository.findById("test-123")).thenReturn(Optional.of(testEntity));

        AppException exception = assertThrows(AppException.class, () -> testService.update("test-123", updateRequest));
        assertEquals(ErrorCode.INVALID_TEST_TYPE, exception.getErrorCode());
    }

    @Test
    void updateTestAudio_Success() {
        FileEntity newAudioFile = FileEntity.builder()
                .id("audio-456")
                .originalFilename("new-audio.mp3")
                .storedFilename("uuid-new-audio.mp3")
                .filePath("/audios/tests/2025/10/22/uuid-new-audio.mp3")
                .fileUrl("http://localhost:8080/api/v1/files/audio-456")
                .fileType(FileType.AUDIO)
                .fileSize(2048L)
                .contentType("audio/mpeg")
                .build();

        when(testRepository.findById("test-123")).thenReturn(Optional.of(testEntity));
        when(fileStorageService.storeFile(eq(mockAudioFile), eq(FileType.AUDIO), anyString())).thenReturn(newAudioFile);
        when(testRepository.save(testEntity)).thenReturn(savedTestEntity);
        when(testMapper.toTestResponse(savedTestEntity)).thenReturn(detailResponse);

        TestDetailResponse result = testService.updateTestAudio("test-123", mockAudioFile);

        assertNotNull(result);
        verify(testRepository).findById("test-123");
        verify(fileStorageService).deleteFile("audio-123"); // Delete old audio
        verify(fileStorageService).storeFile(eq(mockAudioFile), eq(FileType.AUDIO), anyString());
        verify(testRepository, times(2)).save(testEntity); // Called twice: once to remove old audio, once to save new audio
    }

    @Test
    void updateTestAudio_NoOldAudio_Success() {
        testEntity.setAudio(null); // No existing audio
        FileEntity newAudioFile = FileEntity.builder()
                .id("audio-456")
                .fileType(FileType.AUDIO)
                .build();

        when(testRepository.findById("test-123")).thenReturn(Optional.of(testEntity));
        when(fileStorageService.storeFile(eq(mockAudioFile), eq(FileType.AUDIO), anyString())).thenReturn(newAudioFile);
        when(testRepository.save(testEntity)).thenReturn(savedTestEntity);
        when(testMapper.toTestResponse(savedTestEntity)).thenReturn(detailResponse);

        TestDetailResponse result = testService.updateTestAudio("test-123", mockAudioFile);

        assertNotNull(result);
        verify(fileStorageService, never()).deleteFile(any()); // No old audio to delete
        verify(fileStorageService).storeFile(eq(mockAudioFile), eq(FileType.AUDIO), anyString());
    }

    @Test
    void updateTestAudio_TestNotFound_ThrowsException() {
        when(testRepository.findById("nonexistent")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> testService.updateTestAudio("nonexistent", mockAudioFile));
        assertEquals(ErrorCode.TEST_NOT_FOUND, exception.getErrorCode());
        verify(fileStorageService, never()).storeFile(any(), any(), any());
    }

    @Test
    void updateTestAudio_NullAudio_Success() {
        when(testRepository.findById("test-123")).thenReturn(Optional.of(testEntity));
        when(testRepository.save(testEntity)).thenReturn(savedTestEntity);
        when(testMapper.toTestResponse(savedTestEntity)).thenReturn(detailResponse);

        TestDetailResponse result = testService.updateTestAudio("test-123", null);

        assertNotNull(result);
        verify(fileStorageService).deleteFile("audio-123"); // Delete old audio
        verify(fileStorageService, never()).storeFile(any(), any(), any());
    }

    @Test
    void getTestById_Success() {
        testEntity.setPartTestEntities(new ArrayList<>(List.of(partTestEntity)));
        PartTestDetailResponse partResponse = new PartTestDetailResponse();
        partResponse.setId("part-456");
        partResponse.setName("Sample Part");

        when(testRepository.findById("test-123")).thenReturn(Optional.of(testEntity));
        when(testMapper.toTestResponse(testEntity)).thenReturn(detailResponse);
        when(partTestMapper.toPartTestResponse(partTestEntity)).thenReturn(partResponse);

        TestDetailResponse result = testService.getTestById("test-123");

        assertNotNull(result);
        assertEquals(1, result.getParts().size());
        assertEquals("part-456", result.getParts().get(0).getId());
        assertEquals("TOEIC", result.getType());
        verify(testRepository).findById("test-123");
        verify(testMapper).toTestResponse(testEntity);
        verify(partTestMapper).toPartTestResponse(partTestEntity);
    }

    @Test
    void getTestById_NotFound_ThrowsException() {
        when(testRepository.findById("nonexistent")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> testService.getTestById("nonexistent"));
        assertEquals(ErrorCode.TEST_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void deleteTest_Success() {
        testService.deleteTest("test-123");
        verify(testRepository).deleteById("test-123");
    }

    @Test
    void getAllTests_Success() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<TestEntity> page = new PageImpl<>(testList, pageable, 1L);
        when(testRepository.findAllBy(any(Pageable.class))).thenReturn(page);
        when(testMapper.toTestSummary(testEntity)).thenReturn(summaryResponse);

        PageResponse<TestSummaryResponse> result = testService.getAllTests(1, 10);

        assertEquals(1, result.getData().size());
        assertEquals(1L, result.getTotalElements());
        assertEquals("TOEIC", result.getData().get(0).getType());
        verify(testRepository).findAllBy(any(Pageable.class));
        verify(testMapper).toTestSummary(testEntity);
    }

    @Test
    void getAllTests_InvalidPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<TestEntity> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0L);
        when(testRepository.findAllBy(any(Pageable.class))).thenReturn(emptyPage);

        PageResponse<TestSummaryResponse> result = testService.getAllTests(0, 10);

        assertEquals(0, result.getData().size());
        verify(testRepository).findAllBy(any(Pageable.class));
    }

    @Test
    void searchTests_Success_WithBulkCount() {
        TestSearchRequest searchReq = TestSearchRequest.builder().page(1).size(10).build();
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<TestEntity> page = new PageImpl<>(testList, pageable, 1L);

        when(testRepository.findAll(ArgumentMatchers.<Specification<TestEntity>>any(), any(Pageable.class))).thenReturn(page);
        when(partTestRepository.countByTestIds(anyList())).thenReturn(countList);
        when(testMapper.toTestSummary(testEntity)).thenReturn(summaryResponse);

        PageResponse<TestSummaryResponse> result = testService.searchTests(searchReq, pageable);

        assertEquals(1, result.getData().size());
        assertEquals(1, result.getData().get(0).getPartsCount());
        assertEquals("TOEIC", result.getData().get(0).getType());
        verify(testRepository).findAll(ArgumentMatchers.<Specification<TestEntity>>any(), any(Pageable.class));
        verify(partTestRepository).countByTestIds(anyList());
        verify(testMapper).toTestSummary(testEntity);
    }

    @Test
    void searchTests_EmptyPage_NoCountCalled() {
        TestSearchRequest searchReq = TestSearchRequest.builder().page(1).size(10).build();
        Pageable input = PageRequest.of(0, 10, Sort.unsorted());
        Page<TestEntity> emptyPage = Page.empty();

        when(testRepository.findAll(ArgumentMatchers.<Specification<TestEntity>>any(), any(Pageable.class))).thenReturn(emptyPage);

        PageResponse<TestSummaryResponse> result = testService.searchTests(searchReq, input);

        assertEquals(0, result.getData().size());
        verify(partTestRepository, never()).countByTestIds(anyList());
        verify(testRepository).findAll(ArgumentMatchers.<Specification<TestEntity>>any(), any(Pageable.class));
    }

    @Test
    void searchTests_SizeLimitExceeded() {
        TestSearchRequest searchReq = TestSearchRequest.builder().page(1).size(200).build();
        Pageable limited = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<TestEntity> page = new PageImpl<>(testList, limited, 1L);

        when(testRepository.findAll(ArgumentMatchers.<Specification<TestEntity>>any(), any(Pageable.class))).thenReturn(page);
        when(partTestRepository.countByTestIds(anyList())).thenReturn(countList);
        when(testMapper.toTestSummary(testEntity)).thenReturn(summaryResponse);

        PageResponse<TestSummaryResponse> result = testService.searchTests(searchReq, PageRequest.of(0, 200, Sort.unsorted()));

        assertEquals(100, result.getPageSize());
        verify(testRepository).findAll(ArgumentMatchers.<Specification<TestEntity>>any(), any(Pageable.class));
    }

    @Test
    void searchTests_InvalidSortFields_UsesDefault() {
        TestSearchRequest searchReq = TestSearchRequest.builder().page(1).size(10).build();
        Pageable invalid = PageRequest.of(0, 10, Sort.by("invalidField"));
        Page<TestEntity> page = new PageImpl<>(testList, PageRequest.of(0, 10), 1L);

        when(testRepository.findAll(ArgumentMatchers.<Specification<TestEntity>>any(), any(Pageable.class))).thenReturn(page);
        when(partTestRepository.countByTestIds(anyList())).thenReturn(new ArrayList<>());
        when(testMapper.toTestSummary(any(TestEntity.class))).thenReturn(summaryResponse);

        testService.searchTests(searchReq, invalid);

        verify(testRepository).findAll(ArgumentMatchers.<Specification<TestEntity>>any(), any(Pageable.class));
    }

    @Test
    void searchTests_InvalidType_EmptyResult() {
        TestSearchRequest searchReq = TestSearchRequest.builder()
                .page(1).size(10)
                .type("INVALID")
                .build();
        Page<TestEntity> empty = Page.empty();

        when(testRepository.findAll(ArgumentMatchers.<Specification<TestEntity>>any(), any(Pageable.class))).thenReturn(empty);

        PageResponse<TestSummaryResponse> result = testService.searchTests(searchReq, PageRequest.of(0, 10));

        assertEquals(0, result.getData().size());
        verify(partTestRepository, never()).countByTestIds(anyList());
    }

    @Test
    void searchTests_NameLike_PartialMatch() {
        TestSearchRequest searchReq = TestSearchRequest.builder()
                .page(1).size(10)
                .name("Sample")
                .build();
        Page<TestEntity> page = new PageImpl<>(testList, PageRequest.of(0, 10), 1L);

        when(testRepository.findAll(ArgumentMatchers.<Specification<TestEntity>>any(), any(Pageable.class))).thenReturn(page);
        when(partTestRepository.countByTestIds(anyList())).thenReturn(countList);
        when(testMapper.toTestSummary(testEntity)).thenReturn(summaryResponse);

        PageResponse<TestSummaryResponse> result = testService.searchTests(searchReq, PageRequest.of(0, 10));

        assertEquals(1, result.getData().size());
        verify(testRepository).findAll(ArgumentMatchers.<Specification<TestEntity>>any(), any(Pageable.class));
    }

    @Test
    void searchTests_StatusAndDateFilter() {
        TestSearchRequest searchReq = TestSearchRequest.builder()
                .page(1).size(10)
                .status(1)
                .createdFrom(LocalDate.now().minusDays(7))
                .build();
        Page<TestEntity> page = new PageImpl<>(testList, PageRequest.of(0, 10), 1L);

        when(testRepository.findAll(ArgumentMatchers.<Specification<TestEntity>>any(), any(Pageable.class))).thenReturn(page);
        when(partTestRepository.countByTestIds(anyList())).thenReturn(countList);
        when(testMapper.toTestSummary(testEntity)).thenReturn(summaryResponse);

        PageResponse<TestSummaryResponse> result = testService.searchTests(searchReq, PageRequest.of(0, 10));

        assertEquals(1, result.getData().size());
        verify(testRepository).findAll(ArgumentMatchers.<Specification<TestEntity>>any(), any(Pageable.class));
    }
}
