package com.lul.Stydu4.controller;

import com.lul.Stydu4.dto.request.Course.CourseCreationRequest;
import com.lul.Stydu4.dto.request.Course.PaymentCaptureRequest;
import com.lul.Stydu4.dto.response.ApiResponse;
import com.lul.Stydu4.dto.response.Course.CourseResponse;
import com.lul.Stydu4.dto.response.Course.EnrollmentResponse;
import com.lul.Stydu4.dto.response.Course.PaymentResponse;
import com.lul.Stydu4.entity.CourseEntity;
import com.lul.Stydu4.entity.UserEntity;
import com.lul.Stydu4.repository.IUserRepository;
import com.lul.Stydu4.service.ICourseService;
import com.lul.Stydu4.service.IEnrollmentService;
import com.lul.Stydu4.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
@Slf4j
public class CourseController {

    private final ICourseService courseService;
    private final IPaymentService paymentService;
    private final IEnrollmentService enrollmentService;
    private final IUserRepository userRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @RequestBody CourseCreationRequest request) {
        
        CourseResponse response = courseService.createCourse(request);
        return ResponseEntity.ok(ApiResponse.<CourseResponse>builder()
                .result(response)
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getAllCourses() {
        List<CourseResponse> courses = courseService.getAllPublishedCourses();
        return ResponseEntity.ok(ApiResponse.<List<CourseResponse>>builder()
                .result(courses)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourse(@PathVariable String id) {
        CourseResponse course = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.<CourseResponse>builder()
                .result(course)
                .build());
    }

    @PostMapping("/{id}/purchase")
    public ResponseEntity<ApiResponse<PaymentResponse>> purchaseCourse(@PathVariable String id) {
        try {
            UserEntity user = getCurrentUser();
            CourseEntity course = courseService.getCourseEntityById(id);
            
            PaymentResponse payment = paymentService.createPayment(user, course);
            
            return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder()
                    .result(payment)
                    .build());
        } catch (Exception e) {
            log.error("Error purchasing course: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/payment/capture")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> capturePayment(
            @RequestBody PaymentCaptureRequest request) {
        try {
            PaymentResponse payment = paymentService.capturePayment(request.getSessionId());
            
            // Tạo enrollment sau khi thanh toán thành công
            if ("COMPLETED".equals(payment.getStatus())) {
                UserEntity user = getCurrentUser();
                // Lấy course từ orderId trong database
                CourseEntity course = paymentService.getCourseFromOrder(payment.getOrderId());
                EnrollmentResponse enrollment = enrollmentService.enrollUser(user, course);
                
                return ResponseEntity.ok(ApiResponse.<EnrollmentResponse>builder()
                        .result(enrollment)
                        .build());
            }
            
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error capturing payment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/payment/success")
    public ResponseEntity<ApiResponse<String>> paymentSuccess(
            @RequestParam("session_id") String sessionId) {
        try {
            // Frontend sẽ gọi endpoint /payment/capture với sessionId này
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .result("Payment initiated. Please complete the enrollment.")
                    .build());
        } catch (Exception e) {
            log.error("Error in payment success: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my-courses")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getMyCourses() {
        UserEntity user = getCurrentUser();
        List<EnrollmentResponse> enrollments = enrollmentService.getUserEnrollments(user.getId());
        
        return ResponseEntity.ok(ApiResponse.<List<EnrollmentResponse>>builder()
                .result(enrollments)
                .build());
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<String>> publishCourse(@PathVariable String id) {
        courseService.publishCourse(id);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .result("Course published")
                .build());
    }

    @PutMapping("/{id}/unpublish")
    public ResponseEntity<ApiResponse<String>> unpublishCourse(@PathVariable String id) {
        courseService.unpublishCourse(id);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .result("Course unpublished")
                .build());
    }

    private UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
