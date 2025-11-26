package com.lul.Stydu4.service;

import com.lul.Stydu4.entity.OrderEntity;

import java.util.concurrent.CompletableFuture;

public interface IEmailService {
    /**
     * Gửi email xác nhận thanh toán thành công (synchronous)
     * @param order Order đã thanh toán
     */
    void sendPaymentConfirmationEmail(OrderEntity order);
    
    /**
     * Gửi email xác nhận thanh toán thành công (asynchronous)
     * @param order Order đã thanh toán
     * @return CompletableFuture<Void> for async handling
     */
    CompletableFuture<Void> sendPaymentConfirmationEmailAsync(OrderEntity order);
    
    /**
     * Gửi email khi order bị hủy hoặc hết hạn (synchronous)
     * @param order Order bị hủy
     */
    void sendOrderCancellationEmail(OrderEntity order);
    
    /**
     * Gửi email khi order bị hủy hoặc hết hạn (asynchronous)
     * @param order Order bị hủy
     * @return CompletableFuture<Void> for async handling
     */
    CompletableFuture<Void> sendOrderCancellationEmailAsync(OrderEntity order);
    
    /**
     * Gửi email chào mừng enrollment vào khóa học (synchronous)
     * @param userEmail Email người dùng
     * @param courseName Tên khóa học
     */
    void sendEnrollmentWelcomeEmail(String userEmail, String courseName);
    
    /**
     * Gửi email chào mừng enrollment vào khóa học (asynchronous)
     * @param userEmail Email người dùng
     * @param courseName Tên khóa học
     * @return CompletableFuture<Void> for async handling
     */
    CompletableFuture<Void> sendEnrollmentWelcomeEmailAsync(String userEmail, String courseName);
}
