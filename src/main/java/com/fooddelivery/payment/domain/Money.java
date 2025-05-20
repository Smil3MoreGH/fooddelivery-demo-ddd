package com.fooddelivery.payment.domain;

//import java.time.LocalDateTime;
//import java.util.UUID;

// Notice different definition of Money in this context
public class Money {
    private final double amount;
    private final String currencyCode; // ISO currency code

    public Money(double amount, String currencyCode) {
        this.amount = amount;
        this.currencyCode = currencyCode;
    }

    public double getAmount() { return amount; }
    public String getCurrencyCode() { return currencyCode; }
}