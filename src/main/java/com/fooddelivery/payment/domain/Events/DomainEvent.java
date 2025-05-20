package com.fooddelivery.payment.domain.Events;

import java.time.LocalDateTime;
//import java.util.UUID;

// Domain Events - when important things happen
public interface DomainEvent {
    String getEventId();
    LocalDateTime getOccurredOn();
}