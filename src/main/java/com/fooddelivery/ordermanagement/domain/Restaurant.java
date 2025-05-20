package com.fooddelivery.ordermanagement.domain;

// Entity: Restaurant (hat eine eigene ID, kann sich ändern)
public class Restaurant {
    private final String id;      // Restaurant-ID
    private String name;          // Name des Restaurants
    private Address address;      // Adresse

    public Restaurant(String id, String name, Address address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    // Getter
    public String getId() { return id; }
    public String getName() { return name; }
    public Address getAddress() { return address; }

    // Name ändern
    public void updateName(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Name cannot be empty");
        this.name = name;
    }

    // Adresse ändern
    public void relocate(Address newAddress) {
        if (newAddress == null)
            throw new IllegalArgumentException("Address cannot be null");
        this.address = newAddress;
    }

    // Gleichheit basiert auf der ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Restaurant restaurant = (Restaurant) o;
        return id.equals(restaurant.id);
    }
}
