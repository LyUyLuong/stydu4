package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.request.Course.CourseCreationRequest;
import com.lul.Stydu4.dto.response.Course.CourseResponse;
import com.lul.Stydu4.entity.CourseEntity;
import com.lul.Stydu4.entity.TestEntity;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.repository.ICourseRepository;
import com.lul.Stydu4.repository.ITestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseServiceImpl Tests")
class CourseServiceImplTest {

    @Mock
    private ICourseRepository courseRepository;

    @Mock
    private ITestRepository testRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    private CourseEntity course;
    private CourseCreationRequest creationRequest;
    private TestEntity test1;
    private TestEntity test2;

    @BeforeEach
    void setUp() {
        test1 = TestEntity.builder()
                .id("test-1")
                .name("TOEIC Test 1")
                .build();

        test2 = TestEntity.builder()
                .id("test-2")
                .name("TOEIC Test 2")
                .build();

        creationRequest = CourseCreationRequest.builder()
                .title("TOEIC Complete Course")
                .description("Complete TOEIC preparation")
                .price(new BigDecimal("99.99"))
                .imageUrl("https://example.com/image.jpg")
                .duration(30)
                .testIds(List.of("test-1", "test-2"))
                .build();

        course = CourseEntity.builder()
                .id("course-123")
                .title("TOEIC Complete Course")
                .description("Complete TOEIC preparation")
                .price(new BigDecimal("99.99"))
                .imageUrl("https://example.com/image.jpg")
                .duration(30)
                .isPublished(false)
                .tests(List.of(test1, test2))
                .build();
    }

    @Nested
    @DisplayName("createCourse Tests")
    class CreateCourseTests {

        @Test
        @DisplayName("Should create course with tests successfully")
        void createCourse_WithTests_Success() {
            // GIVEN
            when(testRepository.findAllById(List.of("test-1", "test-2")))
                    .thenReturn(List.of(test1, test2));
            when(courseRepository.save(any(CourseEntity.class))).thenReturn(course);

            // WHEN
            CourseResponse result = courseService.createCourse(creationRequest);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("TOEIC Complete Course");
            assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
            assertThat(result.getTestCount()).isEqualTo(2);

            ArgumentCaptor<CourseEntity> captor = ArgumentCaptor.forClass(CourseEntity.class);
            verify(courseRepository).save(captor.capture());
            
            CourseEntity savedCourse = captor.getValue();
            assertThat(savedCourse.getTitle()).isEqualTo("TOEIC Complete Course");
            assertThat(savedCourse.getTests()).hasSize(2);
        }

        @Test
        @DisplayName("Should create course without tests")
        void createCourse_WithoutTests_Success() {
            // GIVEN
            creationRequest.setTestIds(null);
            CourseEntity courseWithoutTests = CourseEntity.builder()
                    .id("course-123")
                    .title("TOEIC Complete Course")
                    .description("Complete TOEIC preparation")
                    .price(new BigDecimal("99.99"))
                    .tests(new ArrayList<>())
                    .build();

            when(courseRepository.save(any(CourseEntity.class))).thenReturn(courseWithoutTests);

            // WHEN
            CourseResponse result = courseService.createCourse(creationRequest);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("TOEIC Complete Course");
            assertThat(result.getTestCount()).isZero();

            verify(testRepository, never()).findAllById(anyList());
            verify(courseRepository).save(any(CourseEntity.class));
        }

        @Test
        @DisplayName("Should create course with empty test list")
        void createCourse_EmptyTestList_Success() {
            // GIVEN
            creationRequest.setTestIds(List.of());
            CourseEntity courseWithoutTests = CourseEntity.builder()
                    .id("course-123")
                    .title("TOEIC Complete Course")
                    .tests(new ArrayList<>())
                    .build();

            when(courseRepository.save(any(CourseEntity.class))).thenReturn(courseWithoutTests);

            // WHEN
            CourseResponse result = courseService.createCourse(creationRequest);

            // THEN
            assertThat(result).isNotNull();
            verify(testRepository, never()).findAllById(anyList());
        }
    }

    @Nested
    @DisplayName("getCourseById Tests")
    class GetCourseByIdTests {

        @Test
        @DisplayName("Should return course when ID exists")
        void getCourseById_ValidId_Success() {
            // GIVEN
            when(courseRepository.findById("course-123")).thenReturn(Optional.of(course));

            // WHEN
            CourseResponse result = courseService.getCourseById("course-123");

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("course-123");
            assertThat(result.getTitle()).isEqualTo("TOEIC Complete Course");

            verify(courseRepository).findById("course-123");
        }

        @Test
        @DisplayName("Should throw exception when course not found")
        void getCourseById_InvalidId_ThrowException() {
            // GIVEN
            when(courseRepository.findById("invalid-id")).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> courseService.getCourseById("invalid-id"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_NOT_FOUND);

            verify(courseRepository).findById("invalid-id");
        }
    }

