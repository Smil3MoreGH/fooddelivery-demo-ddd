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
     * Erstellt Order + Payment (beides als Entwurf/PENDING).
     * Gibt IDs zurück.
     */
    public String[] createOrderAndPayment(String customerId, String restaurantId, Address deliveryAddress, List<OrderItemRequest> itemRequests) {
        // Order anlegen (Status CREATED)
        Order order = orderService.createOrder(customerId, restaurantId, deliveryAddress);
        order.setStatus(OrderStatus.CREATED);

        // Items aus Menü hinzufügen
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

        // Order speichern
        orderService.save(order);

        // Payment anlegen (Status PENDING)
        Payment payment = paymentIntegrationService.initiatePaymentForOrder(order, "PAYPAL", false);
        payment.setStatus(PaymentStatus.PENDING);

        // Payment speichern
        paymentIntegrationService.savePayment(payment);

        // IDs zurückgeben
        return new String[]{ order.getId(), payment.getId() };
    }

    /**
     * Setzt Order-Status.
     */
    public void updateOrderStatus(String orderId, String newStatus) {
        Order order = orderService.findById(orderId);
        if (order != null) {
            order.setStatus(OrderStatus.valueOf(newStatus));
            orderService.save(order);
        }
    }

    /**
     * Setzt Order-Status + "paid"-Flag.
     */
    public void updateOrderStatusAndPaid(String orderId, String newStatus, boolean paid) {
        Order order = orderService.findById(orderId);
        if (order != null) {
            order.setStatus(OrderStatus.valueOf(newStatus));
            order.setPaid(paid);
            orderService.save(order);
        }
    }

    /**
     * Setzt Payment-Status.
     */
    public void updatePaymentStatus(String paymentId, String newStatus) {
        Payment payment = paymentIntegrationService.findById(paymentId);
        if (payment != null) {
            payment.setStatus(PaymentStatus.valueOf(newStatus));
            paymentIntegrationService.savePayment(payment);
        }
    }

    /**
     * Vollständiger Bestell-Workflow (mit direkter Zahlung).
     * Gibt Order-ID zurück, falls erfolgreich, sonst null.
     */
    public String placeOrder(String customerId, String restaurantId, Address deliveryAddress, List<OrderItemRequest> itemRequests, String paymentMethod, boolean simulateSuccess) {
        // Order anlegen
        Order order = orderService.createOrder(customerId, restaurantId, deliveryAddress);

        // Items hinzufügen
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

        // Order bestätigen
        orderService.confirmOrder(order);

        // Order speichern
        orderService.save(order);

        // Payment anstoßen
        Payment payment = paymentIntegrationService.initiatePaymentForOrder(order, paymentMethod, simulateSuccess);

        // Falls Payment erfolgreich, Order als bezahlt markieren
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            orderService.markOrderAsPaid(order);
            orderService.save(order);
            return order.getId();
        } else {
            // Payment fehlgeschlagen
            return null;
        }
    }

    /**
     * DTO für Bestellpositionen.
     */
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
