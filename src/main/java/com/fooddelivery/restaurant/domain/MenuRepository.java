package com.fooddelivery.restaurant.domain;

public interface MenuRepository {
    void save(Menu menu);
    Menu findById(String id);
    Menu findByRestaurantId(String restaurantId);
}