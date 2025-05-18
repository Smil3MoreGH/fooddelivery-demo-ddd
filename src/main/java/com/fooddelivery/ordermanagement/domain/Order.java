package com.fooddelivery.ordermanagement.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Aggregate Root - Order is the main entity that enforces invariants
public class Order {
    private final String id;
    private final String customerId;
    private final String restaurantId;
    private final List<OrderItem> items;
    private final Address deliveryAddress;
    private OrderStatus status;
    private boolean isPaid;
    private LocalDateTime createdAt;
    private List<DomainEvent> domainEvents;

    public Order(String id, String customerId, String restaurantId, Address deliveryAddress) {
        // Validate invariants
        if (id == null || id.trim().isEmpty())
            throw new IllegalArgumentException("Order ID cannot be empty");
        if (customerId == null || customerId.trim().isEmpty())
            throw new IllegalArgumentException("Customer ID cannot be empty");
        if (restaurantId == null || restaurantId.trim().isEmpty())
            throw new IllegalArgumentException("Restaurant ID cannot be empty");
        if (deliveryAddress == null)
            throw new IllegalArgumentException("Delivery address cannot be null");

        this.id = id;
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.deliveryAddress = deliveryAddress;
        this.items = new ArrayList<>();
        this.status = OrderStatus.CREATED;
        this.isPaid = false;
        this.createdAt = LocalDateTime.now();
        this.domainEvents = new ArrayList<>();

        // Register domain event when order is created
        this.domainEvents.add(new OrderPlacedEvent(id, restaurantId));
    }

    // Aggregate methods to enforce invariants
    public void addItem(OrderItem item) {
        if (status != OrderStatus.CREATED)
            throw new IllegalStateException("Cannot add items to an order that's already confirmed");

        items.add(item);
    }

    public void confirm() {
        if (items.isEmpty())
            throw new IllegalStateException("Cannot confirm an order without items");

        status = OrderStatus.CONFIRMED;
    }

    public void markAsPaid() {
        isPaid = true;
    }

    public void startPreparation() {
        if (!isPaid)
            throw new IllegalStateException("Cannot prepare an order that hasn't been paid");
        if (status != OrderStatus.CONFIRMED)
            throw new IllegalStateException("Order must be confirmed before preparation");

        status = OrderStatus.PREPARING;
    }

    public void readyForDelivery() {
        if (status != OrderStatus.PREPARING)
            throw new IllegalStateException("Order must be in preparation before being ready");

        status = OrderStatus.READY_FOR_DELIVERY;
    }

    public void startDelivery() {
        if (status != OrderStatus.READY_FOR_DELIVERY)
            throw new IllegalStateException("Order must be ready before delivery");

        status = OrderStatus.OUT_FOR_DELIVERY;
    }

    public void complete() {
        if (status != OrderStatus.OUT_FOR_DELIVERY)
            throw new IllegalStateException("Order must be out for delivery before completion");

        status = OrderStatus.DELIVERED;
    }

    public void cancel() {
        if (status == OrderStatus.OUT_FOR_DELIVERY || status == OrderStatus.DELIVERED)
            throw new IllegalStateException("Cannot cancel an order that's already out for delivery or delivered");

        status = OrderStatus.CANCELLED;
    }

    // Calculate total order value
    public Money calculateTotal() {
        if (items.isEmpty()) return new Money(0, "EUR");

        Money total = new Money(0, items.get(0).getPrice().getCurrency());
        for (OrderItem item : items) {
            total = total.add(item.getTotalPrice());
        }
        return total;
    }

    // Getters
    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getRestaurantId() { return restaurantId; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); } // Immutable view
    public Address getDeliveryAddress() { return deliveryAddress; }
    public OrderStatus getStatus() { return status; }
    public boolean isPaid() { return isPaid; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Domain Events handling
    public List<DomainEvent> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }

    public void clearEvents() {
        domainEvents.clear();
    }
}
