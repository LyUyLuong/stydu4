package com.lul.Stydu4.service;

import com.lul.Stydu4.dto.request.Course.CourseCreationRequest;
import com.lul.Stydu4.dto.response.Course.CourseResponse;
import com.lul.Stydu4.entity.CourseEntity;

import java.util.List;

public interface ICourseService {

    CourseResponse createCourse(CourseCreationRequest request);

    CourseResponse getCourseById(String id);

    List<CourseResponse> getAllPublishedCourses();

    List<CourseResponse> getAllCourses();

    CourseEntity getCourseEntityById(String id);

    void publishCourse(String id);

    void unpublishCourse(String id);

    CourseResponse updateCourseImage(String id, String imageUrl);

    CourseResponse updateCourse(String id, CourseCreationRequest request);
}
