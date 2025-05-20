package com.fooddelivery.integration;

import com.fooddelivery.ordermanagement.domain.Order;
import com.fooddelivery.payment.domain.Money;
import com.fooddelivery.payment.domain.Payment;
import com.fooddelivery.payment.service.PaymentService;

public class PaymentIntegrationService {
    private final PaymentService paymentService;

    public PaymentIntegrationService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Initiate payment for a given order, returning the Payment object
    public Payment initiatePaymentForOrder(Order order, String paymentMethod, boolean simulateSuccess) {
        Money paymentAmount = new Money(order.calculateTotal().getAmount(), order.calculateTotal().getCurrency());
        return paymentService.processPayment(order.getId(), paymentAmount, paymentMethod, simulateSuccess);
    }

    // Speichere ein Payment-Objekt (z.B. nach Status-Update)
    public void savePayment(Payment payment) {
        paymentService.save(payment);
    }

    // Finde ein Payment anhand der ID
    public Payment findById(String paymentId) {
        return paymentService.findById(paymentId);
    }
}
