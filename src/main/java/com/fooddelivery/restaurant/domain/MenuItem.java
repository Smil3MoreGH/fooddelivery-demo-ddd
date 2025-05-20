package com.fooddelivery.restaurant.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// MenuItem Entity: Einzelnes Menü-Item im Restaurant
public class MenuItem {
    private final String id;         // Item-ID
    private String name;             // Name des Gerichts
    private String description;      // Beschreibung
    private Price price;             // Preis (Value Object)
    private List<String> allergens;  // Allergene
    private boolean available;       // Verfügbarkeit

    public MenuItem(String id, String name, String description, Price price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.allergens = new ArrayList<>();
        this.available = true;
    }

    // Preis aktualisieren
    public void updatePrice(Price newPrice) {
        this.price = newPrice;
    }

    // Verfügbarkeit setzen
    public void setAvailability(boolean available) {
        this.available = available;
    }

    // Allergen hinzufügen
    public void addAllergen(String allergen) {
        allergens.add(allergen);
    }

    // Getter (unveränderbare Allergene-Liste)
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Price getPrice() { return price; }
    public List<String> getAllergens() { return Collections.unmodifiableList(allergens); }
    public boolean isAvailable() { return available; }
}
