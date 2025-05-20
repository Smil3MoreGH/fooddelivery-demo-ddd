package com.fooddelivery.ordermanagement.domain.Events;

import java.time.LocalDateTime;
import java.util.UUID;

public class OrderConfirmedEvent implements DomainEvent {
    private final String eventId;
    private final String orderId;
    private final LocalDateTime occurredOn;

    public OrderConfirmedEvent(String orderId) {
        this.eventId = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.occurredOn = LocalDateTime.now();
    }

    @Override
    public String getEventId() { return eventId; }

    @Override
    public LocalDateTime getOccurredOn() { return occurredOn; }

    public String getOrderId() { return orderId; }
}
