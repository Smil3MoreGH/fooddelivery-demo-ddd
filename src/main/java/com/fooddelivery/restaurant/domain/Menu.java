package com.fooddelivery.restaurant.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Menu Aggregate Root
public class Menu {
    private final String id;
    private final String restaurantId;
    private final List<MenuItem> items;

    public Menu(String id, String restaurantId) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.items = new ArrayList<>();
    }
    // Adds a MenuItem to the Menu after checking for duplicate IDs
    public void addItem(MenuItem item) {
        // Check for duplicate item IDs
        for (MenuItem existingItem : items) {
            if (existingItem.getId().equals(item.getId())) {
                throw new IllegalArgumentException("Item with ID " + item.getId() + " already exists in the menu");
            }
        }

        items.add(item);
    }

    // Removes item by ID
    public void removeItem(String itemId) {
        items.removeIf(item -> item.getId().equals(itemId));
    }

    // Get a specific menu item by id
    public MenuItem getItem(String itemId) {
        return items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElse(null);
    }

    // Get all available items
    public List<MenuItem> getAvailableItems() {
        return items.stream()
                .filter(MenuItem::isAvailable)
                .collect(java.util.stream.Collectors.toList());
    }

    // Getters
    public String getId() { return id; }
    public String getRestaurantId() { return restaurantId; }

    // prevennts external mutation
    public List<MenuItem> getItems() { return Collections.unmodifiableList(items); }
}