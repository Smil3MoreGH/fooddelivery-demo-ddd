package com.fooddelivery.ordermanagement.domain;

// Value Object: Bestelltes Menü-Item mit Menge und Preis
public class OrderItem {
    private final String menuItemId; // Menü-Item-ID
    private final String name;       // Name des Items
    private final int quantity;      // Menge
    private final Money price;       // Einzelpreis

    public OrderItem(String menuItemId, String name, int quantity, Money price) {
        // Validierung der Felder
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

    // Getter (immutable)
    public String getMenuItemId() { return menuItemId; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public Money getPrice() { return price; }

    // Gesamtpreis für die Menge berechnen
    public Money getTotalPrice() {
        return price.multiply(quantity);
    }
}
