package com.fooddelivery.ordermanagement.domain;

// Entity - has identity, can change over time
public class Restaurant {
    private final String id;
    private String name;
    private Address address;

    public Restaurant(String id, String name, Address address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Address getAddress() { return address; }

    public void updateName(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Name cannot be empty");
        this.name = name;
    }

    public void relocate(Address newAddress) {
        if (newAddress == null)
            throw new IllegalArgumentException("Address cannot be null");
        this.address = newAddress;
    }

    // Entities equality is based on identity
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Restaurant restaurant = (Restaurant) o;
        return id.equals(restaurant.id);
    }
}