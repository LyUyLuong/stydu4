package com.lul.Stydu4.controller;

import com.lul.Stydu4.dto.request.Lecture.LectureCreationRequest;
import com.lul.Stydu4.dto.request.Lecture.LectureReorderRequest;
import com.lul.Stydu4.dto.request.Lecture.LectureUpdateRequest;
import com.lul.Stydu4.dto.response.ApiResponse;
import com.lul.Stydu4.dto.response.Lecture.LectureResponse;
import com.lul.Stydu4.service.ILectureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lectures")
@RequiredArgsConstructor
@Slf4j
public class LectureController {

    private final ILectureService lectureService;

    /**
     * Create a new lecture for a course (Admin only)
     */
    @PostMapping("/courses/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LectureResponse>> createLecture(
            @PathVariable String courseId,
            @Valid @RequestBody LectureCreationRequest request) {
        LectureResponse lecture = lectureService.createLecture(courseId, request);
        return ResponseEntity.ok(ApiResponse.<LectureResponse>builder()
                .result(lecture)
                .build());
    }

    /**
     * Update a lecture (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LectureResponse>> updateLecture(
            @PathVariable String id,
            @Valid @RequestBody LectureUpdateRequest request) {
        LectureResponse lecture = lectureService.updateLecture(id, request);
        return ResponseEntity.ok(ApiResponse.<LectureResponse>builder()
                .result(lecture)
                .build());
    }

    /**
     * Delete a lecture (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteLecture(@PathVariable String id) {
        lectureService.deleteLecture(id);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .result("Lecture deleted successfully")
                .build());
    }

    /**
     * Get a single lecture by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LectureResponse>> getLecture(@PathVariable String id) {
        LectureResponse lecture = lectureService.getLectureById(id);
        return ResponseEntity.ok(ApiResponse.<LectureResponse>builder()
                .result(lecture)
                .build());
    }

    /**
     * Get all lectures for a course - Admin view (includes unpublished)
     */
    @GetMapping("/courses/{courseId}/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<LectureResponse>>> getAllLecturesByCourse(
            @PathVariable String courseId) {
        List<LectureResponse> lectures = lectureService.getAllLecturesByCourseId(courseId);
        return ResponseEntity.ok(ApiResponse.<List<LectureResponse>>builder()
                .result(lectures)
                .build());
    }

    /**
     * Get published lectures for a course - User view
     */
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<ApiResponse<List<LectureResponse>>> getPublishedLecturesByCourse(
            @PathVariable String courseId) {
        List<LectureResponse> lectures = lectureService.getPublishedLecturesByCourseId(courseId);
        return ResponseEntity.ok(ApiResponse.<List<LectureResponse>>builder()
                .result(lectures)
                .build());
    }

    /**
     * Reorder lectures for a course (Admin only)
     */
    @PutMapping("/courses/{courseId}/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<LectureResponse>>> reorderLectures(
            @PathVariable String courseId,
            @Valid @RequestBody LectureReorderRequest request) {
        List<LectureResponse> lectures = lectureService.reorderLectures(courseId, request);
        return ResponseEntity.ok(ApiResponse.<List<LectureResponse>>builder()
                .result(lectures)
                .build());
    }

    /**
     * Publish a lecture (Admin only)
     */
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LectureResponse>> publishLecture(@PathVariable String id) {
        LectureResponse lecture = lectureService.publishLecture(id);
        return ResponseEntity.ok(ApiResponse.<LectureResponse>builder()
                .result(lecture)
                .build());
    }

    /**
     * Unpublish a lecture (Admin only)
     */
    @PutMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LectureResponse>> unpublishLecture(@PathVariable String id) {
        LectureResponse lecture = lectureService.unpublishLecture(id);
        return ResponseEntity.ok(ApiResponse.<LectureResponse>builder()
                .result(lecture)
                .build());
    }
}
