// This shows how you might use the system in a main method
// UI
package com.fooddelivery;

import com.fooddelivery.application.OrderApplicationService;
import com.fooddelivery.application.OrderApplicationService.OrderItemRequest;
import com.fooddelivery.ordermanagement.domain.*;
import com.fooddelivery.ordermanagement.service.*;
import com.fooddelivery.payment.domain.*;
import com.fooddelivery.restaurant.domain.*;
import com.fooddelivery.integration.PaymentIntegrationService;

import java.util.*;

public class DemoExecution {
    public static void main(String[] args) {
        System.out.println("=============== FOOD DELIVERY DDD DEMO ===============");

        // Create repositories (in-memory implementations)
        OrderRepository orderRepo = new InMemoryOrderRepository();
        PaymentRepository paymentRepo = new InMemoryPaymentRepository();
        MenuRepository menuRepo = new InMemoryMenuRepository();

        // Create services
        OrderService orderService = new OrderService(orderRepo);
        PaymentService paymentService = new PaymentService(paymentRepo);
        PaymentIntegrationService integrationService = new PaymentIntegrationService(orderService, paymentService);

        // Setup restaurant and menu
        setupRestaurantAndMenu(menuRepo);

        // Create application service
        OrderApplicationService appService = new OrderApplicationService(orderService, menuRepo, integrationService);

        // Demo use case: Place an order
        placeOrderDemo(appService);
    }

    private static void setupRestaurantAndMenu(MenuRepository menuRepo) {
        System.out.println("\n--- Setting up restaurant and menu ---");

        // Create a restaurant menu
        String restaurantId = "rest-123";
        Menu menu = new Menu("menu-123", restaurantId);

        // Add some items to the menu
        MenuItem pizza = new MenuItem(
                "item-1",
                "Margherita Pizza",
                "Classic pizza with tomato sauce, mozzarella, and basil",
                new Price(9.99, "EUR")
        );
        pizza.addAllergen("Gluten");
        pizza.addAllergen("Lactose");

        MenuItem pasta = new MenuItem(
                "item-2",
                "Spaghetti Carbonara",
                "Pasta with egg, cheese, pancetta, and pepper",
                new Price(11.99, "EUR")
        );
        pasta.addAllergen("Gluten");
        pasta.addAllergen("Lactose");
        pasta.addAllergen("Egg");

        menu.addItem(pizza);
        menu.addItem(pasta);

        // Save to repository
        menuRepo.save(menu);

        System.out.println("Restaurant menu created with " + menu.getItems().size() + " items");
    }

    private static void placeOrderDemo(OrderApplicationService appService) {
        System.out.println("\n--- Placing an order ---");

        // Customer info
        String customerId = "cust-" + UUID.randomUUID().toString().substring(0, 8);
        Address deliveryAddress = new Address("Hauptstraße 1", "10115", "Berlin");

        // Order items
        OrderItemRequest pizzaRequest = new OrderItemRequest("item-1", 2); // 2 pizzas
        OrderItemRequest pastaRequest = new OrderItemRequest("item-2", 1); // 1 pasta

        // Place order
        String orderId = appService.placeOrder(
                customerId,
                "rest-123",
                deliveryAddress,
                Arrays.asList(pizzaRequest, pastaRequest)
        );

        System.out.println("Order placed successfully!");
        System.out.println("Order ID: " + orderId);
        System.out.println("Customer ID: " + customerId);
        System.out.println("Delivery Address: " + deliveryAddress.getStreet() + ", " +
                deliveryAddress.getZipCode() + " " + deliveryAddress.getCity());
    }

    // In-memory implementations of repositories (simplified for demo)
    static class InMemoryOrderRepository implements OrderRepository {
        private final Map<String, Order> orders = new HashMap<>();

        @Override
        public void save(Order order) {
            orders.put(order.getId(), order);
        }

        @Override
        public Order findById(String id) {
            return orders.get(id);
        }

        @Override
        public List<Order> findByCustomerId(String customerId) {
            return orders.values().stream()
                    .filter(o -> o.getCustomerId().equals(customerId))
                    .collect(java.util.stream.Collectors.toList());
        }

        @Override
        public List<Order> findByRestaurantId(String restaurantId) {
            return orders.values().stream()
                    .filter(o -> o.getRestaurantId().equals(restaurantId))
                    .collect(java.util.stream.Collectors.toList());
        }
    }

    static class InMemoryPaymentRepository implements PaymentRepository {
        private final Map<String, Payment> payments = new HashMap<>();

        @Override
        public void save(Payment payment) {
            payments.put(payment.getId(), payment);
        }

        @Override
        public Payment findById(String id) {
            return payments.get(id);
        }

        @Override
        public Payment findByOrderId(String orderId) {
            return payments.values().stream()
                    .filter(p -> p.getOrderId().equals(orderId))
                    .findFirst()
                    .orElse(null);
        }
    }

    static class InMemoryMenuRepository implements MenuRepository {
        private final Map<String, Menu> menus = new HashMap<>();

        @Override
        public void save(Menu menu) {
            menus.put(menu.getId(), menu);
        }

        @Override
        public Menu findById(String id) {
            return menus.get(id);
        }

        @Override
        public Menu findByRestaurantId(String restaurantId) {
            return menus.values().stream()
                    .filter(m -> m.getRestaurantId().equals(restaurantId))
                    .findFirst()
                    .orElse(null);
        }
    }
}