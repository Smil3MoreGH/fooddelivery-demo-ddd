package com.fooddelivery.demo;

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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

// --- Einfache Basket-Klasse (du kannst sie in eine eigene Datei auslagern!) ---
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

    @Override
    public void start(Stage stage) {
        showRestaurantList(stage);
    }

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
            // Beim Klick: Menü-Seite öffnen!
            card.setOnMouseClicked(e -> openMenuPage(stage, restaurant));
            cardList.getChildren().add(card);
        }

        root.getChildren().add(cardList);

        Scene scene = new Scene(root, 430, 840);
        scene.getStylesheets().add(getClass().getResource("/mobile-food.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Food Delivery Demo");
        stage.show();
    }

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

        // Warenkorb unten
        VBox basketBox = new VBox(10);
        basketBox.setPadding(new Insets(15, 0, 0, 0));
        basketBox.setAlignment(Pos.TOP_CENTER);
        basketBox.setStyle("-fx-background-color: #f6f6f6; -fx-border-radius: 10; -fx-background-radius: 10;");
        updateBasketBox(basketBox, stage, restaurant);

        // Hier sorgt das dafür, dass menuList den ganzen Platz nimmt, und basketBox immer unten bleibt!
        VBox.setVgrow(menuList, Priority.ALWAYS);

        root.getChildren().addAll(menuList, basketBox);

        javafx.scene.control.Button back = new javafx.scene.control.Button("Zurück");
        back.setOnAction(e -> showRestaurantList(stage));
        root.getChildren().add(back);

        Scene menuScene = new Scene(root, 430, 840);
        menuScene.getStylesheets().add(getClass().getResource("/mobile-food.css").toExternalForm());
        stage.setScene(menuScene);
    }


    // **Card-Design für Menü-Item**
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

        // Dies ist wichtig:
        card.setOnMouseClicked(e -> {
            basket.addItem(item);
            // Finde das basketBox-Element sauber im SceneGraph (über die Parent-Chain):
            VBox root = (VBox) card.getParent().getParent();
            VBox basketBox = (VBox) root.getChildren().get(root.getChildren().size() - 2); // Vorletztes Element ist basketBox
            updateBasketBox(basketBox, stage, restaurant);
        });

        return card;
    }


    // **Warenkorb aktualisieren/anzeigen**
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

        // --- Bestellen-Button ---
        javafx.scene.control.Button orderBtn = new javafx.scene.control.Button("Bestellen");
        orderBtn.setOnAction(e -> openOrderPage(stage, restaurant));

        basketBox.getChildren().addAll(basketTitle, items, totalLabel, clearBtn);
        if (!basket.getItems().isEmpty()) {
            basketBox.getChildren().add(orderBtn); // Nur anzeigen, wenn was drin ist!
        }
    }



    private HBox createRestaurantCard(String imgFile, String name, String address, String tag, String time, String chip, String chipColor) {
        HBox card = new HBox(22);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER_LEFT);

        ImageView img;
        try {
            img = new ImageView(new Image(getClass().getResourceAsStream("/" + imgFile)));
        } catch (Exception e) {
            img = new ImageView(); // Leeres Bild
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

    private void openOrderPage(Stage stage, Restaurant restaurant) {
        VBox root = new VBox(18);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Bestellung abschließen");
        title.setStyle("-fx-font-size:1.4em; -fx-font-weight: bold;");
        root.getChildren().add(title);

        // Felder für Adresse usw.
        javafx.scene.control.TextField nameField = new javafx.scene.control.TextField();
        nameField.setPromptText("Name");

        javafx.scene.control.TextField streetField = new javafx.scene.control.TextField();
        streetField.setPromptText("Straße und Hausnummer");

        javafx.scene.control.TextField cityField = new javafx.scene.control.TextField();
        cityField.setPromptText("PLZ und Ort");

        // Optional: weitere Felder wie Telefonnummer, Zahlungsart...

        javafx.scene.control.Button submitBtn = new javafx.scene.control.Button("Bestellung absenden");
        submitBtn.setOnAction(e -> {
            // HIER: Validierung und Order speichern!
            // Beispiel:
            String name = nameField.getText();
            String street = streetField.getText();
            String city = cityField.getText();

            if (name.isBlank() || street.isBlank() || city.isBlank()) {
                // Zeige eine Warnung
                root.getChildren().add(new Label("Bitte alle Felder ausfüllen!"));
            } else {
                // TODO: Bestellung speichern (in DB) und ggf. Payment
                // Dann ggf. Danke-Seite anzeigen oder zurück
                root.getChildren().clear();
                Label done = new Label("Vielen Dank für deine Bestellung!");
                done.setStyle("-fx-font-size:1.2em; -fx-font-weight:600;");
                root.getChildren().add(done);
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
