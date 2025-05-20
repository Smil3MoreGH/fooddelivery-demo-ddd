package com.fooddelivery.ordermanagement.domain.Events;

import java.time.LocalDateTime;
import java.util.UUID;

public class OrderPlacedEvent implements DomainEvent {
    private final String eventId;
    private final String orderId;
    private final String restaurantId;
    private final LocalDateTime occurredOn;

    public OrderPlacedEvent(String orderId, String restaurantId) {
        this.eventId = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.restaurantId = restaurantId;
        this.occurredOn = LocalDateTime.now();
    }

    @Override
    public String getEventId() { return eventId; }

    @Override
    public LocalDateTime getOccurredOn() { return occurredOn; }

    public String getOrderId() { return orderId; }
    public String getRestaurantId() { return restaurantId; }
}
