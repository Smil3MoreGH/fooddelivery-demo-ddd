package com.fooddelivery.demo;

import com.fooddelivery.ordermanagement.domain.Address;
import com.fooddelivery.ordermanagement.domain.Restaurant;
import com.fooddelivery.restaurant.domain.Menu;
import com.fooddelivery.restaurant.domain.MenuItem;
import com.fooddelivery.restaurant.infrastructure.SqliteMenuRepository;
import com.fooddelivery.restaurant.infrastructure.SqliteRestaurantRepository;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import com.fooddelivery.application.OrderApplicationService;
import com.fooddelivery.application.OrderApplicationService.OrderItemRequest;
import com.fooddelivery.integration.PaymentIntegrationService;
import com.fooddelivery.ordermanagement.infrastructure.SqliteOrderRepository;
import com.fooddelivery.ordermanagement.service.OrderService;
import com.fooddelivery.payment.infrastructure.SqlitePaymentRepository;
import com.fooddelivery.payment.service.PaymentService;

import java.util.ArrayList;
import java.util.List;

// Warenkorb (einfach gehalten)
class Basket {
    private final List<MenuItem> items = new java.util.ArrayList<>();
    public void addItem(MenuItem item) { items.add(item); }
    public List<MenuItem> getItems() { return items; }
    public void clear() { items.clear(); }
}

public class MobileFoodUI extends Application {

    private final SqliteRestaurantRepository restaurantRepo = new SqliteRestaurantRepository();
    private final SqliteMenuRepository menuRepo = new SqliteMenuRepository();
    private final Basket basket = new Basket();
    private final OrderApplicationService orderApplicationService = new OrderApplicationService(
            new OrderService(new SqliteOrderRepository()),
            menuRepo,
            new PaymentIntegrationService(new PaymentService(new SqlitePaymentRepository()))
    );

    @Override
    public void start(Stage stage) {
        showRestaurantList(stage); // Startscreen: Restaurant-Auswahl
    }

