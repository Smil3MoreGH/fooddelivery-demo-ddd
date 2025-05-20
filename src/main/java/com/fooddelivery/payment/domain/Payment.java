package com.fooddelivery.payment.domain;

import java.time.LocalDateTime;

// Payment is the Aggregate Root in this context

public class Payment {
    private final String id;
    private final String orderId;
    private final com.fooddelivery.payment.domain.Money amount;
    private PaymentStatus status;
    private String paymentMethod;
    private String transactionReference;
    private LocalDateTime processedAt;
    public Payment(String id, String orderId, com.fooddelivery.payment.domain.Money amount, String paymentMethod) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = PaymentStatus.PENDING;
    }

    public void markAsCompleted(String transactionReference) {
        if (status != PaymentStatus.PENDING)
            throw new IllegalStateException("Only pending payments can be completed");

        this.transactionReference = transactionReference;
        this.status = PaymentStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        if (status != PaymentStatus.PENDING)
            throw new IllegalStateException("Only pending payments can be marked as failed");

        this.status = PaymentStatus.FAILED;
        this.processedAt = LocalDateTime.now();
    }

    public void refund() {
        if (status != PaymentStatus.COMPLETED)
            throw new IllegalStateException("Only completed payments can be refunded");

        this.status = PaymentStatus.REFUNDED;
    }
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
    public String getId() { return id; }
    public String getOrderId() { return orderId; }
    public com.fooddelivery.payment.domain.Money getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getTransactionReference() { return transactionReference; }
    public LocalDateTime getProcessedAt() { return processedAt; }
}
