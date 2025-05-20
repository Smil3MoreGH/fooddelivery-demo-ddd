package com.fooddelivery.payment.service;

import com.fooddelivery.payment.domain.Money;
import com.fooddelivery.payment.domain.Payment;
import com.fooddelivery.payment.domain.PaymentRepository;

import java.util.UUID;

public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment processPayment(String orderId, Money amount, String paymentMethod, boolean simulateSuccess) {
        String paymentId = UUID.randomUUID().toString();
        Payment payment = new Payment(paymentId, orderId, amount, paymentMethod);

        // Use simulation flag to force result
        boolean isSuccessful = simulateSuccess;

        if (isSuccessful) {
            String transactionRef = "TX-" + UUID.randomUUID().toString().substring(0, 8);
            payment.markAsCompleted(transactionRef);
        } else {
            payment.markAsFailed();
        }

        paymentRepository.save(payment);
        return payment;
    }

    public PaymentRepository getRepository() {
        return paymentRepository;
    }
}
