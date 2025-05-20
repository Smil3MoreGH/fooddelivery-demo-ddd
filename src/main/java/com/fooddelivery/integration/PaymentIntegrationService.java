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

    // Zahlungsprozess für eine Order anstoßen, gibt Payment-Objekt zurück
    public Payment initiatePaymentForOrder(Order order, String paymentMethod, boolean simulateSuccess) {
        Money paymentAmount = new Money(order.calculateTotal().getAmount(), order.calculateTotal().getCurrency());
        return paymentService.processPayment(order.getId(), paymentAmount, paymentMethod, simulateSuccess);
    }

    // Payment speichern (z.B. nach Status-Update)
    public void savePayment(Payment payment) {
        paymentService.save(payment);
    }

    // Payment anhand der ID finden
    public Payment findById(String paymentId) {
        return paymentService.findById(paymentId);
    }
}
