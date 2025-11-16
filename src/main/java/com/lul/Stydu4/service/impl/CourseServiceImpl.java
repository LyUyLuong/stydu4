package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.request.Course.CourseCreationRequest;
import com.lul.Stydu4.dto.response.Course.CourseResponse;
import com.lul.Stydu4.entity.CourseEntity;
import com.lul.Stydu4.entity.TestEntity;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.repository.ICourseRepository;
import com.lul.Stydu4.repository.ITestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements com.lul.Stydu4.service.ICourseService {

    private final ICourseRepository courseRepository;
    private final ITestRepository testRepository;

    @Override
    @Transactional
    public CourseResponse createCourse(CourseCreationRequest request) {
        List<TestEntity> tests = new ArrayList<>();
        
        if (request.getTestIds() != null && !request.getTestIds().isEmpty()) {
            tests = testRepository.findAllById(request.getTestIds());
        }

        CourseEntity course = CourseEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .duration(request.getDuration())
                .tests(tests)
                .build();

        course = courseRepository.save(course);
        log.info("Created course: {}", course.getId());

        return mapToResponse(course);
    }

    @Override
    public CourseResponse getCourseById(String id) {
        // ✅ Use optimized query to prevent N+1 problem when accessing tests
        CourseEntity course = courseRepository.findByIdWithTests(id)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        return mapToResponse(course);
    }

    @Override
    public List<CourseResponse> getAllPublishedCourses() {
        return courseRepository.findByIsPublished(true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CourseEntity getCourseEntityById(String id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
    }

    @Override
    @Transactional
    public void publishCourse(String id) {
        CourseEntity course = getCourseEntityById(id);
        course.setIsPublished(true);
        courseRepository.save(course);
        log.info("Published course: {}", id);
    }

    @Override
    @Transactional
    public void unpublishCourse(String id) {
        CourseEntity course = getCourseEntityById(id);
        course.setIsPublished(false);
        courseRepository.save(course);
        log.info("Unpublished course: {}", id);
    }

    private CourseResponse mapToResponse(CourseEntity course) {
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .imageUrl(course.getImageUrl())
                .duration(course.getDuration())
                .isPublished(course.getIsPublished())
                .testCount(course.getTests() != null ? course.getTests().size() : 0)
                .studentCount(course.getEnrollments() != null ? course.getEnrollments().size() : 0)
                .build();
    }
}