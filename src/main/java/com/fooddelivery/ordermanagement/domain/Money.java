package com.fooddelivery.ordermanagement.domain;

// Value Object für Geldbetrag
public class Money {
    private final double amount;     // Betrag
    private final String currency;   // Währung

    public Money(double amount, String currency) {
        if (amount < 0)
            throw new IllegalArgumentException("Amount cannot be negative");
        if (currency == null || currency.trim().isEmpty())
            throw new IllegalArgumentException("Currency cannot be empty");
        this.amount = amount;
        this.currency = currency;
    }

    // Getter (immutable)
    public double getAmount() { return amount; }
    public String getCurrency() { return currency; }

    // Addiert zwei Beträge (nur gleiche Währung!)
    public Money add(Money other) {
        if (!this.currency.equals(other.currency))
            throw new IllegalArgumentException("Cannot add different currencies");
        return new Money(this.amount + other.amount, this.currency);
    }

    // Multipliziert Betrag mit Faktor
    public Money multiply(int multiplier) {
        return new Money(this.amount * multiplier, this.currency);
    }

    // Gleichheit: Betrag und Währung
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Money money = (Money) o;
        return Double.compare(money.amount, amount) == 0 &&
                currency.equals(money.currency);
    }
}
