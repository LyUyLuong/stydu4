package com.lul.Stydu4.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lul.Stydu4.dto.request.Lecture.LectureCreationRequest;
import com.lul.Stydu4.dto.request.Lecture.LectureReorderRequest;
import com.lul.Stydu4.dto.request.Lecture.LectureUpdateRequest;
import com.lul.Stydu4.dto.response.Lecture.LectureResponse;
import com.lul.Stydu4.service.ILectureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LectureController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("LectureController Tests")
class LectureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ILectureService lectureService;

    private LectureCreationRequest lectureCreationRequest;
    private LectureUpdateRequest lectureUpdateRequest;
    private LectureResponse lectureResponse;

    @BeforeEach
    void setUp() {
        // Setup lecture creation request
        lectureCreationRequest = LectureCreationRequest.builder()
                .title("Introduction to Java")
                .description("Learn the basics of Java programming")
                .type(com.lul.Stydu4.enums.LectureType.VIDEO)
                .content("https://example.com/video1.mp4")
                .duration(3600)
                .build();

        // Setup lecture update request
        lectureUpdateRequest = LectureUpdateRequest.builder()
                .title("Advanced Java Concepts")
                .description("Deep dive into advanced Java")
                .type(com.lul.Stydu4.enums.LectureType.VIDEO)
                .content("https://example.com/video2.mp4")
                .duration(4500)
                .build();

        // Setup lecture response
        lectureResponse = LectureResponse.builder()
                .id("lecture-123")
                .title("Introduction to Java")
                .description("Learn the basics of Java programming")
                .type(com.lul.Stydu4.enums.LectureType.VIDEO)
                .content("https://example.com/video1.mp4")
                .duration(3600)
                .orderIndex(1)
                .isPublished(false)
                .courseId("course-123")
                .build();
    }

    @Nested
    @DisplayName("POST /lectures/courses/{courseId} - Create Lecture Tests")
    class CreateLectureTests {

        @Test
        @DisplayName("Should create lecture successfully")
        void createLecture_ValidRequest_Success() throws Exception {
            // GIVEN
            when(lectureService.createLecture(eq("course-123"), any(LectureCreationRequest.class)))
                    .thenReturn(lectureResponse);

            // WHEN & THEN
            mockMvc.perform(post("/lectures/courses/course-123")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(lectureCreationRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.id").value("lecture-123"))
                    .andExpect(jsonPath("$.result.title").value("Introduction to Java"))
                    .andExpect(jsonPath("$.result.courseId").value("course-123"))
                    .andExpect(jsonPath("$.result.orderIndex").value(1));
        }
    }

    @Nested
    @DisplayName("PUT /lectures/{id} - Update Lecture Tests")
    class UpdateLectureTests {

        @Test
        @DisplayName("Should update lecture successfully")
        void updateLecture_ValidRequest_Success() throws Exception {
            // GIVEN
            LectureResponse updatedLecture = LectureResponse.builder()
                    .id("lecture-123")
                    .title("Advanced Java Concepts")
                    .description("Deep dive into advanced Java")
                    .content("https://example.com/video2.mp4")
                    .duration(4500)
                    .build();

            when(lectureService.updateLecture(eq("lecture-123"), any(LectureUpdateRequest.class)))
                    .thenReturn(updatedLecture);

            // WHEN & THEN
            mockMvc.perform(put("/lectures/lecture-123")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(lectureUpdateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.id").value("lecture-123"))
                    .andExpect(jsonPath("$.result.title").value("Advanced Java Concepts"))
                    .andExpect(jsonPath("$.result.duration").value(4500));
        }
    }

    @Nested
    @DisplayName("DELETE /lectures/{id} - Delete Lecture Tests")
    class DeleteLectureTests {

        @Test
        @DisplayName("Should delete lecture successfully")
        void deleteLecture_ValidId_Success() throws Exception {
            // GIVEN
            doNothing().when(lectureService).deleteLecture("lecture-123");

            // WHEN & THEN
            mockMvc.perform(delete("/lectures/lecture-123")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("Lecture deleted successfully"));
        }
    }

    @Nested
    @DisplayName("GET /lectures/{id} - Get Lecture Tests")
    class GetLectureTests {

        @Test
        @DisplayName("Should get lecture by id successfully")
        void getLecture_ValidId_Success() throws Exception {
            // GIVEN
            when(lectureService.getLectureById("lecture-123")).thenReturn(lectureResponse);

            // WHEN & THEN
            mockMvc.perform(get("/lectures/lecture-123")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.id").value("lecture-123"))
                    .andExpect(jsonPath("$.result.title").value("Introduction to Java"));
        }
    }

    @Nested
    @DisplayName("GET /lectures/courses/{courseId}/all - Get All Lectures Tests")
    class GetAllLecturesTests {

        @Test
        @DisplayName("Should get all lectures including unpublished for admin")
        void getAllLecturesByCourse_Admin_Success() throws Exception {
            // GIVEN
            List<LectureResponse> lectures = Arrays.asList(
                    lectureResponse,
                    LectureResponse.builder()
                            .id("lecture-456")
                            .title("Java Collections")
                            .isPublished(false)
                            .courseId("course-123")
                            .build()
            );

            when(lectureService.getAllLecturesByCourseId("course-123")).thenReturn(lectures);

            // WHEN & THEN
            mockMvc.perform(get("/lectures/courses/course-123/all")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").isArray())
                    .andExpect(jsonPath("$.result.length()").value(2))
                    .andExpect(jsonPath("$.result[0].id").value("lecture-123"))
                    .andExpect(jsonPath("$.result[1].id").value("lecture-456"));
        }
    }

    @Nested
    @DisplayName("GET /lectures/courses/{courseId} - Get Published Lectures Tests")
    class GetPublishedLecturesTests {

        @Test
        @DisplayName("Should get published lectures successfully")
        void getPublishedLecturesByCourse_Success() throws Exception {
            // GIVEN
            List<LectureResponse> lectures = Arrays.asList(
                    LectureResponse.builder()
                            .id("lecture-123")
                            .title("Introduction to Java")
                            .isPublished(true)
                            .courseId("course-123")
                            .build()
            );

            when(lectureService.getPublishedLecturesByCourseId("course-123")).thenReturn(lectures);

            // WHEN & THEN
            mockMvc.perform(get("/lectures/courses/course-123")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").isArray())
                    .andExpect(jsonPath("$.result.length()").value(1))
                    .andExpect(jsonPath("$.result[0].isPublished").value(true));
        }
    }

    @Nested
    @DisplayName("PUT /lectures/courses/{courseId}/reorder - Reorder Lectures Tests")
    class ReorderLecturesTests {

        @Test
        @DisplayName("Should reorder lectures successfully")
        void reorderLectures_ValidRequest_Success() throws Exception {
            // GIVEN
            List<String> lectureIds = Arrays.asList("lecture-456", "lecture-123");

            LectureReorderRequest reorderRequest = LectureReorderRequest.builder()
                    .lectureIds(lectureIds)
                    .build();

            List<LectureResponse> reorderedLectures = Arrays.asList(
                    LectureResponse.builder()
                            .id("lecture-456")
                            .orderIndex(1)
                            .build(),
                    LectureResponse.builder()
                            .id("lecture-123")
                            .orderIndex(2)
                            .build()
            );

            when(lectureService.reorderLectures(eq("course-123"), any(LectureReorderRequest.class)))
                    .thenReturn(reorderedLectures);

            // WHEN & THEN
            mockMvc.perform(put("/lectures/courses/course-123/reorder")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reorderRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").isArray())
                    .andExpect(jsonPath("$.result.length()").value(2))
                    .andExpect(jsonPath("$.result[0].orderIndex").value(1))
                    .andExpect(jsonPath("$.result[1].orderIndex").value(2));
        }
    }

    @Nested
    @DisplayName("PUT /lectures/{id}/publish - Publish Lecture Tests")
    class PublishLectureTests {

        @Test
        @DisplayName("Should publish lecture successfully")
        void publishLecture_ValidId_Success() throws Exception {
            // GIVEN
            LectureResponse publishedLecture = LectureResponse.builder()
                    .id("lecture-123")
                    .title("Introduction to Java")
                    .isPublished(true)
                    .build();

            when(lectureService.publishLecture("lecture-123")).thenReturn(publishedLecture);

            // WHEN & THEN
            mockMvc.perform(put("/lectures/lecture-123/publish")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.id").value("lecture-123"))
                    .andExpect(jsonPath("$.result.isPublished").value(true));
        }
    }

    @Nested
    @DisplayName("PUT /lectures/{id}/unpublish - Unpublish Lecture Tests")
    class UnpublishLectureTests {

        @Test
        @DisplayName("Should unpublish lecture successfully")
        void unpublishLecture_ValidId_Success() throws Exception {
            // GIVEN
            LectureResponse unpublishedLecture = LectureResponse.builder()
                    .id("lecture-123")
                    .title("Introduction to Java")
                    .isPublished(false)
                    .build();

            when(lectureService.unpublishLecture("lecture-123")).thenReturn(unpublishedLecture);

            // WHEN & THEN
            mockMvc.perform(put("/lectures/lecture-123/unpublish")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.id").value("lecture-123"))
                    .andExpect(jsonPath("$.result.isPublished").value(false));
        }
    }
}
