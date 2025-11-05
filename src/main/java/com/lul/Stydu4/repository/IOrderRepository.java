package com.lul.Stydu4.repository;

import com.lul.Stydu4.entity.OrderEntity;
import com.lul.Stydu4.enums.PaymentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IOrderRepository extends JpaRepository<OrderEntity, String> {
    
    @EntityGraph(attributePaths = {"course"})
    List<OrderEntity> findByUserId(String userId);
    
    List<OrderEntity> findByUserIdAndStatus(String userId, PaymentStatus status);
    
    Optional<OrderEntity> findByStripeSessionId(String stripeSessionId);
    
    Optional<OrderEntity> findByStripePaymentIntentId(String stripePaymentIntentId);
    
    @Query("SELECT SUM(o.amount) FROM OrderEntity o WHERE o.status = :status")
    BigDecimal calculateTotalRevenue(@Param("status") PaymentStatus status);
    
    @Query("SELECT SUM(o.amount) FROM OrderEntity o WHERE o.status = :status AND o.createdDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueByPeriod(@Param("status") PaymentStatus status, 
                                        @Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o.course.id as courseId, o.course.title as courseName, " +
           "SUM(o.amount) as revenue, COUNT(o) as totalEnrollments, o.course.price as price " +
           "FROM OrderEntity o WHERE o.status = :status " +
           "AND o.createdDate BETWEEN :startDate AND :endDate " +
           "GROUP BY o.course.id, o.course.title, o.course.price " +
           "ORDER BY revenue DESC")
    List<Object[]> findTopCoursesByRevenue(@Param("status") PaymentStatus status,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
    
    // For order expiration service
    @Query("SELECT o FROM OrderEntity o WHERE o.status = :status AND o.createdDate < :beforeTime")
    List<OrderEntity> findByStatusAndCreatedDateBefore(@Param("status") PaymentStatus status,
                                                        @Param("beforeTime") LocalDateTime beforeTime);
    
    List<OrderEntity> findByStatus(PaymentStatus status);
}
