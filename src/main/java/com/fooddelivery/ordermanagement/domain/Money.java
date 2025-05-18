package com.fooddelivery.ordermanagement.domain;

// Value Object - represents money
public class Money {
    private final double amount;
    private final String currency;

    public Money(double amount, String currency) {
        if (amount < 0)
            throw new IllegalArgumentException("Amount cannot be negative");
        if (currency == null || currency.trim().isEmpty())
            throw new IllegalArgumentException("Currency cannot be empty");

        this.amount = amount;
        this.currency = currency;
    }

    public double getAmount() { return amount; }
    public String getCurrency() { return currency; }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency))
            throw new IllegalArgumentException("Cannot add different currencies");
        return new Money(this.amount + other.amount, this.currency);
    }

    public Money multiply(int multiplier) {
        return new Money(this.amount * multiplier, this.currency);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Money money = (Money) o;
        return Double.compare(money.amount, amount) == 0 &&
                currency.equals(money.currency);
    }
}