    // Restaurant-Liste anzeigen
    private void showRestaurantList(Stage stage) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(22));
        root.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("LieferanDDDo");
        title.setStyle("-fx-font-size: 2em; -fx-font-weight: bold; -fx-text-fill: #FF8000;");
        root.getChildren().add(title);

        VBox cardList = new VBox(20);
        cardList.setFillWidth(true);

        List<Restaurant> restaurants = restaurantRepo.findAll();

        for (Restaurant restaurant : restaurants) {
            String imgFile = getImageForRestaurant(restaurant.getName());
            HBox card = createRestaurantCard(
                    imgFile,
                    restaurant.getName(),
                    restaurant.getAddress().getStreet(),
                    "Küche", "20–30 min", "Top Angebot", "#FF8000"
            );
            card.setOnMouseClicked(e -> openMenuPage(stage, restaurant)); // Klick: Menü-Seite öffnen
            cardList.getChildren().add(card);
        }

        root.getChildren().add(cardList);

        Scene scene = new Scene(root, 430, 840);
        scene.getStylesheets().add(getClass().getResource("/mobile-food.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Food Delivery Demo");
        stage.show();
    }

    // Menü-Seite für Restaurant
    private void openMenuPage(Stage stage, Restaurant restaurant) {
        Menu menu = menuRepo.findByRestaurantId(restaurant.getId());

        VBox root = new VBox(20);
        root.setPadding(new Insets(22));
        root.setAlignment(Pos.TOP_CENTER);

        Label title = new Label(restaurant.getName() + " – Menü");
        title.setStyle("-fx-font-size: 1.6em; -fx-font-weight: bold;");
        root.getChildren().add(title);

        VBox menuList = new VBox(15);
        menuList.setFillWidth(true);

        if (menu != null) {
            for (MenuItem item : menu.getItems()) {
                HBox itemCard = createMenuItemCard(item, stage, restaurant);
                menuList.getChildren().add(itemCard);
            }
        } else {
            menuList.getChildren().add(new Label("Kein Menü verfügbar."));
        }

        // Warenkorb-Bereich unten
        VBox basketBox = new VBox(10);
        basketBox.setPadding(new Insets(15, 0, 0, 0));
        basketBox.setAlignment(Pos.TOP_CENTER);
        basketBox.setStyle("-fx-background-color: #f6f6f6; -fx-border-radius: 10; -fx-background-radius: 10;");
        updateBasketBox(basketBox, stage, restaurant);

        VBox.setVgrow(menuList, Priority.ALWAYS);

        root.getChildren().addAll(menuList, basketBox);

        javafx.scene.control.Button back = new javafx.scene.control.Button("Zurück");
        back.setOnAction(e -> showRestaurantList(stage));
        root.getChildren().add(back);

        Scene menuScene = new Scene(root, 430, 840);
        menuScene.getStylesheets().add(getClass().getResource("/mobile-food.css").toExternalForm());
        stage.setScene(menuScene);
    }

    // Menü-Item-Karte
    private HBox createMenuItemCard(MenuItem item, Stage stage, Restaurant restaurant) {
        HBox card = new HBox(20);
        card.setPadding(new Insets(14));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #fff; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, #ccc, 6, 0, 0, 2);");
        card.getStyleClass().add("menu-card");

        VBox info = new VBox(5);
        Label name = new Label(item.getName());
        name.setStyle("-fx-font-size:1.1em; -fx-font-weight: 500;");
        Label desc = new Label(item.getDescription());
        desc.setStyle("-fx-font-size:0.9em; -fx-text-fill:#555;");
        info.getChildren().addAll(name, desc);

        Label price = new Label(String.format("%.2f %s", item.getPrice().getValue(), item.getPrice().getCurrency()));
        price.setStyle("-fx-font-size:1.1em; -fx-font-weight:600; -fx-text-fill: #FF8000;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(info, spacer, price);

        // Klick: Item in den Warenkorb
        card.setOnMouseClicked(e -> {
            basket.addItem(item);
            VBox root = (VBox) card.getParent().getParent();
            VBox basketBox = (VBox) root.getChildren().get(root.getChildren().size() - 2);
            updateBasketBox(basketBox, stage, restaurant);
        });

        return card;
    }

    // Warenkorb anzeigen/aktualisieren
    private void updateBasketBox(VBox basketBox, Stage stage, Restaurant restaurant) {
        basketBox.getChildren().clear();

        Label basketTitle = new Label("Warenkorb");
        basketTitle.setStyle("-fx-font-size:1.1em; -fx-font-weight:600;");

        VBox items = new VBox(4);
        double total = 0.0;
        if (basket.getItems().isEmpty()) {
            items.getChildren().add(new Label("Noch nichts im Warenkorb."));
        } else {
            for (MenuItem item : basket.getItems()) {
                HBox row = new HBox(8);
                Label name = new Label(item.getName());
                name.setStyle("-fx-font-size:1em;");
                Label price = new Label(String.format("%.2f %s", item.getPrice().getValue(), item.getPrice().getCurrency()));
                price.setStyle("-fx-font-size:1em; -fx-text-fill: #FF8000;");
                row.getChildren().addAll(name, price);
                items.getChildren().add(row);
                total += item.getPrice().getValue();
            }
        }

        Label totalLabel = new Label("Gesamt: " + String.format("%.2f €", total));
        totalLabel.setStyle("-fx-font-size:1em; -fx-font-weight:600; -fx-text-fill:#222;");

        javafx.scene.control.Button clearBtn = new javafx.scene.control.Button("Leeren");
        clearBtn.setOnAction(e -> {
            basket.clear();
            updateBasketBox(basketBox, stage, restaurant);
        });

        // Bestellen-Button
        javafx.scene.control.Button orderBtn = new javafx.scene.control.Button("Bestellen");
        orderBtn.setOnAction(e -> openOrderPage(stage, restaurant));

        basketBox.getChildren().addAll(basketTitle, items, totalLabel, clearBtn);
        if (!basket.getItems().isEmpty()) {
            basketBox.getChildren().add(orderBtn);
        }
    }

    // Restaurant-Karten
    private HBox createRestaurantCard(String imgFile, String name, String address, String tag, String time, String chip, String chipColor) {
        HBox card = new HBox(22);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER_LEFT);

        ImageView img;
        try {
            img = new ImageView(new Image(getClass().getResourceAsStream("/" + imgFile)));
        } catch (Exception e) {
            img = new ImageView();
        }
        img.setFitHeight(88);
        img.setFitWidth(88);
        img.setPreserveRatio(true);

        VBox vbox = new VBox(7);
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("card-title");

        Label addressLabel = new Label(address);
        addressLabel.getStyleClass().add("card-address");

        HBox tags = new HBox(10);
        Label tagLabel = new Label(tag);
        tagLabel.getStyleClass().add("chip");

        Label chipLabel = new Label(chip);
        chipLabel.setStyle("-fx-background-color:" + chipColor + "; -fx-background-radius:13; -fx-padding:3 12 3 12; -fx-text-fill:white; -fx-font-weight:600;");

        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("chip-green");

        tags.getChildren().addAll(tagLabel, timeLabel, chipLabel);

        vbox.getChildren().addAll(nameLabel, addressLabel, tags);

        card.getChildren().addAll(img, vbox);
        return card;
    }

    // Seite: Adresse + Bestellung absenden
    private void openOrderPage(Stage stage, Restaurant restaurant) {
        VBox root = new VBox(18);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Bestellung abschließen");
        title.setStyle("-fx-font-size:1.4em; -fx-font-weight: bold;");
        root.getChildren().add(title);

        // Adressfelder
        javafx.scene.control.TextField nameField = new javafx.scene.control.TextField();
        nameField.setPromptText("Name");

        javafx.scene.control.TextField streetField = new javafx.scene.control.TextField();
        streetField.setPromptText("Straße und Hausnummer");

        javafx.scene.control.TextField cityField = new javafx.scene.control.TextField();
        cityField.setPromptText("PLZ und Ort");

        javafx.scene.control.Button submitBtn = new javafx.scene.control.Button("Bestellung absenden");
        submitBtn.setOnAction(e -> {
            String name = nameField.getText();
            String street = streetField.getText();
            String city = cityField.getText();
            if (name.isBlank() || street.isBlank() || city.isBlank()) {
                root.getChildren().add(new Label("Bitte alle Felder ausfüllen!"));
            } else {
                double total = 0.0;
                for (MenuItem item : basket.getItems()) {
                    total += item.getPrice().getValue();
                }
                openPaymentPage(stage, restaurant, name, street, city, total);
            }
        });

        root.getChildren().addAll(nameField, streetField, cityField, submitBtn);

        javafx.scene.control.Button backBtn = new javafx.scene.control.Button("Zurück");
        backBtn.setOnAction(e -> openMenuPage(stage, restaurant));
        root.getChildren().add(backBtn);

        Scene orderScene = new Scene(root, 430, 840);
        orderScene.getStylesheets().add(getClass().getResource("/mobile-food.css").toExternalForm());
        stage.setScene(orderScene);
    }

    // Zahlungsseite anzeigen
    private void openPaymentPage(Stage stage, Restaurant restaurant, String name, String street, String city, double total) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Bezahlen");
        title.setStyle("-fx-font-size:1.3em; -fx-font-weight:bold;");
        root.getChildren().add(title);

        Label sum = new Label("Gesamtbetrag: " + String.format("%.2f €", total));
        sum.setStyle("-fx-font-size:1.2em; -fx-font-weight:600;");
        root.getChildren().add(sum);

        javafx.scene.control.Button paypalBtn = new javafx.scene.control.Button("Mit PayPal bezahlen");
        root.getChildren().add(paypalBtn);

        paypalBtn.setOnAction(e -> {
            // Order & Payment erzeugen
            String[] ids = createOrderAndPayment(name, street, city, restaurant);
            String orderId = ids[0];
            String paymentId = ids[1];
            openPaymentLoadingPage(stage, orderId, paymentId, restaurant, total);
        });

        javafx.scene.control.Button backBtn = new javafx.scene.control.Button("Zurück");
        backBtn.setOnAction(e -> openOrderPage(stage, restaurant));
        root.getChildren().add(backBtn);

        Scene paymentScene = new Scene(root, 430, 840);
        paymentScene.getStylesheets().add(getClass().getResource("/mobile-food.css").toExternalForm());
        stage.setScene(paymentScene);
    }

    // Order + Payment anlegen, IDs zurückgeben
    private String[] createOrderAndPayment(String name, String street, String city, Restaurant restaurant) {
        Address address = new Address(street, "00000", city);
        List<OrderItemRequest> itemRequests = new ArrayList<>();
        for (MenuItem item : basket.getItems()) {
            itemRequests.add(new OrderItemRequest(item.getId(), 1));
        }
        return orderApplicationService.createOrderAndPayment(name, restaurant.getId(), address, itemRequests);
    }

    // Lade-/Zahlungsstatus-Seite
    private void openPaymentLoadingPage(Stage stage, String orderId, String paymentId, Restaurant restaurant, double total) {
        VBox root = new VBox(30);
        root.setPadding(new Insets(60));
        root.setAlignment(Pos.CENTER);

        Label loading = new Label("Warte auf Bestätigung ...");
        loading.setStyle("-fx-font-size: 1.1em; -fx-font-weight: 500;");

        ProgressIndicator pi = new ProgressIndicator();
        pi.setPrefSize(56, 56);

        root.getChildren().addAll(pi, loading);

        javafx.scene.control.Button confirmBtn = new javafx.scene.control.Button("Zurück mit Bezahlen");
        javafx.scene.control.Button cancelBtn = new javafx.scene.control.Button("Zurück ohne Bezahlen");

        confirmBtn.setOnAction(e -> {
            // Bestellung als bestätigt + bezahlt markieren
            orderApplicationService.updateOrderStatusAndPaid(orderId, "CONFIRMED", true);
            orderApplicationService.updatePaymentStatus(paymentId, "COMPLETED");
            basket.clear();
            showConfirmation(stage, "Zahlung erfolgreich! Bestellung ist bestätigt.");
        });

        cancelBtn.setOnAction(e -> {
            // Bestellung stornieren
            orderApplicationService.updateOrderStatus(orderId, "CANCELLED");
            orderApplicationService.updatePaymentStatus(paymentId, "FAILED");
            showConfirmation(stage, "Bezahlung abgebrochen. Bestellung storniert.");
        });

        HBox btnBox = new HBox(22, confirmBtn, cancelBtn);
        btnBox.setAlignment(Pos.CENTER);
        root.getChildren().add(btnBox);

        Scene scene = new Scene(root, 430, 840);
        scene.getStylesheets().add(getClass().getResource("/mobile-food.css").toExternalForm());
        stage.setScene(scene);
    }

    // Bestätigungsseite
    private void showConfirmation(Stage stage, String message) {
        VBox root = new VBox(32);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(60));
        Label msg = new Label(message);
        msg.setStyle("-fx-font-size: 1.25em; -fx-font-weight: bold;");
        root.getChildren().add(msg);

        javafx.scene.control.Button backBtn = new javafx.scene.control.Button("Zurück zur Startseite");
        backBtn.setOnAction(e -> showRestaurantList(stage));
        root.getChildren().add(backBtn);

        Scene scene = new Scene(root, 430, 840);
        scene.getStylesheets().add(getClass().getResource("/mobile-food.css").toExternalForm());
        stage.setScene(scene);
    }

    // Nicht verwendet, aber Beispiel für direkten Order+Zahlungsversuch
    private boolean tryPlaceOrderWithPayment(String name, String street, String city, Restaurant restaurant, boolean simulateSuccess) {
        Address address = new Address(street, "00000", city);
        List<OrderItemRequest> itemRequests = new ArrayList<>();
        for (MenuItem item : basket.getItems()) {
            itemRequests.add(new OrderItemRequest(item.getId(), 1));
        }
        String orderId = orderApplicationService.placeOrder(
                name,
                restaurant.getId(),
                address,
                itemRequests,
                "PAYPAL",
                simulateSuccess
        );
        return orderId != null;
    }

    // Hilfsmethode: Bild für Restaurant-Namen
    private String getImageForRestaurant(String restaurantName) {
        return switch (restaurantName) {
            case "Burger Place" -> "burger.png";
            case "Pizza Paradiso" -> "pizza.png";
            case "Salat Oase" -> "salad.png";
            default -> "default.png";
        };
    }

    public static void main(String[] args) {
        launch();
    }
}
