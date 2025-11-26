package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.request.Lecture.LectureCreationRequest;
import com.lul.Stydu4.dto.request.Lecture.LectureReorderRequest;
import com.lul.Stydu4.dto.request.Lecture.LectureUpdateRequest;
import com.lul.Stydu4.dto.response.Lecture.LectureResponse;
import com.lul.Stydu4.entity.CourseEntity;
import com.lul.Stydu4.entity.LectureEntity;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.repository.ICourseRepository;
import com.lul.Stydu4.repository.ILectureRepository;
import com.lul.Stydu4.service.ILectureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LectureServiceImpl implements ILectureService {

    private final ILectureRepository lectureRepository;
    private final ICourseRepository courseRepository;

    @Override
    @Transactional
    public LectureResponse createLecture(String courseId, LectureCreationRequest request) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        // Get next order index
        Integer maxOrderIndex = lectureRepository.findMaxOrderIndexByCourseId(courseId);
        Integer nextOrderIndex = maxOrderIndex + 1;

        LectureEntity lecture = LectureEntity.builder()
                .course(course)
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .content(request.getContent())
                .duration(request.getDuration())
                .orderIndex(nextOrderIndex)
                .isPublished(false)
                .build();

        lecture = lectureRepository.save(lecture);
        log.info("Created lecture: {} for course: {}", lecture.getId(), courseId);

        return mapToResponse(lecture);
    }

    @Override
    @Transactional
    public LectureResponse updateLecture(String lectureId, LectureUpdateRequest request) {
        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new AppException(ErrorCode.LECTURE_NOT_FOUND));

        if (request.getTitle() != null) {
            lecture.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            lecture.setDescription(request.getDescription());
        }
        if (request.getType() != null) {
            lecture.setType(request.getType());
        }
        if (request.getContent() != null) {
            lecture.setContent(request.getContent());
        }
        if (request.getDuration() != null) {
            lecture.setDuration(request.getDuration());
        }
        if (request.getIsPublished() != null) {
            lecture.setIsPublished(request.getIsPublished());
        }

        lecture = lectureRepository.save(lecture);
        log.info("Updated lecture: {}", lectureId);

        return mapToResponse(lecture);
    }

    @Override
    @Transactional
    public void deleteLecture(String lectureId) {
        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new AppException(ErrorCode.LECTURE_NOT_FOUND));

        String courseId = lecture.getCourse().getId();
        Integer deletedOrderIndex = lecture.getOrderIndex();

        lectureRepository.delete(lecture);
        log.info("Deleted lecture: {}", lectureId);

        // Reorder remaining lectures
        List<LectureEntity> remainingLectures = lectureRepository.findByCourseIdOrderByOrderIndex(courseId);
        for (LectureEntity l : remainingLectures) {
            if (l.getOrderIndex() > deletedOrderIndex) {
                l.setOrderIndex(l.getOrderIndex() - 1);
            }
        }
        lectureRepository.saveAll(remainingLectures);
    }

    @Override
    public LectureResponse getLectureById(String lectureId) {
        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new AppException(ErrorCode.LECTURE_NOT_FOUND));
        return mapToResponse(lecture);
    }

    @Override
    public List<LectureResponse> getAllLecturesByCourseId(String courseId) {
        List<LectureEntity> lectures = lectureRepository.findByCourseIdOrderByOrderIndex(courseId);
        return lectures.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LectureResponse> getPublishedLecturesByCourseId(String courseId) {
        List<LectureEntity> lectures = lectureRepository.findByCourseIdAndIsPublishedTrueOrderByOrderIndex(courseId);
        return lectures.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<LectureResponse> reorderLectures(String courseId, LectureReorderRequest request) {
        List<String> lectureIds = request.getLectureIds();

        // Validate all lectures belong to the course
        List<LectureEntity> lectures = lectureRepository.findAllById(lectureIds);
        boolean allBelongToCourse = lectures.stream()
                .allMatch(l -> l.getCourse().getId().equals(courseId));

        if (!allBelongToCourse) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // Update order indices
        for (int i = 0; i < lectureIds.size(); i++) {
            String lectureId = lectureIds.get(i);
            LectureEntity lecture = lectures.stream()
                    .filter(l -> l.getId().equals(lectureId))
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.LECTURE_NOT_FOUND));
            lecture.setOrderIndex(i);
        }

        lectureRepository.saveAll(lectures);
        log.info("Reordered {} lectures for course: {}", lectures.size(), courseId);

        return getAllLecturesByCourseId(courseId);
    }

    @Override
    @Transactional
    public LectureResponse publishLecture(String lectureId) {
        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new AppException(ErrorCode.LECTURE_NOT_FOUND));
        lecture.setIsPublished(true);
        lecture = lectureRepository.save(lecture);
        log.info("Published lecture: {}", lectureId);
        return mapToResponse(lecture);
    }

    @Override
    @Transactional
    public LectureResponse unpublishLecture(String lectureId) {
        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new AppException(ErrorCode.LECTURE_NOT_FOUND));
        lecture.setIsPublished(false);
        lecture = lectureRepository.save(lecture);
        log.info("Unpublished lecture: {}", lectureId);
        return mapToResponse(lecture);
    }

    private LectureResponse mapToResponse(LectureEntity lecture) {
        return LectureResponse.builder()
                .id(lecture.getId())
                .courseId(lecture.getCourse().getId())
                .courseTitle(lecture.getCourse().getTitle())
                .title(lecture.getTitle())
                .description(lecture.getDescription())
                .orderIndex(lecture.getOrderIndex())
                .type(lecture.getType())
                .content(lecture.getContent())
                .duration(lecture.getDuration())
                .isPublished(lecture.getIsPublished())
                .createdAt(lecture.getCreatedDate())
                .updatedAt(lecture.getModifiedDate())
                .build();
    }
}
