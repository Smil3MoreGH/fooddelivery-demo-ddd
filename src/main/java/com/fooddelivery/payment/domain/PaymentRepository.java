package com.fooddelivery.payment.domain;

public interface PaymentRepository {
    void save(Payment payment);
    Payment findById(String id);
    Payment findByOrderId(String orderId);
}