package com.fooddelivery.restaurant.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// MenuItem Entity
public class MenuItem {
    private final String id;
    private String name;
    private String description;
    private Price price;
    private List<String> allergens;
    private boolean available;

    public MenuItem(String id, String name, String description, Price price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.allergens = new ArrayList<>();
        this.available = true;
    }

    public void updatePrice(Price newPrice) {
        this.price = newPrice;
    }

    public void setAvailability(boolean available) {
        this.available = available;
    }

    public void addAllergen(String allergen) {
        allergens.add(allergen);
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Price getPrice() { return price; }
    public List<String> getAllergens() { return Collections.unmodifiableList(allergens); }
    public boolean isAvailable() { return available; }
}