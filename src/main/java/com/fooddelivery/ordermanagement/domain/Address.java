package com.fooddelivery.ordermanagement.domain;

// Value Object für Adresse (ohne eigene ID)
public class Address {
    private final String street;   // Straße und Hausnummer
    private final String zipCode;  // 5-stellige PLZ
    private final String city;     // Stadt

    public Address(String street, String zipCode, String city) {
        // Validierung der Felder
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

    // Nur Getter (immutable)
    public String getStreet() { return street; }
    public String getZipCode() { return zipCode; }
    public String getCity() { return city; }

    // Gleichheit: basierend auf Attributen
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
