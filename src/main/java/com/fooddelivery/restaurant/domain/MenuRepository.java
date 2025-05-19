package com.fooddelivery.restaurant.domain;

// Menu: Domain Repository Interface
public interface MenuRepository {
    void save(Menu menu);
    Menu findById(String id);
    Menu findByRestaurantId(String restaurantId);
}