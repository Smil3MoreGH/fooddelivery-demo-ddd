package com.fooddelivery.ordermanagement.service;

import com.fooddelivery.ordermanagement.domain.Address;
import com.fooddelivery.ordermanagement.domain.Order;
import com.fooddelivery.ordermanagement.domain.OrderRepository;

import java.util.UUID;

// Domain Service - complex operations involving multiple aggregates
public class OrderService {
    public final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order createOrder(String customerId, String restaurantId, Address deliveryAddress) {
        String orderId = UUID.randomUUID().toString();
        Order order = new Order(orderId, customerId, restaurantId, deliveryAddress);
        orderRepository.save(order);
        return order;
    }

    public void processPayment(String orderId, boolean successful) {
        Order order = orderRepository.findById(orderId);
        if (order == null)
            throw new IllegalArgumentException("Order not found");

        if (successful) {
            order.markAsPaid();
            orderRepository.save(order);
        }
    }
    public OrderRepository getOrderRepository() {
        return orderRepository;
    }
}