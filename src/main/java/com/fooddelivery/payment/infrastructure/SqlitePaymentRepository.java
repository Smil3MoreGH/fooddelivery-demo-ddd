package com.fooddelivery.payment.infrastructure;

import com.fooddelivery.payment.domain.*;

import java.sql.*;

// SQLite-Repository für Payments (Zahlungen)
public class SqlitePaymentRepository implements PaymentRepository {
    private static final String DB_PATH = "./payment.db";
    private Connection connection;

    public SqlitePaymentRepository() {
        connect();      // Verbindung zur Datenbank aufbauen
        createTable();  // Tabelle anlegen, falls nötig
    }

    // Verbindung zu SQLite herstellen
    private void connect() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
        } catch (SQLException e) {
            throw new RuntimeException("DB Connection failed: " + e.getMessage(), e);
        }
    }

    // Legt die Payment-Tabelle an, falls sie noch nicht existiert
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

    // Speichert oder aktualisiert ein Payment
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

    // Payment per ID finden
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

    // Payment per Order-ID finden
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

    // Hilfsmethode: DB-Zeile in Payment-Objekt mappen
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

        // Status korrekt setzen
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
        // processedAt: Ignoriert, falls kein Setter vorhanden (siehe Kommentar)
        return payment;
    }
}
