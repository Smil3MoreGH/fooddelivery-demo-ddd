package com.fooddelivery.demo;

import com.fooddelivery.ordermanagement.domain.Address;
import com.fooddelivery.ordermanagement.domain.Order;
import com.fooddelivery.ordermanagement.domain.OrderItem;
import com.fooddelivery.ordermanagement.domain.Restaurant;
import com.fooddelivery.ordermanagement.infrastructure.SqliteOrderRepository;
import com.fooddelivery.ordermanagement.domain.Money;
import com.fooddelivery.payment.domain.Payment;
import com.fooddelivery.payment.infrastructure.SqlitePaymentRepository;
import com.fooddelivery.restaurant.domain.Menu;
import com.fooddelivery.restaurant.domain.MenuItem;
import com.fooddelivery.restaurant.domain.Price;
import com.fooddelivery.restaurant.infrastructure.SqliteMenuRepository;
import com.fooddelivery.restaurant.infrastructure.SqliteRestaurantRepository;

public class DemoDbInitializer {
    private static final String DB_PATH = "./order_management.db";

    public static void seedRestaurantsAndMenus() {
        SqliteRestaurantRepository restaurantRepo = new SqliteRestaurantRepository();
        SqliteMenuRepository menuRepo = new SqliteMenuRepository();

        // 1. Burger-Restaurant
        String burgerId = "rest-burger";
        Restaurant burger = new Restaurant(burgerId, "Burger Place",
                new Address("Burgerstraße 1", "10001", "Burgertown"));
        restaurantRepo.save(burger);

        Menu burgerMenu = new Menu("menu-burger", burgerId);
        burgerMenu.addItem(new MenuItem("burger-1", "Cheeseburger", "Klassischer Cheeseburger mit Pommes", new Price(8.99, "EUR")));
        burgerMenu.addItem(new MenuItem("burger-2", "Veggie Burger", "Vegetarischer Burger mit Süßkartoffelpommes", new Price(9.49, "EUR")));
        burgerMenu.addItem(new MenuItem("burger-3", "Double Beef", "Doppelter Beef-Burger, extra saftig", new Price(11.99, "EUR")));
        menuRepo.save(burgerMenu);

        // 2. Pizza-Restaurant
        String pizzaId = "rest-pizza";
        Restaurant pizza = new Restaurant(pizzaId, "Pizza Paradiso",
                new Address("Pizzagasse 2", "10002", "Pizzacity"));
        restaurantRepo.save(pizza);

        Menu pizzaMenu = new Menu("menu-pizza", pizzaId);
        pizzaMenu.addItem(new MenuItem("pizza-1", "Margherita", "Tomate, Mozzarella, Basilikum", new Price(7.99, "EUR")));
        pizzaMenu.addItem(new MenuItem("pizza-2", "Salami", "Salami, Mozzarella, Tomatensauce", new Price(9.29, "EUR")));
        pizzaMenu.addItem(new MenuItem("pizza-3", "Funghi", "Champignons, Käse, Tomatensauce", new Price(8.79, "EUR")));
        menuRepo.save(pizzaMenu);

        // 3. Salat-Restaurant
        String saladId = "rest-salat";
        Restaurant salad = new Restaurant(saladId, "Salat Oase",
                new Address("Salatweg 3", "10003", "Greensville"));
        restaurantRepo.save(salad);

        Menu saladMenu = new Menu("menu-salat", saladId);
        saladMenu.addItem(new MenuItem("salat-1", "Caesar Salad", "Römersalat, Hähnchen, Caesar-Dressing", new Price(10.49, "EUR")));
        saladMenu.addItem(new MenuItem("salat-2", "Greek Salad", "Tomaten, Gurken, Feta, Oliven", new Price(9.89, "EUR")));
        saladMenu.addItem(new MenuItem("salat-3", "Quinoa Bowl", "Quinoa, Kichererbsen, Gemüse", new Price(11.29, "EUR")));
        menuRepo.save(saladMenu);

        // === DUMMY ORDER & PAYMENT ===
        SqliteOrderRepository orderRepo = new SqliteOrderRepository();
        SqlitePaymentRepository paymentRepo = new SqlitePaymentRepository();

        String dummyOrderId = "order-demo-1";
        String dummyCustomerId = "customer-demo";
        String dummyRestaurantId = "rest-burger";
        Address dummyAddress = new Address("Teststraße 12", "12345", "Testhausen");

        // Dummy-Order anlegen
        Order dummyOrder = new Order(dummyOrderId, dummyCustomerId, dummyRestaurantId, dummyAddress);

        // OrderItem mit deiner eigenen Money-Klasse hinzufügen
        Money burgerPrice = new Money(8.99, "EUR");
        OrderItem burgerItem = new OrderItem("burger-1", "Cheeseburger", 1, burgerPrice);
        dummyOrder.addItem(burgerItem);

        // Jetzt Status setzen
        dummyOrder.confirm();
        dummyOrder.markAsPaid();

        orderRepo.save(dummyOrder);

        // Dummy Payment für die Order
        // ACHTUNG: Falls payment.domain.Money ein anderes Package ist,
        // kannst du evtl. auch einfach deine Money-Klasse verwenden,
        // oder ein separates Objekt bauen, je nach deiner Payment-Klasse!
        com.fooddelivery.payment.domain.Money paymentAmount = new com.fooddelivery.payment.domain.Money(8.99, "EUR");
        String dummyPaymentId = "payment-demo-1";
        Payment dummyPayment = new Payment(dummyPaymentId, dummyOrderId, paymentAmount, "Kreditkarte");
        dummyPayment.markAsCompleted("TRANSACTION12345");

        paymentRepo.save(dummyPayment);

        System.out.println("Drei Restaurants mit Menüs wurden angelegt!");
        System.out.println("Dummy Order und Dummy Payment wurden angelegt!");
    }
}
