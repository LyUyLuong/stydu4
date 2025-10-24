package com.lul.Stydu4.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lul.Stydu4.dto.request.Test.TestCreationRequest;
import com.lul.Stydu4.dto.request.Test.TestSearchRequest;
import com.lul.Stydu4.dto.request.Test.TestUpdateRequest;
import com.lul.Stydu4.dto.response.PageResponse;
import com.lul.Stydu4.dto.response.Test.TestDetailResponse;
import com.lul.Stydu4.dto.response.Test.TestSummaryResponse;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.service.ITestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestController.class)
@AutoConfigureMockMvc(addFilters = false)  // Disable Security filters
@DisplayName("TestController Tests")
class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ITestService testService;

    private TestCreationRequest validCreationRequest;
    private TestUpdateRequest validUpdateRequest;
    private TestDetailResponse testDetailResponse;
    private TestSummaryResponse testSummaryResponse;

    @BeforeEach
    void setUp() {
        validCreationRequest = TestCreationRequest.builder()
                .name("TOEIC Practice Test 1")
                .description("Full TOEIC practice test")
                .status(1)
                .type("TOEIC")
                .numberOfParticipants(0L)
                .build();

        validUpdateRequest = TestUpdateRequest.builder()
                .name("Updated TOEIC Test")
                .description("Updated description")
                .status(1)
                .type("TOEIC")
                .numberOfParticipants(100L)
                .build();

        testDetailResponse = TestDetailResponse.builder()
                .id("test-123")
                .name("TOEIC Practice Test 1")
                .description("Full TOEIC practice test")
                .status(1)
                .type("TOEIC")
                .numberOfParticipants(0L)
                .slug("toeic-practice-test-1")
                .build();

        testSummaryResponse = TestSummaryResponse.builder()
                .id("test-123")
                .name("TOEIC Practice Test 1")
                .description("Full TOEIC practice test")
                .status(1)
                .type("TOEIC")
                .numberOfParticipants(0L)
                .slug("toeic-practice-test-1")
                .partsCount(7)
                .build();
    }

    @Nested
    @DisplayName("POST /tests - Create Test")
    class CreateTestTests {

        @Test
        @DisplayName("Should create test successfully with valid data")
        void createTest_ValidRequest_Success() throws Exception {
            // GIVEN
            when(testService.create(any(TestCreationRequest.class)))
                    .thenReturn(testDetailResponse);

            // WHEN
            ResultActions result = mockMvc.perform(post("/tests")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreationRequest)));

            // THEN
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.id").value("test-123"))
                    .andExpect(jsonPath("$.result.name").value("TOEIC Practice Test 1"))
                    .andExpect(jsonPath("$.result.description").value("Full TOEIC practice test"))
                    .andExpect(jsonPath("$.result.type").value("TOEIC"))
                    .andExpect(jsonPath("$.result.status").value(1))
                    .andExpect(jsonPath("$.result.numberOfParticipants").value(0));

            verify(testService, times(1)).create(any(TestCreationRequest.class));
        }

        @Test
        @DisplayName("Should create test even when name is null (no validation)")
        void createTest_NullName_Success() throws Exception {
            // GIVEN
            validCreationRequest.setName(null);
            when(testService.create(any(TestCreationRequest.class)))
                    .thenReturn(testDetailResponse);

            // WHEN
            ResultActions result = mockMvc.perform(post("/tests")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreationRequest)));

            // THEN - Should pass validation (no @NotNull on name field)
            result.andDo(print())
                    .andExpect(status().isOk());

            verify(testService, times(1)).create(any(TestCreationRequest.class));
        }
    }

    @Nested
    @DisplayName("POST /tests/with-files - Create Test With Audio")
    class CreateTestWithFilesTests {

        @Test
        @DisplayName("Should create test with audio file successfully")
        void createTestWithFiles_WithAudio_Success() throws Exception {
            // GIVEN
            MockMultipartFile audioFile = new MockMultipartFile(
                    "audio",
                    "test-audio.mp3",
                    "audio/mpeg",
                    "audio content".getBytes()
            );

            MockMultipartFile dataFile = new MockMultipartFile(
                    "data",
                    "",
                    "application/json",
                    objectMapper.writeValueAsString(validCreationRequest).getBytes()
            );

            TestDetailResponse responseWithAudio = TestDetailResponse.builder()
                    .id("test-123")
                    .name("TOEIC Practice Test 1")
                    .audioId("audio-123")
                    .audioUrl("http://example.com/audio/audio-123.mp3")
                    .build();

            when(testService.createWithAudio(any(TestCreationRequest.class), any()))
                    .thenReturn(responseWithAudio);

            // WHEN
            ResultActions result = mockMvc.perform(multipart("/tests/with-files")
                    .file(audioFile)
                    .file(dataFile));

            // THEN
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.id").value("test-123"))
                    .andExpect(jsonPath("$.result.audioId").value("audio-123"))
                    .andExpect(jsonPath("$.result.audioUrl").value("http://example.com/audio/audio-123.mp3"));

            verify(testService, times(1)).createWithAudio(any(TestCreationRequest.class), any());
        }

        @Test
        @DisplayName("Should create test without audio file")
        void createTestWithFiles_WithoutAudio_Success() throws Exception {
            // GIVEN
            MockMultipartFile dataFile = new MockMultipartFile(
                    "data",
                    "",
                    "application/json",
                    objectMapper.writeValueAsString(validCreationRequest).getBytes()
            );

            when(testService.createWithAudio(any(TestCreationRequest.class), eq(null)))
                    .thenReturn(testDetailResponse);

            // WHEN
            ResultActions result = mockMvc.perform(multipart("/tests/with-files")
                    .file(dataFile));

            // THEN
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.id").value("test-123"));

            verify(testService, times(1)).createWithAudio(any(TestCreationRequest.class), eq(null));
        }
    }

    @Nested
    @DisplayName("GET /tests - Get All Tests")
    class GetAllTestsTests {

        @Test
        @DisplayName("Should return paginated tests with default parameters")
        void getAllTests_DefaultParams_Success() throws Exception {
            // GIVEN
            PageResponse<TestSummaryResponse> pageResponse = PageResponse.<TestSummaryResponse>builder()
                    .currentPage(1)
                    .pageSize(5)
                    .totalPages(1)
                    .totalElements(2L)
                    .data(List.of(testSummaryResponse))
                    .build();

            when(testService.getAllTests(1, 5)).thenReturn(pageResponse);

            // WHEN
            ResultActions result = mockMvc.perform(get("/tests"));

            // THEN
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.currentPage").value(1))
                    .andExpect(jsonPath("$.result.pageSize").value(5))
                    .andExpect(jsonPath("$.result.totalElements").value(2))
                    .andExpect(jsonPath("$.result.data").isArray());

            verify(testService, times(1)).getAllTests(1, 5);
        }

        @Test
        @DisplayName("Should return paginated tests with custom parameters")
        void getAllTests_CustomParams_Success() throws Exception {
            // GIVEN
            PageResponse<TestSummaryResponse> pageResponse = PageResponse.<TestSummaryResponse>builder()
                    .currentPage(2)
                    .pageSize(10)
                    .totalPages(5)
                    .totalElements(50L)
                    .data(List.of(testSummaryResponse))
                    .build();

            when(testService.getAllTests(2, 10)).thenReturn(pageResponse);

            // WHEN
            ResultActions result = mockMvc.perform(get("/tests")
                    .param("page", "2")
                    .param("size", "10"));

            // THEN
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.currentPage").value(2))
                    .andExpect(jsonPath("$.result.pageSize").value(10))
                    .andExpect(jsonPath("$.result.totalElements").value(50));

            verify(testService, times(1)).getAllTests(2, 10);
        }

        @Test
        @DisplayName("Should return empty list when no tests exist")
        void getAllTests_EmptyList_Success() throws Exception {
            // GIVEN
            PageResponse<TestSummaryResponse> pageResponse = PageResponse.<TestSummaryResponse>builder()
                    .currentPage(1)
                    .pageSize(5)
                    .totalPages(0)
                    .totalElements(0L)
                    .data(List.of())
                    .build();

            when(testService.getAllTests(1, 5)).thenReturn(pageResponse);

            // WHEN
            ResultActions result = mockMvc.perform(get("/tests"));

            // THEN
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.totalElements").value(0))
                    .andExpect(jsonPath("$.result.data").isEmpty());

            verify(testService, times(1)).getAllTests(1, 5);
        }
    }

    @Nested
    @DisplayName("GET /tests/search-with-specification - Search Tests")
    class SearchTestsTests {

        @Test
        @DisplayName("Should search tests with all filters")
        void searchTests_AllFilters_Success() throws Exception {
            // GIVEN
            PageResponse<TestSummaryResponse> pageResponse = PageResponse.<TestSummaryResponse>builder()
                    .currentPage(1)
                    .pageSize(5)
                    .totalPages(1)
                    .totalElements(1L)
                    .data(List.of(testSummaryResponse))
                    .build();

            when(testService.searchTests(any(TestSearchRequest.class), any(Pageable.class)))
                    .thenReturn(pageResponse);

            // WHEN
            ResultActions result = mockMvc.perform(get("/tests/search-with-specification")
                    .param("name", "TOEIC")
                    .param("type", "TOEIC")
                    .param("status", "1")
                    .param("createdFrom", "2024-01-01")
                    .param("createdTo", "2024-12-31")
                    .param("page", "1")
                    .param("size", "5"));

            // THEN
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.totalElements").value(1))
                    .andExpect(jsonPath("$.result.data").isArray());

            verify(testService, times(1)).searchTests(any(TestSearchRequest.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search tests with partial filters")
        void searchTests_PartialFilters_Success() throws Exception {
            // GIVEN
            PageResponse<TestSummaryResponse> pageResponse = PageResponse.<TestSummaryResponse>builder()
                    .currentPage(1)
                    .pageSize(5)
                    .totalPages(1)
                    .totalElements(5L)
                    .data(List.of(testSummaryResponse))
                    .build();

            when(testService.searchTests(any(TestSearchRequest.class), any(Pageable.class)))
                    .thenReturn(pageResponse);

            // WHEN
            ResultActions result = mockMvc.perform(get("/tests/search-with-specification")
                    .param("name", "TOEIC"));

            // THEN
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.totalElements").value(5));

            verify(testService, times(1)).searchTests(any(TestSearchRequest.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should fail when page is less than 1")
        void searchTests_InvalidPage_ValidationError() throws Exception {
            // WHEN
            ResultActions result = mockMvc.perform(get("/tests/search-with-specification")
                    .param("page", "0"));

            // THEN
            result.andDo(print())
                    .andExpect(status().isBadRequest());

            verify(testService, never()).searchTests(any(TestSearchRequest.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should fail when size exceeds 100")
        void searchTests_InvalidSize_ValidationError() throws Exception {
            // WHEN
            ResultActions result = mockMvc.perform(get("/tests/search-with-specification")
                    .param("size", "101"));

            // THEN
            result.andDo(print())
                    .andExpect(status().isBadRequest());

            verify(testService, never()).searchTests(any(TestSearchRequest.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /tests/{testId} - Get Test By ID")
    class GetTestByIdTests {

        @Test
        @DisplayName("Should return test when ID exists")
        void getTestById_ValidId_Success() throws Exception {
            // GIVEN
            when(testService.getTestById("test-123")).thenReturn(testDetailResponse);

            // WHEN
            ResultActions result = mockMvc.perform(get("/tests/{testId}", "test-123"));

            // THEN
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.id").value("test-123"))
                    .andExpect(jsonPath("$.result.name").value("TOEIC Practice Test 1"))
                    .andExpect(jsonPath("$.result.type").value("TOEIC"));

            verify(testService, times(1)).getTestById("test-123");
        }

        @Test
        @DisplayName("Should fail when test ID not found")
        void getTestById_InvalidId_NotFound() throws Exception {
            // GIVEN
            when(testService.getTestById(anyString()))
                    .thenThrow(new AppException(ErrorCode.TEST_NOT_FOUND));

            // WHEN
            ResultActions result = mockMvc.perform(get("/tests/{testId}", "invalid-id"));

            // THEN
            result.andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.TEST_NOT_FOUND.getCode()));

            verify(testService, times(1)).getTestById("invalid-id");
        }
    }

    @Nested
    @DisplayName("PUT /tests/{testId} - Update Test")
    class UpdateTestTests {

        @Test
        @DisplayName("Should update test successfully with valid data")
        void updateTest_ValidRequest_Success() throws Exception {
            // GIVEN
            TestDetailResponse updatedResponse = TestDetailResponse.builder()
                    .id("test-123")
                    .name("Updated TOEIC Test")
                    .description("Updated description")
                    .status(1)
                    .type("TOEIC")
                    .numberOfParticipants(100L)
                    .build();

            when(testService.update(anyString(), any(TestUpdateRequest.class)))
                    .thenReturn(updatedResponse);

            // WHEN
            ResultActions result = mockMvc.perform(put("/tests/{testId}", "test-123")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateRequest)));

            // THEN
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.id").value("test-123"))
                    .andExpect(jsonPath("$.result.name").value("Updated TOEIC Test"))
                    .andExpect(jsonPath("$.result.description").value("Updated description"))
                    .andExpect(jsonPath("$.result.numberOfParticipants").value(100));

            verify(testService, times(1)).update(eq("test-123"), any(TestUpdateRequest.class));
        }

        @Test
        @DisplayName("Should fail when updating non-existent test")
        void updateTest_InvalidId_NotFound() throws Exception {
            // GIVEN
            when(testService.update(anyString(), any(TestUpdateRequest.class)))
                    .thenThrow(new AppException(ErrorCode.TEST_NOT_FOUND));

            // WHEN
            ResultActions result = mockMvc.perform(put("/tests/{testId}", "invalid-id")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateRequest)));

            // THEN
            result.andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.TEST_NOT_FOUND.getCode()));

            verify(testService, times(1)).update(eq("invalid-id"), any(TestUpdateRequest.class));
        }
    }

    @Nested
    @DisplayName("POST /tests/{testId}/audio - Update Test Audio")
    class UpdateTestAudioTests {

        @Test
        @DisplayName("Should update test audio successfully")
        void updateTestAudio_ValidFile_Success() throws Exception {
            // GIVEN
            MockMultipartFile audioFile = new MockMultipartFile(
                    "audio",
                    "updated-audio.mp3",
                    "audio/mpeg",
                    "updated audio content".getBytes()
            );

            TestDetailResponse responseWithAudio = TestDetailResponse.builder()
                    .id("test-123")
                    .name("TOEIC Practice Test 1")
                    .audioId("audio-456")
                    .audioUrl("http://example.com/audio/audio-456.mp3")
                    .build();

            when(testService.updateTestAudio(anyString(), any()))
                    .thenReturn(responseWithAudio);

            // WHEN
            ResultActions result = mockMvc.perform(multipart("/tests/{testId}/audio", "test-123")
                    .file(audioFile)
                    .with(request -> {
                        request.setMethod("POST");
                        return request;
                    }));

            // THEN
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.id").value("test-123"))
                    .andExpect(jsonPath("$.result.audioId").value("audio-456"))
                    .andExpect(jsonPath("$.result.audioUrl").value("http://example.com/audio/audio-456.mp3"));

            verify(testService, times(1)).updateTestAudio(eq("test-123"), any());
        }

        @Test
        @DisplayName("Should fail when test not found")
        void updateTestAudio_TestNotFound_NotFound() throws Exception {
            // GIVEN
            MockMultipartFile audioFile = new MockMultipartFile(
                    "audio",
                    "audio.mp3",
                    "audio/mpeg",
                    "audio content".getBytes()
            );

            when(testService.updateTestAudio(anyString(), any()))
                    .thenThrow(new AppException(ErrorCode.TEST_NOT_FOUND));

            // WHEN
            ResultActions result = mockMvc.perform(multipart("/tests/{testId}/audio", "invalid-id")
                    .file(audioFile)
                    .with(request -> {
                        request.setMethod("POST");
                        return request;
                    }));

            // THEN
            result.andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.TEST_NOT_FOUND.getCode()));

            verify(testService, times(1)).updateTestAudio(eq("invalid-id"), any());
        }
    }

    @Nested
    @DisplayName("DELETE /tests/{testId} - Delete Test")
    class DeleteTestTests {

        @Test
        @DisplayName("Should delete test successfully")
        void deleteTest_ValidId_Success() throws Exception {
            // GIVEN
            doNothing().when(testService).deleteTest("test-123");

            // WHEN
            ResultActions result = mockMvc.perform(delete("/tests/{testId}", "test-123"));

            // THEN
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("Test deleted"));

            verify(testService, times(1)).deleteTest("test-123");
        }

        @Test
        @DisplayName("Should fail when deleting non-existent test")
        void deleteTest_InvalidId_NotFound() throws Exception {
            // GIVEN
            doThrow(new AppException(ErrorCode.TEST_NOT_FOUND))
                    .when(testService).deleteTest("invalid-id");

            // WHEN
            ResultActions result = mockMvc.perform(delete("/tests/{testId}", "invalid-id"));

            // THEN
            result.andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.TEST_NOT_FOUND.getCode()));

            verify(testService, times(1)).deleteTest("invalid-id");
        }
    }
}
