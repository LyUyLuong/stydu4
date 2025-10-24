package com.lul.Stydu4.repository;

import com.lul.Stydu4.entity.OrderEntity;
import com.lul.Stydu4.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IOrderRepository extends JpaRepository<OrderEntity, String> {
    
    List<OrderEntity> findByUserId(String userId);
    
    List<OrderEntity> findByUserIdAndStatus(String userId, PaymentStatus status);
    
    Optional<OrderEntity> findByStripeSessionId(String stripeSessionId);
    
    Optional<OrderEntity> findByStripePaymentIntentId(String stripePaymentIntentId);
}
