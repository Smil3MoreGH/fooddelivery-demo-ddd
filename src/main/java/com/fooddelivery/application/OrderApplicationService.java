// This coordinates use cases and provides an API for the UI
package com.fooddelivery.application;

import com.fooddelivery.integration.PaymentIntegrationService;
import com.fooddelivery.ordermanagement.service.*;
import com.fooddelivery.ordermanagement.domain.*;
import com.fooddelivery.restaurant.domain.*;

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

    // Use case: Place an order
    public String placeOrder(
            String customerId,
            String restaurantId,
            Address deliveryAddress,
            List<OrderItemRequest> itemRequests) {

        // Create order in Order bounded context
        Order order = orderService.createOrder(customerId, restaurantId, deliveryAddress);

        // Get menu from Restaurant bounded context
        Menu menu = menuRepository.findByRestaurantId(restaurantId);

        // Add items to order
        for (OrderItemRequest itemRequest : itemRequests) {
            MenuItem menuItem = menu.getItem(itemRequest.getMenuItemId());

            if (menuItem != null && menuItem.isAvailable()) {
                // Translate from MenuItem (Restaurant context) to OrderItem (Order context)
                OrderItem orderItem = new OrderItem(
                        menuItem.getId(),
                        menuItem.getName(),
                        itemRequest.getQuantity(),
                        new Money(menuItem.getPrice().getValue(), menuItem.getPrice().getCurrency())
                );

                order.addItem(orderItem);
            }
        }

        // Confirm order
        order.confirm();
        orderService.orderRepository.save(order);

        // Process payment through integration service (anti-corruption layer)
        paymentIntegrationService.initiatePaymentForOrder(order.getId());

        return order.getId();
    }

    public PaymentIntegrationService getPaymentIntegrationService() {
        return paymentIntegrationService;
    }

    public OrderService getOrderService() {
        return orderService;
    }

    // DTO for the application layer
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