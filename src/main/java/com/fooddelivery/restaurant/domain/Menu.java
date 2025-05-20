package com.fooddelivery.restaurant.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Menu Aggregate Root: Enthält Menü-Items für ein Restaurant
public class Menu {
    private final String id;              // Menü-ID
    private final String restaurantId;    // Zugehöriges Restaurant
    private final List<MenuItem> items;   // Liste aller Menü-Items

    public Menu(String id, String restaurantId) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.items = new ArrayList<>();
    }

    // Fügt ein neues Menü-Item hinzu (kein Duplikat erlaubt)
    public void addItem(MenuItem item) {
        for (MenuItem existingItem : items) {
            if (existingItem.getId().equals(item.getId())) {
                throw new IllegalArgumentException("Item with ID " + item.getId() + " already exists in the menu");
            }
        }
        items.add(item);
    }

    // Entfernt ein Menü-Item per ID
    public void removeItem(String itemId) {
        items.removeIf(item -> item.getId().equals(itemId));
    }

    // Gibt ein Menü-Item per ID zurück
    public MenuItem getItem(String itemId) {
        return items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElse(null);
    }

    // Gibt alle verfügbaren Items zurück
    public List<MenuItem> getAvailableItems() {
        return items.stream()
                .filter(MenuItem::isAvailable)
                .collect(java.util.stream.Collectors.toList());
    }

    // Getter
    public String getId() { return id; }
    public String getRestaurantId() { return restaurantId; }

    // Liefert eine unveränderbare Liste aller Items zurück
    public List<MenuItem> getItems() { return Collections.unmodifiableList(items); }
}
