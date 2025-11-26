package com.lul.Stydu4.service;

import com.lul.Stydu4.dto.request.Lecture.LectureCreationRequest;
import com.lul.Stydu4.dto.request.Lecture.LectureReorderRequest;
import com.lul.Stydu4.dto.request.Lecture.LectureUpdateRequest;
import com.lul.Stydu4.dto.response.Lecture.LectureResponse;

import java.util.List;

public interface ILectureService {

    /**
     * Create a new lecture for a course
     */
    LectureResponse createLecture(String courseId, LectureCreationRequest request);

    /**
     * Update an existing lecture
     */
    LectureResponse updateLecture(String lectureId, LectureUpdateRequest request);

    /**
     * Delete a lecture
     */
    void deleteLecture(String lectureId);

    /**
     * Get a single lecture by ID
     */
    LectureResponse getLectureById(String lectureId);

    /**
     * Get all lectures for a course (admin view - includes unpublished)
     */
    List<LectureResponse> getAllLecturesByCourseId(String courseId);

    /**
     * Get only published lectures for a course (user view)
     */
    List<LectureResponse> getPublishedLecturesByCourseId(String courseId);

    /**
     * Reorder lectures for a course
     */
    List<LectureResponse> reorderLectures(String courseId, LectureReorderRequest request);

    /**
     * Publish a lecture
     */
    LectureResponse publishLecture(String lectureId);

    /**
     * Unpublish a lecture
     */
    LectureResponse unpublishLecture(String lectureId);
}
