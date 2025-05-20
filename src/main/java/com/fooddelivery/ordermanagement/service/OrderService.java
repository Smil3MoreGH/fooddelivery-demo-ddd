package com.fooddelivery.ordermanagement.service;

import com.fooddelivery.ordermanagement.domain.*;
import java.util.UUID;
import java.util.List;

// Service: Geschäftslogik für Bestellungen
public class OrderService {
    public final OrderRepository orderRepository; // Zugriff auf Datenbank

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // Neue Bestellung anlegen
    public Order createOrder(String customerId, String restaurantId, Address deliveryAddress) {
        String orderId = UUID.randomUUID().toString();
        return new Order(orderId, customerId, restaurantId, deliveryAddress);
    }

    // Mehrere Items zur Bestellung hinzufügen
    public void addItems(Order order, List<OrderItem> items) {
        for (OrderItem item : items) {
            order.addItem(item);
        }
    }

    // Bestellung bestätigen
    public void confirmOrder(Order order) {
        order.confirm();
    }

    // Bestellung per ID finden
    public Order findById(String orderId) {
        return orderRepository.findById(orderId);
    }

    // Als bezahlt markieren
    public void markOrderAsPaid(Order order) {
        order.markAsPaid();
    }

    // Bestellung speichern
    public void save(Order order) {
        orderRepository.save(order);
    }
}
