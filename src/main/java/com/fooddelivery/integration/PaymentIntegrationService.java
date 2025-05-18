// Anti-Corruption Layer translating between Order and Payment contexts
package com.fooddelivery.integration;

import com.fooddelivery.ordermanagement.domain.Address;
import com.fooddelivery.ordermanagement.domain.Order;
import com.fooddelivery.ordermanagement.domain.OrderItem;
import com.fooddelivery.ordermanagement.domain.Money;
import com.fooddelivery.ordermanagement.domain.Restaurant;
import com.fooddelivery.ordermanagement.service.OrderService;
import com.fooddelivery.payment.domain.Payment;
import com.fooddelivery.payment.domain.PaymentStatus;
import com.fooddelivery.payment.domain.PaymentService;
import com.fooddelivery.restaurant.domain.MenuItem;

import java.util.List;


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
        Money orderTotal = order.calculateTotal();
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

    public boolean payAndCreateOrder(
            String customerName,
            String street,
            String city,
            String restaurantId,
            List<MenuItem> items,
            double totalAmount,
            String paymentMethod) {

        String orderId = "order-" + System.currentTimeMillis();
        String paymentId = "pay-" + System.currentTimeMillis();

        // === 1. Payment erzeugen und speichern ===
        com.fooddelivery.payment.domain.Money paymentAmount =
                new com.fooddelivery.payment.domain.Money(totalAmount, "EUR");

        Payment payment = new Payment(paymentId, orderId, paymentAmount, paymentMethod);

        if (!"PAYPAL".equals(paymentMethod)) {
            payment.markAsFailed(); // <-- Setze explizit auf FAILED!
        } else {
            payment.markAsCompleted(paymentMethod + "-TRANSACTION-" + System.currentTimeMillis());
        }

        paymentService.getRepository().save(payment);

        // === 2. Prüfen, ob Payment erfolgreich ===
        Payment paymentFromDb = paymentService.getRepository().findById(paymentId);
        if (paymentFromDb == null || paymentFromDb.getStatus() != PaymentStatus.COMPLETED) {
            return false;
        }

        // === 3. Jetzt Order erzeugen und speichern ===
        Address deliveryAddress = new Address(street, "00000", city); // Dummy PLZ
        Order order = new Order(orderId, customerName, restaurantId, deliveryAddress);

        for (MenuItem item : items) {
            com.fooddelivery.ordermanagement.domain.Money itemPrice =
                    new com.fooddelivery.ordermanagement.domain.Money(item.getPrice().getValue(), item.getPrice().getCurrency());
            order.addItem(new OrderItem(item.getId(), item.getName(), 1, itemPrice));
        }
        order.confirm();
        order.markAsPaid(); // wird nur aufgerufen, wenn wirklich bezahlt
        orderService.getOrderRepository().save(order);

        return true;
    }

}