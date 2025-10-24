package com.lul.Stydu4.service;

import com.lul.Stydu4.dto.response.Course.PaymentResponse;
import com.lul.Stydu4.entity.CourseEntity;
import com.lul.Stydu4.entity.UserEntity;

public interface IPaymentService {
    
    PaymentResponse createPayment(UserEntity user, CourseEntity course) throws Exception;
    
    PaymentResponse capturePayment(String sessionId) throws Exception;
    
    CourseEntity getCourseFromOrder(String orderId) throws Exception;
}
