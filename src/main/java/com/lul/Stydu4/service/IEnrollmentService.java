package com.lul.Stydu4.service;

import com.lul.Stydu4.dto.response.Course.EnrollmentResponse;
import com.lul.Stydu4.entity.CourseEntity;
import com.lul.Stydu4.entity.UserEntity;

import java.util.List;

public interface IEnrollmentService {
    
    EnrollmentResponse enrollUser(UserEntity user, CourseEntity course);
    
    List<EnrollmentResponse> getUserEnrollments(String userId);
    
    boolean hasActiveEnrollment(String userId, String courseId);
    
    void checkAndExpireEnrollments();
}
