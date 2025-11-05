package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.entity.OrderEntity;
import com.lul.Stydu4.service.IEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Email Service Implementation
 * 
 * HIỆN TẠI: Chỉ log email ra console (chưa gửi thật)
 * 
 * ĐỂ KÍCH HOẠT GỬI EMAIL THẬT:
 * 
 * 1. Thêm dependency vào pom.xml:
 *    <dependency>
 *        <groupId>org.springframework.boot</groupId>
 *        <artifactId>spring-boot-starter-mail</artifactId>
 *    </dependency>
 * 
 * 2. Cấu hình SMTP trong application.yaml (đã có template comment)
 * 
 * 3. Inject JavaMailSender:
 *    private final JavaMailSender mailSender;
 * 
 * 4. Uncomment code gửi email thật trong các method
 * 
 * HOẶC dùng SendGrid/Mailgun/AWS SES API
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements IEmailService {

    @Value("${app.frontend.url}")
    private String frontendUrl;
    
    // TODO: Inject when enabling real email
    // private final JavaMailSender mailSender;

    @Override
    public void sendPaymentConfirmationEmail(OrderEntity order) {
        try {
            String userEmail = order.getUser().getEmail();
            String courseName = order.getCourse().getTitle();
            String orderId = order.getId();
            String amount = order.getAmount().toString();
            
            log.info("=================================================");
            log.info("SENDING PAYMENT CONFIRMATION EMAIL");
            log.info("=================================================");
            log.info("To: {}", userEmail);
            log.info("Subject: Payment Successful - {}", courseName);
            log.info("Order ID: {}", orderId);
            log.info("Amount: ${}", amount);
            log.info("Course: {}", courseName);
            log.info("Access your course: {}/courses/{}", frontendUrl, order.getCourse().getId());
            log.info("=================================================");
            
            // TODO: Implement actual email sending
            // Example with JavaMailSender:
            // MimeMessage message = mailSender.createMimeMessage();
            // MimeMessageHelper helper = new MimeMessageHelper(message, true);
            // helper.setTo(userEmail);
            // helper.setSubject("Payment Successful - " + courseName);
            // helper.setText(buildEmailTemplate(order), true);
            // mailSender.send(message);
            
        } catch (Exception e) {
            log.error("Failed to send payment confirmation email: {}", e.getMessage(), e);
            // Don't throw exception - email failure shouldn't break payment flow
        }
    }

    @Override
    public void sendOrderCancellationEmail(OrderEntity order) {
        try {
            String userEmail = order.getUser().getEmail();
            String courseName = order.getCourse().getTitle();
            String orderId = order.getId();
            
            log.info("=================================================");
            log.info("SENDING ORDER CANCELLATION EMAIL");
            log.info("=================================================");
            log.info("To: {}", userEmail);
            log.info("Subject: Order Cancelled - {}", courseName);
            log.info("Order ID: {}", orderId);
            log.info("Course: {}", courseName);
            log.info("Reason: Payment session expired or cancelled");
            log.info("Try again: {}/courses", frontendUrl);
            log.info("=================================================");
            
            // TODO: Implement actual email sending
            
        } catch (Exception e) {
            log.error("Failed to send cancellation email: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendEnrollmentWelcomeEmail(String userEmail, String courseName) {
        try {
            log.info("=================================================");
            log.info("SENDING ENROLLMENT WELCOME EMAIL");
            log.info("=================================================");
            log.info("To: {}", userEmail);
            log.info("Subject: Welcome to {} - Let's Start Learning!", courseName);
            log.info("Course: {}", courseName);
            log.info("Start learning: {}/my-courses", frontendUrl);
            log.info("=================================================");
            
            // TODO: Implement actual email sending
            
        } catch (Exception e) {
            log.error("Failed to send welcome email: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Build HTML email template
     * TODO: Use Thymeleaf or FreeMarker for better templating
     * TODO: Uncomment when JavaMailSender is configured
     */
    @SuppressWarnings("unused")
    private String buildEmailTemplate(OrderEntity order) {
        return String.format("""
            <html>
                <body>
                    <h2>Payment Successful!</h2>
                    <p>Thank you for your purchase.</p>
                    <p><strong>Order ID:</strong> %s</p>
                    <p><strong>Course:</strong> %s</p>
                    <p><strong>Amount:</strong> $%s</p>
                    <p><a href="%s/my-courses">Access Your Course</a></p>
                </body>
            </html>
            """, 
            order.getId(),
            order.getCourse().getTitle(),
            order.getAmount(),
            frontendUrl
        );
    }
}
