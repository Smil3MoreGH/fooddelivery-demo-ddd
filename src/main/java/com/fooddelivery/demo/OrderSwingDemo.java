package com.fooddelivery.demo;

import com.fooddelivery.application.*;
import com.fooddelivery.ordermanagement.domain.*;
import com.fooddelivery.ordermanagement.service.*;
import com.fooddelivery.integration.*;
import com.fooddelivery.payment.domain.*;
import com.fooddelivery.restaurant.domain.Menu;
import com.fooddelivery.restaurant.domain.MenuItem;
import com.fooddelivery.restaurant.domain.MenuRepository;
import com.fooddelivery.restaurant.domain.Price;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class OrderSwingDemo extends JFrame {
    private JTextArea eventLog;
    private JButton placeOrderBtn, payBtn, deliverBtn, cancelBtn;
    private OrderApplicationService appService;
    private String orderId;

    public OrderSwingDemo() {
        super("Food Delivery DDD Swing Demo");

        // --- DDD Infrastructure Setup ---
        OrderRepository orderRepo = new DemoExecution.InMemoryOrderRepository();
        PaymentRepository paymentRepo = new DemoExecution.InMemoryPaymentRepository();
        MenuRepository menuRepo = new DemoExecution.InMemoryMenuRepository();

        OrderService orderService = new OrderService(orderRepo);
        PaymentService paymentService = new PaymentService(paymentRepo);
        PaymentIntegrationService integrationService = new PaymentIntegrationService(orderService, paymentService);

        setupRestaurantAndMenu(menuRepo);

        appService = new OrderApplicationService(orderService, menuRepo, integrationService);

        // --- UI Setup ---
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        placeOrderBtn = new JButton("Place Order");
        payBtn = new JButton("Pay Order");
        deliverBtn = new JButton("Deliver Order");
        cancelBtn = new JButton("Cancel Order");

        payBtn.setEnabled(false);
        deliverBtn.setEnabled(false);
        cancelBtn.setEnabled(false);

        buttonPanel.add(placeOrderBtn);
        buttonPanel.add(payBtn);
        buttonPanel.add(deliverBtn);
        buttonPanel.add(cancelBtn);

        eventLog = new JTextArea(12, 50);
        eventLog.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(eventLog);

        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // --- Button Actions ---
        placeOrderBtn.addActionListener(e -> onPlaceOrder());
        payBtn.addActionListener(e -> onPayOrder());
        deliverBtn.addActionListener(e -> onDeliverOrder());
        cancelBtn.addActionListener(e -> onCancelOrder());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void onPlaceOrder() {
        // For demo: fixed customer and address, 2 pizza + 1 pasta
        String customerId = "cust-" + UUID.randomUUID().toString().substring(0, 8);
        Address address = new Address("Hauptstraße 1", "10115", "Berlin");
        List<OrderApplicationService.OrderItemRequest> items = Arrays.asList(
                new OrderApplicationService.OrderItemRequest("item-1", 2),
                new OrderApplicationService.OrderItemRequest("item-2", 1)
        );
        orderId = appService.placeOrder(customerId, "rest-123", address, items);

        eventLog.append("Order placed! Order ID: " + orderId + "\n");
        // For now, we only show "placed" event.
        showDomainEvents(orderId);

        payBtn.setEnabled(true);
        deliverBtn.setEnabled(false);
        cancelBtn.setEnabled(true);
        placeOrderBtn.setEnabled(false);
    }

    private void onPayOrder() {
        // Simulate successful payment through integration service
        boolean paid = appService.getPaymentIntegrationService().initiatePaymentForOrder(orderId);
        if (paid) {
            eventLog.append("Payment successful for Order ID: " + orderId + "\n");
        } else {
            eventLog.append("Payment FAILED for Order ID: " + orderId + "\n");
        }
        showDomainEvents(orderId);

        payBtn.setEnabled(false);
        deliverBtn.setEnabled(true);
    }

    private void onDeliverOrder() {
        // You may want to implement "deliver" method in Order/ApplicationService
        eventLog.append("Order delivered! (Demo: implement event trigger here)\n");
        // TODO: Fire OrderDeliveredEvent, update domain and show event
        showDomainEvents(orderId);

        deliverBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
    }

    private void onCancelOrder() {
        // TODO: Call cancel on your domain model/application service
        eventLog.append("Order cancelled! (Demo: implement event trigger here)\n");
        // TODO: Fire OrderCancelledEvent, update domain and show event
        showDomainEvents(orderId);

        payBtn.setEnabled(false);
        deliverBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
    }

    private void showDomainEvents(String orderId) {
        // Fetch events from order aggregate (implement getDomainEvents in Order/OrderRepo)
        Order order = appService.getOrderService().getOrderRepository().findById(orderId);
        if (order != null) {
            for (DomainEvent event : order.getDomainEvents()) {
                eventLog.append("Event: " + event.getClass().getSimpleName() +
                        " at " + event.getOccurredOn() + "\n");
            }
            order.clearEvents(); // Optional: Clear after showing
        }
    }

    private void setupRestaurantAndMenu(MenuRepository menuRepo) {
        String restaurantId = "rest-123";
        Menu menu = new Menu("menu-123", restaurantId);

        MenuItem pizza = new MenuItem(
                "item-1", "Margherita Pizza", "Pizza with tomato sauce, mozzarella, basil",
                new Price(9.99, "EUR")
        );
        pizza.addAllergen("Gluten");
        pizza.addAllergen("Lactose");

        MenuItem pasta = new MenuItem(
                "item-2", "Spaghetti Carbonara", "Pasta with egg, cheese, pancetta, pepper",
                new Price(11.99, "EUR")
        );
        pasta.addAllergen("Gluten");
        pasta.addAllergen("Lactose");
        pasta.addAllergen("Egg");

        menu.addItem(pizza);
        menu.addItem(pasta);
        menuRepo.save(menu);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new OrderSwingDemo().setVisible(true));
    }
}
