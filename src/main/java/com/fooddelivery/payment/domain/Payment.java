package com.fooddelivery.payment.domain;

import java.time.LocalDateTime;

// Aggregate Root: Payment (Zahlung)
public class Payment {
    private final String id;                                 // Payment-ID
    private final String orderId;                            // Zugehörige Order
    private final com.fooddelivery.payment.domain.Money amount; // Betrag
    private PaymentStatus status;                            // Status der Zahlung
    private String paymentMethod;                            // Zahlungsmethode (z.B. PAYPAL)
    private String transactionReference;                     // Transaktionsreferenz
    private LocalDateTime processedAt;                       // Zeitstempel Verarbeitung

    public Payment(String id, String orderId, com.fooddelivery.payment.domain.Money amount, String paymentMethod) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = PaymentStatus.PENDING;
    }

    // Als abgeschlossen markieren (nur von PENDING)
    public void markAsCompleted(String transactionReference) {
        if (status != PaymentStatus.PENDING)
            throw new IllegalStateException("Only pending payments can be completed");

        this.transactionReference = transactionReference;
        this.status = PaymentStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
    }

    // Als fehlgeschlagen markieren (nur von PENDING)
    public void markAsFailed() {
        if (status != PaymentStatus.PENDING)
            throw new IllegalStateException("Only pending payments can be marked as failed");

        this.status = PaymentStatus.FAILED;
        this.processedAt = LocalDateTime.now();
    }

    // Rückerstattung (nur von COMPLETED)
    public void refund() {
        if (status != PaymentStatus.COMPLETED)
            throw new IllegalStateException("Only completed payments can be refunded");

        this.status = PaymentStatus.REFUNDED;
    }

    // Status manuell setzen
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    // Getter
    public String getId() { return id; }
    public String getOrderId() { return orderId; }
    public com.fooddelivery.payment.domain.Money getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getTransactionReference() { return transactionReference; }
    public LocalDateTime getProcessedAt() { return processedAt; }
}
