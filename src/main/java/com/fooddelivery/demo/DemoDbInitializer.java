package com.fooddelivery.demo;

import com.fooddelivery.ordermanagement.domain.*;
import com.fooddelivery.ordermanagement.infrastructure.SqliteOrderRepository;
import com.fooddelivery.payment.domain.*;
import com.fooddelivery.payment.infrastructure.SqlitePaymentRepository;
import com.fooddelivery.restaurant.infrastructure.SqliteRestaurantRepository;
import com.fooddelivery.restaurant.domain.Menu;
import com.fooddelivery.restaurant.domain.MenuItem;
import com.fooddelivery.restaurant.domain.Price;
import com.fooddelivery.restaurant.infrastructure.SqliteMenuRepository;

import java.util.UUID;

public class DemoDbInitializer {

    public static void initializeAllDbs() {
        // 1. Repositories anlegen
        var restaurantRepo = new SqliteRestaurantRepository();
        var menuRepo = new SqliteMenuRepository();
        var orderRepo = new SqliteOrderRepository();
        var paymentRepo = new SqlitePaymentRepository();

        // 2. Restaurants + Menüs anlegen
        for (int r = 1; r <= 3; r++) {
            String restId = "rest-" + r;
            String restName = "Restaurant " + r;
            Address address = new Address("Straße " + r, "1010" + r, "Stadt" + r);

            Restaurant restaurant = new Restaurant(restId, restName, address);
            restaurantRepo.save(restaurant);

            Menu menu = new Menu("menu-" + r, restId);
            // Drei Gerichte pro Restaurant
            for (int i = 1; i <= 3; i++) {
                MenuItem item = new MenuItem(
                        "item-" + r + "-" + i,
                        "Gericht " + i + " von " + restName,
                        "Lecker Gericht Nr. " + i,
                        new Price(5.99 + i, "EUR")
                );
                menu.addItem(item);
            }
            menuRepo.save(menu);
        }

        // 3. Optional: Beispiel-Bestellung + Zahlung
        String customerId = "demo-customer";
        String restId = "rest-1";
        Address deliveryAddr = new Address("Kundenstraße 1", "10111", "DemoCity");
        Order order = new Order(UUID.randomUUID().toString(), customerId, restId, deliveryAddr);

        // Items aus Menü holen
        Menu demoMenu = menuRepo.findByRestaurantId(restId);
        if (demoMenu != null && !demoMenu.getItems().isEmpty()) {
            for (MenuItem menuItem : demoMenu.getItems()) {
                OrderItem orderItem = new OrderItem(
                        menuItem.getId(),
                        menuItem.getName(),
                        1,
                        new com.fooddelivery.ordermanagement.domain.Money(menuItem.getPrice().getValue(), menuItem.getPrice().getCurrency())
                );
                order.addItem(orderItem);
            }
        }
        order.confirm();
        orderRepo.save(order);

        // Zahlung anlegen
        var payment = new Payment(
                UUID.randomUUID().toString(),
                order.getId(),
                new com.fooddelivery.payment.domain.Money(order.calculateTotal().getAmount(), "EUR"),
                "CREDIT_CARD"
        );
        payment.markAsCompleted("TX-INITDEMO");
        paymentRepo.save(payment);

        System.out.println("Demo-Daten erfolgreich angelegt!");
    }
}
