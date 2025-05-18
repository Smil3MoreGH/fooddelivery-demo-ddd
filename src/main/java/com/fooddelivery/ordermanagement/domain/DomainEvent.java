package com.fooddelivery.ordermanagement.domain;

import java.time.LocalDateTime;
import java.util.UUID;

// Domain Events - when important things happen
public interface DomainEvent {
    String getEventId();
    LocalDateTime getOccurredOn();
}