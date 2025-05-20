package com.fooddelivery.restaurant.domain;

// Price Value Object (immutable): Preis eines Menü-Items
public class Price {
    private final double value;      // Betrag
    private final String currency;   // Währung (z.B. "EUR")

    public Price(double value, String currency) {
        if (value < 0)
            throw new IllegalArgumentException("Price cannot be negative");
        this.value = value;
        this.currency = currency;
    }

    // Getter
    public double getValue() { return value; }
    public String getCurrency() { return currency; }
}
