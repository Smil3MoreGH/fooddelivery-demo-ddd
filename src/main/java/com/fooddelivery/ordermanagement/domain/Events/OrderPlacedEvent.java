package com.fooddelivery.ordermanagement.domain.Events;

import java.time.LocalDateTime;
import java.util.UUID;

// Domain-Event: Neue Bestellung wurde aufgegeben
public class OrderPlacedEvent implements DomainEvent {
    private final String eventId;        // Eindeutige Event-ID
    private final String orderId;        // ID der Bestellung
    private final String restaurantId;   // Restaurant, bei dem bestellt wurde
    private final LocalDateTime occurredOn; // Zeitpunkt des Events

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
