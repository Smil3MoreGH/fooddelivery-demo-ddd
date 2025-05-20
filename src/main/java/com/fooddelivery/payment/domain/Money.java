package com.fooddelivery.payment.domain;

// Value Object: Geldbetrag für Zahlungen (mit ISO-Währungscode)
public class Money {
    private final double amount;         // Betrag
    private final String currencyCode;   // Währung (ISO-Code, z.B. "EUR")

    public Money(double amount, String currencyCode) {
        this.amount = amount;
        this.currencyCode = currencyCode;
    }

    // Getter
    public double getAmount() { return amount; }
    public String getCurrencyCode() { return currencyCode; }
}
