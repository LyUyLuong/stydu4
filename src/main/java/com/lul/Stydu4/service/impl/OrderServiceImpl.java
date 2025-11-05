package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.response.Order.OrderResponse;
import com.lul.Stydu4.entity.OrderEntity;
import com.lul.Stydu4.repository.IOrderRepository;
import com.lul.Stydu4.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements IOrderService {

    private final IOrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(String userId) {
        List<OrderEntity> orders = orderRepository.findByUserId(userId);
        
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String orderId, String userId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        // Verify order belongs to user
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied: Order does not belong to user");
        }
        
        return mapToResponse(order);
    }

    private OrderResponse mapToResponse(OrderEntity order) {
        return OrderResponse.builder()
                .id(order.getId())
                .courseId(order.getCourse().getId())
                .courseTitle(order.getCourse().getTitle())
                .courseDescription(order.getCourse().getDescription())
                .amount(order.getAmount())
                .status(order.getStatus())
                .stripeSessionId(order.getStripeSessionId())
                .createdAt(order.getCreatedDate())
                .updatedAt(order.getModifiedDate())
                .build();
    }
}
