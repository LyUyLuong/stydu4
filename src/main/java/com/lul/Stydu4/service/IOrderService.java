package com.lul.Stydu4.service;

import com.lul.Stydu4.dto.response.Order.OrderResponse;

import java.util.List;

public interface IOrderService {
    List<OrderResponse> getUserOrders(String userId);
}
