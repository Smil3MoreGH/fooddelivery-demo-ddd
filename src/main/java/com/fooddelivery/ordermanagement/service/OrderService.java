package com.fooddelivery.ordermanagement.service;

import com.fooddelivery.ordermanagement.domain.*;
import java.util.UUID;
import java.util.List;

public class OrderService {
    public final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order createOrder(String customerId, String restaurantId, Address deliveryAddress) {
        String orderId = UUID.randomUUID().toString();
        return new Order(orderId, customerId, restaurantId, deliveryAddress);
    }

    public void addItems(Order order, List<OrderItem> items) {
        for (OrderItem item : items) {
            order.addItem(item);
        }
    }

    public void confirmOrder(Order order) {
        order.confirm();
    }

    public Order findById(String orderId) {
        return orderRepository.findById(orderId);
    }
    public void markOrderAsPaid(Order order) {
        order.markAsPaid();
    }

    public void save(Order order) {
        orderRepository.save(order);
    }
}
