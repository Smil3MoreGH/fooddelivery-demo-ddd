package com.fooddelivery.payment.domain;

import com.fooddelivery.ordermanagement.domain.Money;

import java.util.UUID;

// Domain Service in Payment Context
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment processPayment(String orderId, Money amount, String paymentMethod) {
        String paymentId = UUID.randomUUID().toString();
        Payment payment = new Payment(paymentId, orderId, amount, paymentMethod);

        // Simulate payment processing
        boolean isSuccessful = Math.random() > 0.1; // 90% success rate

        if (isSuccessful) {
            String transactionRef = "TX-" + UUID.randomUUID().toString().substring(0, 8);
            payment.markAsCompleted(transactionRef);
        } else {
            payment.markAsFailed();
        }

        paymentRepository.save(payment);
        return payment;
    }
}