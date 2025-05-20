package com.fooddelivery.payment.infrastructure;

import com.fooddelivery.payment.domain.*;

import java.sql.*;
//import java.time.LocalDateTime;
//import java.util.*;

public class SqlitePaymentRepository implements PaymentRepository {
    private static final String DB_PATH = "./payment.db";
    private Connection connection;

    public SqlitePaymentRepository() {
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
        String paymentTable = """
            CREATE TABLE IF NOT EXISTS payments (
                id TEXT PRIMARY KEY,
                order_id TEXT,
                amount DOUBLE,
                currency TEXT,
                status TEXT,
                payment_method TEXT,
                transaction_reference TEXT,
                processed_at TEXT
            );
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(paymentTable);
        } catch (SQLException e) {
            throw new RuntimeException("Create table failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Payment payment) {
        String sql = """
            INSERT OR REPLACE INTO payments
            (id, order_id, amount, currency, status, payment_method, transaction_reference, processed_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?);
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, payment.getId());
            pstmt.setString(2, payment.getOrderId());
            pstmt.setDouble(3, payment.getAmount().getAmount());
            pstmt.setString(4, payment.getAmount().getCurrencyCode());
            pstmt.setString(5, payment.getStatus().toString());
            pstmt.setString(6, payment.getPaymentMethod());
            pstmt.setString(7, payment.getTransactionReference());
            pstmt.setString(8, payment.getProcessedAt() != null ? payment.getProcessedAt().toString() : null);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Save payment failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Payment findById(String id) {
        String sql = "SELECT * FROM payments WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToPayment(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById failed: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Payment findByOrderId(String orderId) {
        String sql = "SELECT * FROM payments WHERE order_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToPayment(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByOrderId failed: " + e.getMessage(), e);
        }
        return null;
    }

    // Helper zum Mappen eines ResultSet auf dein Payment-Domain-Objekt
    private Payment mapToPayment(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String orderId = rs.getString("order_id");
        double amount = rs.getDouble("amount");
        String currency = rs.getString("currency");
        String statusStr = rs.getString("status");
        String paymentMethod = rs.getString("payment_method");
        String transactionReference = rs.getString("transaction_reference");
        String processedAtStr = rs.getString("processed_at");

        Money money = new Money(amount, currency);
        Payment payment = new Payment(id, orderId, money, paymentMethod);

        // Status setzen
        PaymentStatus status = PaymentStatus.valueOf(statusStr);
        switch (status) {
            case COMPLETED -> payment.markAsCompleted(transactionReference);
            case FAILED -> payment.markAsFailed();
            case REFUNDED -> {
                payment.markAsCompleted(transactionReference);
                payment.refund();
            }
            default -> {
                // nothing, already PENDING
            }
        }
        // processedAt kann beim Markieren gesetzt werden; wir überschreiben es hier ggf.
        if (processedAtStr != null && !processedAtStr.isEmpty()) {
            try {
                // Nur wenn du in Payment ein Setter für processedAt hast – ansonsten ignorieren.
                // LocalDateTime processedAt = LocalDateTime.parse(processedAtStr);
            } catch (Exception ignored) {}
        }
        return payment;
    }
}
