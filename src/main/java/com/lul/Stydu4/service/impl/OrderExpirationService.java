package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.entity.OrderEntity;
import com.lul.Stydu4.enums.PaymentStatus;
import com.lul.Stydu4.repository.IOrderRepository;
import com.lul.Stydu4.service.IEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service tự động hủy các order PENDING quá hạn
 * 
 * Chạy mỗi 5 phút để kiểm tra và hủy các order đã quá 1 giờ
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderExpirationService {

    private final IOrderRepository orderRepository;
    private final IEmailService emailService;
    
    // Timeout: 1 giờ
    private static final int ORDER_TIMEOUT_HOURS = 1;

    /**
     * Scheduled job chạy mỗi 5 phút
     * Cron expression: 0 star-slash-5 star star star star
     */
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void expirePendingOrders() {
        log.info("Starting order expiration check...");
        
        try {
            // Tính thời gian expire: hiện tại - 1 giờ
            LocalDateTime expirationTime = LocalDateTime.now().minusHours(ORDER_TIMEOUT_HOURS);
            
            // Tìm tất cả order PENDING được tạo trước expirationTime
            List<OrderEntity> expiredOrders = orderRepository
                    .findByStatusAndCreatedDateBefore(PaymentStatus.PENDING, expirationTime);
            
            if (expiredOrders.isEmpty()) {
                log.info("No pending orders to expire.");
                return;
            }
            
            int expiredCount = 0;
            
            for (OrderEntity order : expiredOrders) {
                try {
                    log.info("Expiring order {} - Created: {}, User: {}, Course: {}, Amount: {}", 
                            order.getId(), 
                            order.getCreatedDate(),
                            order.getUser().getUsername(),
                            order.getCourse().getTitle(),
                            order.getAmount());
                    
                    // Chuyển status sang FAILED
                    order.setStatus(PaymentStatus.FAILED);
                    order = orderRepository.save(order);
                    expiredCount++;
                    
                    // Send cancellation email
                    try {
                        emailService.sendOrderCancellationEmail(order);
                    } catch (Exception emailEx) {
                        log.error("Failed to send cancellation email for order {}: {}", 
                                order.getId(), emailEx.getMessage());
                    }
                    
                } catch (Exception e) {
                    log.error("Error expiring order {}: {}", order.getId(), e.getMessage());
                    // Continue with other orders
                }
            }
            
            log.warn("Expired {} out of {} pending orders (older than {} hour)", 
                    expiredCount, expiredOrders.size(), ORDER_TIMEOUT_HOURS);
            
        } catch (Exception e) {
            log.error("Error during order expiration check: {}", e.getMessage(), e);
        }
    }

    /**
     * Manual method để expire một order cụ thể
     * Có thể gọi từ webhook hoặc manual trigger
     */
    @Transactional
    public boolean expireOrder(String orderId) {
        try {
            OrderEntity order = orderRepository.findById(orderId).orElse(null);
            
            if (order == null) {
                log.warn("Order {} not found", orderId);
                return false;
            }
            
            if (order.getStatus() != PaymentStatus.PENDING) {
                log.info("Order {} is not pending (status: {}), skipping", orderId, order.getStatus());
                return false;
            }
            
            log.info("Manually expiring order {}", orderId);
            order.setStatus(PaymentStatus.FAILED);
            orderRepository.save(order);
            
            return true;
            
        } catch (Exception e) {
            log.error("Error expiring order {}: {}", orderId, e.getMessage());
            return false;
        }
    }

    /**
     * Kiểm tra xem một order có hết hạn chưa
     */
    public boolean isOrderExpired(OrderEntity order) {
        if (order.getStatus() != PaymentStatus.PENDING) {
            return false;
        }
        
        LocalDateTime expirationTime = LocalDateTime.now().minusHours(ORDER_TIMEOUT_HOURS);
        return order.getCreatedDate().isBefore(expirationTime);
    }
}
