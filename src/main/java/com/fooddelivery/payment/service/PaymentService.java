package com.fooddelivery.payment.service;

import com.fooddelivery.payment.domain.Money;
import com.fooddelivery.payment.domain.Payment;
import com.fooddelivery.payment.domain.PaymentRepository;

import java.util.UUID;

// Service für Zahlungs-Logik
public class PaymentService {
    private final PaymentRepository paymentRepository; // Repository für Speicherung

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    // Startet und verarbeitet eine Zahlung (mit Simulations-Flag)
    public Payment processPayment(String orderId, Money amount, String paymentMethod, boolean simulateSuccess) {
        String paymentId = UUID.randomUUID().toString();
        Payment payment = new Payment(paymentId, orderId, amount, paymentMethod);

        // Erfolgreiche oder fehlgeschlagene Zahlung simulieren
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

    // Speichert Payment-Objekt
    public void save(Payment payment) {
        paymentRepository.save(payment);
    }

    // Findet Payment per ID
    public Payment findById(String paymentId) {
        return paymentRepository.findById(paymentId);
    }

    // Gibt das Repository zurück (z.B. für Tests)
    public PaymentRepository getRepository() {
        return paymentRepository;
    }
}
