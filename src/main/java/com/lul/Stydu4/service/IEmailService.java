package com.lul.Stydu4.service;

import com.lul.Stydu4.entity.OrderEntity;

public interface IEmailService {
    /**
     * Gửi email xác nhận thanh toán thành công
     * @param order Order đã thanh toán
     */
    void sendPaymentConfirmationEmail(OrderEntity order);
    
    /**
     * Gửi email khi order bị hủy hoặc hết hạn
     * @param order Order bị hủy
     */
    void sendOrderCancellationEmail(OrderEntity order);
    
    /**
     * Gửi email chào mừng enrollment vào khóa học
     * @param userEmail Email người dùng
     * @param courseName Tên khóa học
     */
    void sendEnrollmentWelcomeEmail(String userEmail, String courseName);
}
