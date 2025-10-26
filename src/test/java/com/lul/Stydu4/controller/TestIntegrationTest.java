package com.lul.Stydu4.controller; // Hoặc package test của bạn

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lul.Stydu4.dto.request.Test.TestCreationRequest;
import com.lul.Stydu4.entity.FileEntity;
import com.lul.Stydu4.entity.TestEntity;
import com.lul.Stydu4.enums.FileType;
import com.lul.Stydu4.enums.TestType;
import com.lul.Stydu4.repository.IFileRepository;
import com.lul.Stydu4.repository.ITestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

// === THÊM IMPORT NÀY ===
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Đây là một Integration Test HOÀN CHỈNH.
 * ... (comment của bạn) ...
 */
@SpringBootTest
@AutoConfigureMockMvc // Giờ chúng ta không cần (addFilters = false) nữa
@ActiveProfiles("test")
@Transactional
@DisplayName("Full Integration Tests - Test Flow")
class TestIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ITestRepository testRepository;

    @Autowired
    private IFileRepository fileRepository;

    private FileEntity sampleAudioFile;

    @BeforeEach
    void setUp() {
        // @Transactional sẽ tự động rollback, nhưng chúng ta deleteAll để chắc chắn
        testRepository.deleteAll();
        fileRepository.deleteAll();

        // GIVEN: Một file audio tồn tại trong H2 Database
        sampleAudioFile = FileEntity.builder()
                .id("audio-it-123") // "it" = integration test
                .originalFilename("audio.mp3")
                .storedFilename("uuid-audio.mp3")
                .filePath("/audios/tests/...")
                .fileUrl("http://localhost:8080/api/v1/files/audio-it-123")
                .fileType(FileType.AUDIO)
                .fileSize(1024L)
                .contentType("audio/mpeg")
                .build();

        fileRepository.save(sampleAudioFile); // Lưu vào H2
    }

    @Test
    @DisplayName("Should Create Test, Save to H2, and Return DTO")
    // === THÊM DÒNG NÀY ĐỂ GIẢ LẬP USER ===
    // Giả lập 1 user tên "admin" với quyền "ADMIN"
    // (Hãy thay 'ADMIN' bằng quyền thực tế mà endpoint của bạn yêu cầu, ví dụ 'USER')
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createTest_FullFlow_Success() throws Exception {
        // --- GIVEN ---
        // Request DTO để tạo một Test mới
        TestCreationRequest creationRequest = TestCreationRequest.builder()
                .name("Full Integration Test")
                .description("Test from Web to H2 DB")
                .status(1)
                .type("TOEIC")
                .numberOfParticipants(0L)
                .audioId(sampleAudioFile.getId()) // Sử dụng ID của file đã tạo ở setUp
                .build();

        // --- WHEN ---
        // 1. Thực hiện request POST (Đi từ Controller -> Service -> Repository -> H2)
        ResultActions result = mockMvc.perform(post("/tests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creationRequest)));

        // --- THEN ---
        // 2. Kiểm tra HTTP Response (Controller trả về)
        MvcResult mvcResult = result.andDo(print())
                .andExpect(status().isOk()) // Test sẽ pass ở đây, trả về 200
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.id").isNotEmpty()) // ID được tạo tự động
                .andExpect(jsonPath("$.result.name").value("Full Integration Test"))
                .andExpect(jsonPath("$.result.audioId").value("audio-it-123"))
                .andReturn();

        // 3. Kiểm tra Database (Truy vấn H2 DB trực tiếp)
        // Lấy ID từ response để truy vấn
        String responseContent = mvcResult.getResponse().getContentAsString();
        String createdTestId = objectMapper.readTree(responseContent).at("/result/id").asText();

        // Truy vấn H2 DB bằng Repository THẬT
        Optional<TestEntity> savedEntityOpt = testRepository.findById(createdTestId);

        // Khẳng định rằng TestEntity đã được LƯU vào H2
        assertTrue(savedEntityOpt.isPresent(), "Test entity nên được tìm thấy trong H2 DB");

        TestEntity savedEntity = savedEntityOpt.get();
        assertEquals("Full Integration Test", savedEntity.getName());
        assertEquals(TestType.TOEIC, savedEntity.getType());
        assertNotNull(savedEntity.getAudio());
        assertEquals(sampleAudioFile.getId(), savedEntity.getAudio().getId());
    }
}