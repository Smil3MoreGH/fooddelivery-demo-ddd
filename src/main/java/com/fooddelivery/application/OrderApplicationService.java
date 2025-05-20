package com.fooddelivery.application;

import com.fooddelivery.integration.PaymentIntegrationService;
import com.fooddelivery.ordermanagement.domain.*;
import com.fooddelivery.ordermanagement.service.OrderService;
import com.fooddelivery.restaurant.domain.Menu;
import com.fooddelivery.restaurant.domain.MenuItem;
import com.fooddelivery.restaurant.domain.MenuRepository;
import com.fooddelivery.payment.domain.Payment;
import com.fooddelivery.payment.domain.PaymentStatus;
import com.fooddelivery.payment.service.PaymentService;

import java.util.ArrayList;
import java.util.List;

public class OrderApplicationService {
    private final OrderService orderService;
    private final MenuRepository menuRepository;
    private final PaymentIntegrationService paymentIntegrationService;

    public OrderApplicationService(
            OrderService orderService,
            MenuRepository menuRepository,
            PaymentIntegrationService paymentIntegrationService) {
        this.orderService = orderService;
        this.menuRepository = menuRepository;
        this.paymentIntegrationService = paymentIntegrationService;
    }

    /**
     * Neuer Workflow:
     * - Erstellt eine Order mit Status CREATED.
     * - Erstellt ein Payment mit Status PENDING.
     * - Gibt beide IDs zurück.
     */
    public String[] createOrderAndPayment(String customerId, String restaurantId, Address deliveryAddress, List<OrderItemRequest> itemRequests) {
        // 1. Create Order aggregate mit Status CREATED
        Order order = orderService.createOrder(customerId, restaurantId, deliveryAddress);
        order.setStatus(OrderStatus.CREATED);

        // 2. Add items to order
        Menu menu = menuRepository.findByRestaurantId(restaurantId);
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemRequest itemRequest : itemRequests) {
            MenuItem menuItem = menu.getItem(itemRequest.getMenuItemId());
            if (menuItem != null && menuItem.isAvailable()) {
                items.add(new OrderItem(
                        menuItem.getId(),
                        menuItem.getName(),
                        itemRequest.getQuantity(),
                        new Money(menuItem.getPrice().getValue(), menuItem.getPrice().getCurrency())
                ));
            }
        }
        orderService.addItems(order, items);

        // 3. Save order with status CREATED
        orderService.save(order);

        // 4. Create Payment with status PENDING (simulateSuccess = false!)
        Payment payment = paymentIntegrationService.initiatePaymentForOrder(order, "PAYPAL", false);
        payment.setStatus(PaymentStatus.PENDING);

        // 5. Save payment
        paymentIntegrationService.savePayment(payment);

        // 6. Return IDs
        return new String[]{ order.getId(), payment.getId() };
    }
    public void updateOrderStatus(String orderId, String newStatus) {
        Order order = orderService.findById(orderId);
        if (order != null) {
            order.setStatus(OrderStatus.valueOf(newStatus));
            orderService.save(order);
        }
    }
    public void updateOrderStatusAndPaid(String orderId, String newStatus, boolean paid) {
        Order order = orderService.findById(orderId);
        if (order != null) {
            order.setStatus(OrderStatus.valueOf(newStatus));
            order.setPaid(paid); // <--- das ist neu!
            orderService.save(order);
        }
    }
    public void updatePaymentStatus(String paymentId, String newStatus) {
        Payment payment = paymentIntegrationService.findById(paymentId);
        if (payment != null) {
            payment.setStatus(PaymentStatus.valueOf(newStatus));
            paymentIntegrationService.savePayment(payment);
        }
    }
    public String placeOrder(String customerId, String restaurantId, Address deliveryAddress, List<OrderItemRequest> itemRequests, String paymentMethod, boolean simulateSuccess) {
        // 1. Create Order aggregate
        Order order = orderService.createOrder(customerId, restaurantId, deliveryAddress);

        // 2. Add items to order
        Menu menu = menuRepository.findByRestaurantId(restaurantId);
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemRequest itemRequest : itemRequests) {
            MenuItem menuItem = menu.getItem(itemRequest.getMenuItemId());
            if (menuItem != null && menuItem.isAvailable()) {
                items.add(new OrderItem(
                        menuItem.getId(),
                        menuItem.getName(),
                        itemRequest.getQuantity(),
                        new Money(menuItem.getPrice().getValue(), menuItem.getPrice().getCurrency())
                ));
            }
        }
        orderService.addItems(order, items);

        // 3. Confirm the order
        orderService.confirmOrder(order);

        // 4. Save order (before payment)
        orderService.save(order);

        // 5. Initiate payment via Integration/ACL
        Payment payment = paymentIntegrationService.initiatePaymentForOrder(order, paymentMethod, simulateSuccess);

        // 6. If payment successful, mark order as paid and save again
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            orderService.markOrderAsPaid(order);
            orderService.save(order);
            return order.getId(); // Or a success DTO
        } else {
            // Payment failed, order is not paid/confirmed
            // Optionally fire a domain event, or throw an exception, or return error DTO
            return null; // Or a failure DTO/error code/message
        }
    }
    public static class OrderItemRequest {
        private final String menuItemId;
        private final int quantity;

        public OrderItemRequest(String menuItemId, int quantity) {
            this.menuItemId = menuItemId;
            this.quantity = quantity;
        }

        public String getMenuItemId() { return menuItemId; }
        public int getQuantity() { return quantity; }
    }
}
