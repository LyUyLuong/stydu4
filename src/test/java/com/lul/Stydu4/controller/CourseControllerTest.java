package com.lul.Stydu4.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lul.Stydu4.dto.request.Course.CourseCreationRequest;
import com.lul.Stydu4.dto.response.Course.CourseResponse;
import com.lul.Stydu4.entity.FileEntity;
import com.lul.Stydu4.enums.FileType;
import com.lul.Stydu4.repository.IUserRepository;
import com.lul.Stydu4.service.ICourseService;
import com.lul.Stydu4.service.IEnrollmentService;
import com.lul.Stydu4.service.IFileStorageService;
import com.lul.Stydu4.service.IPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CourseController Tests")
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ICourseService courseService;

    @MockitoBean
    private IPaymentService paymentService;

    @MockitoBean
    private IEnrollmentService enrollmentService;

    @MockitoBean
    private IUserRepository userRepository;

    @MockitoBean
    private IFileStorageService fileStorageService;

    private CourseCreationRequest courseRequest;
    private CourseResponse courseResponse;

    @BeforeEach
    void setUp() {
        // Setup course request
        courseRequest = CourseCreationRequest.builder()
                .title("Introduction to Java Programming")
                .description("Learn Java from scratch")
                .price(BigDecimal.valueOf(99.99))
                .build();

        // Setup course response
        courseResponse = CourseResponse.builder()
                .id("course-123")
                .title("Introduction to Java Programming")
                .description("Learn Java from scratch")
                .price(BigDecimal.valueOf(99.99))
                .isPublished(false)
                .build();
    }

    @Nested
    @DisplayName("POST /courses - Create Course Tests")
    class CreateCourseTests {

        @Test
        @DisplayName("Should create course successfully")
        void createCourse_ValidRequest_Success() throws Exception {
            // GIVEN
            when(courseService.createCourse(any(CourseCreationRequest.class)))
                    .thenReturn(courseResponse);

            // WHEN & THEN
            mockMvc.perform(post("/courses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(courseRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.id").value("course-123"))
                    .andExpect(jsonPath("$.result.title").value("Introduction to Java Programming"))
                    .andExpect(jsonPath("$.result.price").value(99.99));
        }
    }

    @Nested
    @DisplayName("PUT /courses/{id} - Update Course Tests")
    class UpdateCourseTests {

        @Test
        @DisplayName("Should update course successfully")
        void updateCourse_ValidRequest_Success() throws Exception {
            // GIVEN
            CourseResponse updatedCourse = CourseResponse.builder()
                    .id("course-123")
                    .title("Advanced Java Programming")
                    .price(BigDecimal.valueOf(149.99))
                    .build();

            when(courseService.updateCourse(eq("course-123"), any(CourseCreationRequest.class)))
                    .thenReturn(updatedCourse);

            // WHEN & THEN
            mockMvc.perform(put("/courses/course-123")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(courseRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.id").value("course-123"))
                    .andExpect(jsonPath("$.result.title").value("Advanced Java Programming"));
        }
    }

    @Nested
    @DisplayName("GET /courses - Get All Courses Tests")
    class GetAllCoursesTests {

        @Test
        @DisplayName("Should get all published courses")
        void getAllCourses_Success() throws Exception {
            // GIVEN
            List<CourseResponse> courses = Arrays.asList(
                    courseResponse,
                    CourseResponse.builder()
                            .id("course-456")
                            .title("Python for Beginners")
                            .isPublished(true)
                            .build()
            );

            when(courseService.getAllPublishedCourses()).thenReturn(courses);

            // WHEN & THEN
            mockMvc.perform(get("/courses")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").isArray())
                    .andExpect(jsonPath("$.result.length()").value(2))
                    .andExpect(jsonPath("$.result[0].id").value("course-123"))
                    .andExpect(jsonPath("$.result[1].id").value("course-456"));
        }
    }

    @Nested
    @DisplayName("GET /courses/admin/all - Get All Courses For Admin Tests")
    class GetAllCoursesForAdminTests {

        @Test
        @DisplayName("Should get all courses including unpublished for admin")
        void getAllCoursesForAdmin_Success() throws Exception {
            // GIVEN
            List<CourseResponse> courses = Arrays.asList(
                    courseResponse,
                    CourseResponse.builder()
                            .id("course-456")
                            .title("Unpublished Course")
                            .isPublished(false)
                            .build()
            );

            when(courseService.getAllCourses()).thenReturn(courses);

            // WHEN & THEN
            mockMvc.perform(get("/courses/admin/all")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").isArray())
                    .andExpect(jsonPath("$.result.length()").value(2));
        }
    }

    @Nested
    @DisplayName("GET /courses/{id} - Get Course Tests")
    class GetCourseTests {

        @Test
        @DisplayName("Should get course by id successfully")
        void getCourse_ValidId_Success() throws Exception {
            // GIVEN
            when(courseService.getCourseById("course-123")).thenReturn(courseResponse);

            // WHEN & THEN
            mockMvc.perform(get("/courses/course-123")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.id").value("course-123"))
                    .andExpect(jsonPath("$.result.title").value("Introduction to Java Programming"));
        }
    }

    @Nested
    @DisplayName("PUT /courses/{id}/publish - Publish Course Tests")
    class PublishCourseTests {

        @Test
        @DisplayName("Should publish course successfully")
        void publishCourse_ValidId_Success() throws Exception {
            // GIVEN
            // publishCourse is a void method, no mocking needed

            // WHEN & THEN
            mockMvc.perform(put("/courses/course-123/publish")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("Course published"));
        }
    }

    @Nested
    @DisplayName("PUT /courses/{id}/unpublish - Unpublish Course Tests")
    class UnpublishCourseTests {

        @Test
        @DisplayName("Should unpublish course successfully")
        void unpublishCourse_ValidId_Success() throws Exception {
            // GIVEN
            // unpublishCourse is a void method, no mocking needed

            // WHEN & THEN
            mockMvc.perform(put("/courses/course-123/unpublish")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("Course unpublished"));
        }
    }

    @Nested
    @DisplayName("POST /courses/{id}/upload-image - Upload Course Image Tests")
    class UploadCourseImageTests {

        @Test
        @DisplayName("Should upload course image successfully")
        void uploadCourseImage_ValidFile_Success() throws Exception {
            // GIVEN
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "course-image.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            FileEntity uploadedFile = FileEntity.builder()
                    .id("file-123")
                    .fileUrl("https://example.com/images/course-image.jpg")
                    .fileType(FileType.IMAGE)
                    .build();

            CourseResponse updatedCourse = CourseResponse.builder()
                    .id("course-123")
                    .title("Introduction to Java Programming")
                    .imageUrl("https://example.com/images/course-image.jpg")
                    .build();

            when(fileStorageService.storeFile(any(), eq(FileType.IMAGE), eq("courses")))
                    .thenReturn(uploadedFile);
            when(courseService.updateCourseImage(eq("course-123"), anyString()))
                    .thenReturn(updatedCourse);

            // WHEN & THEN
            mockMvc.perform(multipart("/courses/course-123/upload-image")
                            .file(file)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Course image uploaded successfully"))
                    .andExpect(jsonPath("$.result.imageUrl").value("https://example.com/images/course-image.jpg"));
        }
    }
}
