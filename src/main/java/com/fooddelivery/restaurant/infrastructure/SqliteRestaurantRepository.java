package com.fooddelivery.restaurant.infrastructure;

import com.fooddelivery.ordermanagement.domain.Address;
import com.fooddelivery.ordermanagement.domain.Restaurant;

import java.sql.*;
import java.util.*;

public class SqliteRestaurantRepository {
    private static final String DB_PATH = "./restaurant_menu.db";
    private Connection connection;

    public SqliteRestaurantRepository() {
        connect();
        createTable();
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
        } catch (SQLException e) {
            throw new RuntimeException("DB Connection failed: " + e.getMessage(), e);
        }
    }

    private void createTable() {
        String restaurantTable = """
            CREATE TABLE IF NOT EXISTS restaurants (
                id TEXT PRIMARY KEY,
                name TEXT,
                street TEXT
            );
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(restaurantTable);
        } catch (SQLException e) {
            throw new RuntimeException("Create table failed: " + e.getMessage(), e);
        }
    }

    // Speichert oder überschreibt ein Restaurant
    public void save(Restaurant restaurant) {
        String sql = """
            INSERT OR REPLACE INTO restaurants (id, name, street)
            VALUES (?, ?, ?);
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, restaurant.getId());
            pstmt.setString(2, restaurant.getName());
            pstmt.setString(3, restaurant.getAddress().getStreet());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Save restaurant failed: " + e.getMessage(), e);
        }
    }

    // Holt ein Restaurant anhand der ID
    public Restaurant findById(String id) {
        String sql = "SELECT * FROM restaurants WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToRestaurant(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById failed: " + e.getMessage(), e);
        }
        return null;
    }

    // Gibt alle Restaurants zurück (z.B. für das Demo-Menü)
    public List<Restaurant> findAll() {
        String sql = "SELECT * FROM restaurants";
        List<Restaurant> list = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapToRestaurant(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findAll failed: " + e.getMessage(), e);
        }
        return list;
    }

    private Restaurant mapToRestaurant(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String name = rs.getString("name");
        String street = rs.getString("street");
        // Adresse für Demo minimal
        Address address = new Address(street != null ? street : "Unbekannte Straße", "00000", "Unbekannt");
        return new Restaurant(id, name, address);
    }
}
