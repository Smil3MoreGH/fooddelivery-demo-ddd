package com.fooddelivery.ordermanagement.domain.Events;

import java.time.LocalDateTime;
import java.util.UUID;

// Domain-Event: Order wurde bestätigt
public class OrderConfirmedEvent implements DomainEvent {
    private final String eventId;
    private final String orderId;
    private final LocalDateTime occurredOn;

    public OrderConfirmedEvent(String orderId) {
        this.eventId = UUID.randomUUID().toString(); // Eindeutige Event-ID
        this.orderId = orderId;                      // Zugehörige Order-ID
        this.occurredOn = LocalDateTime.now();       // Zeitpunkt des Events
    }

    @Override
    public String getEventId() { return eventId; }
    @Override
    public LocalDateTime getOccurredOn() { return occurredOn; }
    public String getOrderId() { return orderId; }
}
