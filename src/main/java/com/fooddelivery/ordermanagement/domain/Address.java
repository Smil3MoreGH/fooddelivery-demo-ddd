package com.fooddelivery.ordermanagement.domain;

//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.UUID;

// Value Object - represents a concept with no identity
public class Address {
    private final String street;
    private final String zipCode;
    private final String city;

    public Address(String street, String zipCode, String city) {
        // Validate invariants
        if (street == null || street.trim().isEmpty())
            throw new IllegalArgumentException("Street cannot be empty");
        if (zipCode == null || !zipCode.matches("\\d{5}"))
            throw new IllegalArgumentException("Zip code must be 5 digits");
        if (city == null || city.trim().isEmpty())
            throw new IllegalArgumentException("City cannot be empty");

        this.street = street;
        this.zipCode = zipCode;
        this.city = city;
    }

    // Value Objects are immutable - only getters, no setters
    public String getStreet() { return street; }
    public String getZipCode() { return zipCode; }
    public String getCity() { return city; }

    // Value Objects equality is based on attributes
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;
        return street.equals(address.street) &&
                zipCode.equals(address.zipCode) &&
                city.equals(address.city);
    }

    @Override
    public int hashCode() {
        int result = street.hashCode();
        result = 31 * result + zipCode.hashCode();
        result = 31 * result + city.hashCode();
        return result;
    }
}