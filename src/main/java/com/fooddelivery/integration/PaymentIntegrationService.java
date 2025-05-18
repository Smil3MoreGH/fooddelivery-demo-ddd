// Anti-Corruption Layer translating between Order and Payment contexts
package com.fooddelivery.integration;

import com.fooddelivery.ordermanagement.domain.*;
import com.fooddelivery.ordermanagement.service.*;
import com.fooddelivery.payment.domain.*;

// This translates between different contexts that have different models
public class PaymentIntegrationService {
    private final OrderService orderService;
    private final PaymentService paymentService;

    public PaymentIntegrationService(OrderService orderService, PaymentService paymentService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    // Process payment and update order status
    public boolean initiatePaymentForOrder(String orderId) {
        // Get order from Order context
        Order order = orderService.orderRepository.findById(orderId);
        if (order == null) return false;

        // Convert Order Money (from OrderContext) to Payment Money (PaymentContext)
        com.fooddelivery.ordermanagement.domain.Money orderTotal = order.calculateTotal();
        com.fooddelivery.payment.domain.Money paymentAmount =
                new com.fooddelivery.payment.domain.Money(orderTotal.getAmount(), orderTotal.getCurrency());

        // Process payment in Payment context
        Payment payment = paymentService.processPayment(
                orderId,
                paymentAmount,
                "CREDIT_CARD"  // Would come from user input in real app
        );

        // Update order status based on payment result
        boolean isSuccessful = payment.getStatus() == PaymentStatus.COMPLETED;
        orderService.processPayment(orderId, isSuccessful);

        return isSuccessful;
    }
}