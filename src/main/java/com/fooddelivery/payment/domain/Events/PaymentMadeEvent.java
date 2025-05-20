package com.fooddelivery.payment.domain.Events;

import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentMadeEvent implements DomainEvent {
    private final String eventId;
    private final String paymentId;
    private final String orderId;
    private final double amount;
    private final String currency;
    private final String paymentMethod;
    private final LocalDateTime occurredOn;

    public PaymentMadeEvent(String paymentId, String orderId, double amount, String currency, String paymentMethod) {
        this.eventId = UUID.randomUUID().toString();
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.occurredOn = LocalDateTime.now();
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }

    public String getPaymentId() { return paymentId; }
    public String getOrderId() { return orderId; }
    public double getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getPaymentMethod() { return paymentMethod; }
}