    @Nested
    @DisplayName("getAllPublishedCourses Tests")
    class GetAllPublishedCoursesTests {

        @Test
        @DisplayName("Should return only published courses")
        void getAllPublishedCourses_Success() {
            // GIVEN
            CourseEntity course2 = CourseEntity.builder()
                    .id("course-456")
                    .title("Advanced TOEIC")
                    .isPublished(true)
                    .tests(List.of())
                    .build();

            course.setIsPublished(true);

            when(courseRepository.findByIsPublished(true)).thenReturn(List.of(course, course2));

            // WHEN
            List<CourseResponse> result = courseService.getAllPublishedCourses();

            // THEN
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getTitle()).isEqualTo("TOEIC Complete Course");
            assertThat(result.get(1).getTitle()).isEqualTo("Advanced TOEIC");

            verify(courseRepository).findByIsPublished(true);
        }

        @Test
        @DisplayName("Should return empty list when no published courses")
        void getAllPublishedCourses_EmptyList() {
            // GIVEN
            when(courseRepository.findByIsPublished(true)).thenReturn(List.of());

            // WHEN
            List<CourseResponse> result = courseService.getAllPublishedCourses();

            // THEN
            assertThat(result).isEmpty();
            verify(courseRepository).findByIsPublished(true);
        }
    }

    @Nested
    @DisplayName("getAllCourses Tests")
    class GetAllCoursesTests {

        @Test
        @DisplayName("Should return all courses regardless of published status")
        void getAllCourses_Success() {
            // GIVEN
            CourseEntity publishedCourse = CourseEntity.builder()
                    .id("course-456")
                    .title("Published Course")
                    .isPublished(true)
                    .tests(List.of())
                    .build();

            CourseEntity unpublishedCourse = CourseEntity.builder()
                    .id("course-789")
                    .title("Unpublished Course")
                    .isPublished(false)
                    .tests(List.of())
                    .build();

            when(courseRepository.findAll()).thenReturn(List.of(publishedCourse, unpublishedCourse));

            // WHEN
            List<CourseResponse> result = courseService.getAllCourses();

            // THEN
            assertThat(result).hasSize(2);
            verify(courseRepository).findAll();
        }
    }

    @Nested
    @DisplayName("getCourseEntityById Tests")
    class GetCourseEntityByIdTests {

        @Test
        @DisplayName("Should return course entity when ID exists")
        void getCourseEntityById_ValidId_Success() {
            // GIVEN
            when(courseRepository.findById("course-123")).thenReturn(Optional.of(course));

            // WHEN
            CourseEntity result = courseService.getCourseEntityById("course-123");

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("course-123");
            assertThat(result.getTitle()).isEqualTo("TOEIC Complete Course");

            verify(courseRepository).findById("course-123");
        }

        @Test
        @DisplayName("Should throw exception when course not found")
        void getCourseEntityById_InvalidId_ThrowException() {
            // GIVEN
            when(courseRepository.findById("invalid-id")).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> courseService.getCourseEntityById("invalid-id"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("publishCourse Tests")
    class PublishCourseTests {

        @Test
        @DisplayName("Should publish course successfully")
        void publishCourse_Success() {
            // GIVEN
            course.setIsPublished(false);
            when(courseRepository.findById("course-123")).thenReturn(Optional.of(course));
            when(courseRepository.save(any(CourseEntity.class))).thenReturn(course);

            // WHEN
            courseService.publishCourse("course-123");

            // THEN
            ArgumentCaptor<CourseEntity> captor = ArgumentCaptor.forClass(CourseEntity.class);
            verify(courseRepository).save(captor.capture());
            
            CourseEntity savedCourse = captor.getValue();
            assertThat(savedCourse.getIsPublished()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when course not found")
        void publishCourse_CourseNotFound_ThrowException() {
            // GIVEN
            when(courseRepository.findById("invalid-id")).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> courseService.publishCourse("invalid-id"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_NOT_FOUND);

            verify(courseRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("unpublishCourse Tests")
    class UnpublishCourseTests {

        @Test
        @DisplayName("Should unpublish course successfully")
        void unpublishCourse_Success() {
            // GIVEN
            course.setIsPublished(true);
            when(courseRepository.findById("course-123")).thenReturn(Optional.of(course));
            when(courseRepository.save(any(CourseEntity.class))).thenReturn(course);

            // WHEN
            courseService.unpublishCourse("course-123");

            // THEN
            ArgumentCaptor<CourseEntity> captor = ArgumentCaptor.forClass(CourseEntity.class);
            verify(courseRepository).save(captor.capture());
            
            CourseEntity savedCourse = captor.getValue();
            assertThat(savedCourse.getIsPublished()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when course not found")
        void unpublishCourse_CourseNotFound_ThrowException() {
            // GIVEN
            when(courseRepository.findById("invalid-id")).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> courseService.unpublishCourse("invalid-id"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_NOT_FOUND);

            verify(courseRepository, never()).save(any());
        }
    }
}
