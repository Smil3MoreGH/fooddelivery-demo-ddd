package com.fooddelivery.ordermanagement.infrastructure;

import com.fooddelivery.ordermanagement.domain.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SqliteOrderRepository implements OrderRepository {
    private static final String DB_PATH = "./order_management.db";
    private Connection connection;

    public SqliteOrderRepository() {
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
        String orderTable = """
            CREATE TABLE IF NOT EXISTS orders (
                id TEXT PRIMARY KEY,
                customer_id TEXT,
                restaurant_id TEXT,
                street TEXT,
                status TEXT,
                is_paid INTEGER,
                created_at DATETIME
            );
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(orderTable);
        } catch (SQLException e) {
            throw new RuntimeException("Create table failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Order order) {
        String sql = """
            INSERT OR REPLACE INTO orders 
            (id, customer_id, restaurant_id, street, status, is_paid, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?);
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, order.getId());
            pstmt.setString(2, order.getCustomerId());
            pstmt.setString(3, order.getRestaurantId());
            pstmt.setString(4, order.getDeliveryAddress().getStreet());
            pstmt.setString(5, order.getStatus().toString());
            pstmt.setInt(6, order.isPaid() ? 1 : 0);
            pstmt.setString(7, order.getCreatedAt().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Save order failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Order findById(String id) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToOrder(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById failed: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        String sql = "SELECT * FROM orders WHERE customer_id = ?";
        List<Order> result = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapToOrder(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByCustomerId failed: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public List<Order> findByRestaurantId(String restaurantId) {
        String sql = "SELECT * FROM orders WHERE restaurant_id = ?";
        List<Order> result = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, restaurantId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapToOrder(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByRestaurantId failed: " + e.getMessage(), e);
        }
        return result;
    }

    // Helper to map a ResultSet row to an Order domain object
    private Order mapToOrder(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String customerId = rs.getString("customer_id");
        String restaurantId = rs.getString("restaurant_id");
        String street = rs.getString("street");
        String statusStr = rs.getString("status");
        boolean isPaid = rs.getInt("is_paid") == 1;
        String createdAtStr = rs.getString("created_at");

        // Default values for missing address fields
        Address address = new Address(
                street != null ? street : "Unbekannte Straße",
                "00000",
                "Unbekannt"
        );

        OrderStatus status = OrderStatus.valueOf(statusStr);
        LocalDateTime createdAt = LocalDateTime.parse(createdAtStr);

        // Für Demo: Items bleiben leer, DomainEvents werden ignoriert
        Order order = new Order(id, customerId, restaurantId, address);
        // Status etc. nachträglich setzen
        if (status != OrderStatus.CREATED) {
            switch (status) {
                case CONFIRMED -> order.confirm();
                case PREPARING -> {
                    order.confirm();
                    order.markAsPaid();
                    order.startPreparation();
                }
                case READY_FOR_DELIVERY -> {
                    order.confirm();
                    order.markAsPaid();
                    order.startPreparation();
                    order.readyForDelivery();
                }
                case OUT_FOR_DELIVERY -> {
                    order.confirm();
                    order.markAsPaid();
                    order.startPreparation();
                    order.readyForDelivery();
                    order.startDelivery();
                }
                case DELIVERED -> {
                    order.confirm();
                    order.markAsPaid();
                    order.startPreparation();
                    order.readyForDelivery();
                    order.startDelivery();
                    order.complete();
                }
                case CANCELLED -> order.cancel();
            }
        }
        if (isPaid) order.markAsPaid();

        // createdAt nachträglich setzen (Workaround für Demo, ggf. Setter hinzufügen)
        // In deiner Domain-Order gibt es keinen Setter, also bleibt es beim aktuellen Zeitpunkt

        return order;
    }
}
