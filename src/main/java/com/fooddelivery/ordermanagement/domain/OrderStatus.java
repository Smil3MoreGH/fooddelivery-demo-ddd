package com.fooddelivery.ordermanagement.domain;

// Enum - Order status states
public enum OrderStatus {
    CREATED,
    CONFIRMED,
    PREPARING,
    READY_FOR_DELIVERY,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}
