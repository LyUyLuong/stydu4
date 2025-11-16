package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.entity.OrderEntity;
import com.lul.Stydu4.service.IEmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Email Service Implementation
 * 
 * Gửi email thông báo cho user về:
 * - Thanh toán thành công
 * - Đơn hàng bị hủy/hết hạn
 * - Chào mừng enrollment vào khóa học
 * 
 * Sử dụng JavaMailSender với SMTP (Gmail)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;
    
    @Value("${email.from}")
    private String emailFrom;
    
    @Value("${email.enabled:true}")
    private boolean emailEnabled;

    @Override
    public void sendPaymentConfirmationEmail(OrderEntity order) {
        if (!emailEnabled) {
            log.info("Email disabled - skipping payment confirmation email for order: {}", order.getId());
            return;
        }
        
        try {
            String userEmail = order.getUser().getEmail();
            String courseName = order.getCourse().getTitle();
            String orderId = order.getId();
            
            log.info("Sending payment confirmation email to: {}", userEmail);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(emailFrom);
            helper.setTo(userEmail);
            helper.setSubject("✅ Payment Successful - " + courseName);
            helper.setText(buildPaymentConfirmationEmailHtml(order), true);
            
            mailSender.send(message);
            
            log.info("Successfully sent payment confirmation email for order: {}", orderId);
            
        } catch (Exception e) {
            log.error("Failed to send payment confirmation email for order {}: {}", 
                    order.getId(), e.getMessage(), e);
        }
    }

    @Override
    public void sendOrderCancellationEmail(OrderEntity order) {
        if (!emailEnabled) {
            log.info("Email disabled - skipping cancellation email for order: {}", order.getId());
            return;
        }
        
        try {
            String userEmail = order.getUser().getEmail();
            String courseName = order.getCourse().getTitle();
            String orderId = order.getId();
            
            log.info("Sending order cancellation email to: {}", userEmail);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(emailFrom);
            helper.setTo(userEmail);
            helper.setSubject("❌ Order Cancelled - " + courseName);
            helper.setText(buildCancellationEmailHtml(order), true);
            
            mailSender.send(message);
            
            log.info("Successfully sent cancellation email for order: {}", orderId);
            
        } catch (Exception e) {
            log.error("Failed to send cancellation email for order {}: {}", 
                    order.getId(), e.getMessage(), e);
        }
    }

    @Override
    public void sendEnrollmentWelcomeEmail(String userEmail, String courseName) {
        if (!emailEnabled) {
            log.info("Email disabled - skipping welcome email for course: {}", courseName);
            return;
        }
        
        try {
            log.info("Sending enrollment welcome email to: {}", userEmail);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(emailFrom);
            helper.setTo(userEmail);
            helper.setSubject("🎉 Welcome to " + courseName);
            helper.setText(buildWelcomeEmailHtml(courseName), true);
            
            mailSender.send(message);
            
            log.info("Successfully sent welcome email for course: {}", courseName);
            
        } catch (Exception e) {
            log.error("Failed to send welcome email: {}", e.getMessage(), e);
        }
    }
    
    // ============================================
    // ASYNC EMAIL METHODS (Non-blocking)
    // ============================================
    
    /**
     * Send payment confirmation email asynchronously
     * Uses dedicated email thread pool to avoid blocking main transaction
     */
    @Override
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendPaymentConfirmationEmailAsync(OrderEntity order) {
        log.debug("Async: Sending payment confirmation email for order: {}", order.getId());
        try {
            sendPaymentConfirmationEmail(order);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Async: Failed to send payment confirmation email", e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Send order cancellation email asynchronously
     */
    @Override
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendOrderCancellationEmailAsync(OrderEntity order) {
        log.debug("Async: Sending order cancellation email for order: {}", order.getId());
        try {
            sendOrderCancellationEmail(order);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Async: Failed to send cancellation email", e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Send enrollment welcome email asynchronously
     */
    @Override
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendEnrollmentWelcomeEmailAsync(String userEmail, String courseName) {
        log.debug("Async: Sending welcome email to: {}", userEmail);
        try {
            sendEnrollmentWelcomeEmail(userEmail, courseName);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Async: Failed to send welcome email", e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Build HTML email for payment confirmation
     */
    private String buildPaymentConfirmationEmailHtml(OrderEntity order) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background: #f9f9f9; }
                    .footer { padding: 20px; text-align: center; color: #777; font-size: 12px; }
                    .button { display: inline-block; padding: 12px 24px; background: #4CAF50; 
                             color: white; text-decoration: none; border-radius: 4px; margin: 10px 0; }
                    .info-box { background: white; padding: 15px; margin: 10px 0; border-left: 4px solid #4CAF50; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ Payment Successful!</h1>
                    </div>
                    <div class="content">
                        <p>Hello <strong>%s</strong>,</p>
                        <p>Thank you for your purchase! Your payment has been successfully processed.</p>
                        
                        <div class="info-box">
                            <h3>Order Details:</h3>
                            <p><strong>Order ID:</strong> %s</p>
                            <p><strong>Course:</strong> %s</p>
                            <p><strong>Amount:</strong> $%s</p>
                        </div>
                        
                        <p>You now have full access to the course materials.</p>
                        
                        <a href="%s/courses/%s" class="button">Access Your Course</a>
                        
                        <p>If you have any questions, please don't hesitate to contact us.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2025 Stydu4. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            order.getUser().getUsername(),
            order.getId(),
            order.getCourse().getTitle(),
            order.getAmount(),
            frontendUrl,
            order.getCourse().getId()
        );
    }
    
    /**
     * Build HTML email for order cancellation
     */
    private String buildCancellationEmailHtml(OrderEntity order) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #f44336; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background: #f9f9f9; }
                    .footer { padding: 20px; text-align: center; color: #777; font-size: 12px; }
                    .button { display: inline-block; padding: 12px 24px; background: #2196F3; 
                             color: white; text-decoration: none; border-radius: 4px; margin: 10px 0; }
                    .info-box { background: white; padding: 15px; margin: 10px 0; border-left: 4px solid #f44336; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>❌ Order Cancelled</h1>
                    </div>
                    <div class="content">
                        <p>Hello <strong>%s</strong>,</p>
                        <p>Your order has been cancelled because the payment session expired.</p>
                        
                        <div class="info-box">
                            <h3>Cancelled Order:</h3>
                            <p><strong>Order ID:</strong> %s</p>
                            <p><strong>Course:</strong> %s</p>
                            <p><strong>Reason:</strong> Payment session expired or cancelled</p>
                        </div>
                        
                        <p>Don't worry! You can purchase the course again at any time.</p>
                        
                        <a href="%s/courses" class="button">Browse Courses</a>
                        
                        <p>If you need assistance, please contact our support team.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2025 Stydu4. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            order.getUser().getUsername(),
            order.getId(),
            order.getCourse().getTitle(),
            frontendUrl
        );
    }
    
    /**
     * Build HTML email for enrollment welcome
     */
    private String buildWelcomeEmailHtml(String courseName) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #2196F3; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background: #f9f9f9; }
                    .footer { padding: 20px; text-align: center; color: #777; font-size: 12px; }
                    .button { display: inline-block; padding: 12px 24px; background: #2196F3; 
                             color: white; text-decoration: none; border-radius: 4px; margin: 10px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Welcome to %s!</h1>
                    </div>
                    <div class="content">
                        <p>Congratulations on enrolling in <strong>%s</strong>!</p>
                        <p>You're now ready to start your learning journey.</p>
                        
                        <p><strong>Next Steps:</strong></p>
                        <ul>
                            <li>Access your course materials</li>
                            <li>Complete the lessons at your own pace</li>
                            <li>Track your progress</li>
                            <li>Take practice tests</li>
                        </ul>
                        
                        <a href="%s/courses" class="button">Browse Courses</a>
                        
                        <p>We're here to support you every step of the way!</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2025 Stydu4. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            courseName,
            courseName,
            frontendUrl
        );
    }
}
