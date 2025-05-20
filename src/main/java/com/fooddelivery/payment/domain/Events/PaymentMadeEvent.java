package com.fooddelivery.payment.domain.Events;

import java.time.LocalDateTime;
import java.util.UUID;

// Domain-Event: Eine Zahlung wurde durchgeführt
public class PaymentMadeEvent implements DomainEvent {
    private final String eventId;         // Eindeutige Event-ID
    private final String paymentId;       // Payment-ID
    private final String orderId;         // Zu welcher Order
    private final double amount;          // Betrag
    private final String currency;        // Währung
    private final String paymentMethod;   // Zahlungsmethode (z.B. PayPal)
    private final LocalDateTime occurredOn; // Zeitpunkt des Events

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
    public String getEventId() { return eventId; }
    @Override
    public LocalDateTime getOccurredOn() { return occurredOn; }
    public String getPaymentId() { return paymentId; }
    public String getOrderId() { return orderId; }
    public double getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getPaymentMethod() { return paymentMethod; }
}
