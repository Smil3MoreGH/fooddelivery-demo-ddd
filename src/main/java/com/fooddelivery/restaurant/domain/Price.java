package com.fooddelivery.restaurant.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Price Value Object
public class Price {
    private final double value;
    private final String currency;

    public Price(double value, String currency) {
        if (value < 0)
            throw new IllegalArgumentException("Price cannot be negative");

        this.value = value;
        this.currency = currency;
    }

    public double getValue() { return value; }
    public String getCurrency() { return currency; }
}