package com.lul.Stydu4.service;

import com.lul.Stydu4.dto.response.Cart.CartResponse;

import java.math.BigDecimal;
import java.util.List;

public interface ICartService {
    CartResponse addToCart(String userId, String courseId);
    List<CartResponse> getCartItems(String userId);
    void removeFromCart(String userId, String courseId);
    void clearCart(String userId);
    BigDecimal getTotalPrice(String userId);
    int getCartCount(String userId);
}
