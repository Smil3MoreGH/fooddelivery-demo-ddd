package com.fooddelivery.ordermanagement.domain;

import java.util.List;

// Repository interface - persistence abstraction
public interface OrderRepository {
    void save(Order order);
    Order findById(String id);
    List<Order> findByCustomerId(String customerId);
    List<Order> findByRestaurantId(String restaurantId);
}