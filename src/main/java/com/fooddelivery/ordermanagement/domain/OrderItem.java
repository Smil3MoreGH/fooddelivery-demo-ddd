package com.fooddelivery.ordermanagement.domain;

// Value Object - represents an ordered menu item with quantity
public class OrderItem {
    private final String menuItemId;
    private final String name;
    private final int quantity;
    private final Money price; // Another Value Object

    public OrderItem(String menuItemId, String name, int quantity, Money price) {
        if (menuItemId == null || menuItemId.trim().isEmpty())
            throw new IllegalArgumentException("Menu item ID cannot be empty");
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Name cannot be empty");
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be positive");
        if (price == null)
            throw new IllegalArgumentException("Price cannot be null");

        this.menuItemId = menuItemId;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public String getMenuItemId() { return menuItemId; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public Money getPrice() { return price; }

    public Money getTotalPrice() {
        return price.multiply(quantity);
    }
}