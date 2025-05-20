package com.fooddelivery.restaurant.infrastructure;

import com.fooddelivery.restaurant.domain.*;
import java.sql.*;

// SQLite-Repository für Menüs und Menü-Items
public class SqliteMenuRepository implements MenuRepository {
    private static final String DB_PATH = "./restaurant_menu.db";
    private Connection connection;

    public SqliteMenuRepository() {
        connect();        // Datenbankverbindung herstellen
        createTables();   // Tabellen anlegen, falls nicht vorhanden
    }

    // Verbindung zur SQLite-Datenbank aufbauen
    private void connect() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
        } catch (SQLException e) {
            throw new RuntimeException("DB Connection failed: " + e.getMessage(), e);
        }
    }

    // Menüs- und Menü-Items-Tabellen erstellen
    private void createTables() {
        String menuTable = """
            CREATE TABLE IF NOT EXISTS menus (
                id TEXT PRIMARY KEY,
                restaurant_id TEXT
            );
        """;
        String menuItemTable = """
            CREATE TABLE IF NOT EXISTS menu_items (
                id TEXT PRIMARY KEY,
                menu_id TEXT,
                name TEXT,
                description TEXT,
                price DOUBLE,
                currency TEXT,
                available INTEGER
            );
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(menuTable);
            stmt.execute(menuItemTable);
        } catch (SQLException e) {
            throw new RuntimeException("Create tables failed: " + e.getMessage(), e);
        }
    }

    // Menü inkl. Items speichern (Items werden vorher gelöscht)
    @Override
    public void save(Menu menu) {
        // Menü speichern
        String sqlMenu = """
            INSERT OR REPLACE INTO menus (id, restaurant_id)
            VALUES (?, ?);
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sqlMenu)) {
            pstmt.setString(1, menu.getId());
            pstmt.setString(2, menu.getRestaurantId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Save menu failed: " + e.getMessage(), e);
        }

        // Vorherige Items löschen
        String deleteItems = "DELETE FROM menu_items WHERE menu_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteItems)) {
            pstmt.setString(1, menu.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Delete old items failed: " + e.getMessage(), e);
        }

        // Neue Items einfügen
        String sqlItem = """
            INSERT INTO menu_items (id, menu_id, name, description, price, currency, available)
            VALUES (?, ?, ?, ?, ?, ?, ?);
        """;
        for (MenuItem item : menu.getItems()) {
            try (PreparedStatement pstmt = connection.prepareStatement(sqlItem)) {
                pstmt.setString(1, item.getId());
                pstmt.setString(2, menu.getId());
                pstmt.setString(3, item.getName());
                pstmt.setString(4, item.getDescription());
                pstmt.setDouble(5, item.getPrice().getValue());
                pstmt.setString(6, item.getPrice().getCurrency());
                pstmt.setInt(7, item.isAvailable() ? 1 : 0);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Save menu item failed: " + e.getMessage(), e);
            }
        }
    }

    // Menü per ID finden
    @Override
    public Menu findById(String id) {
        String sqlMenu = "SELECT * FROM menus WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sqlMenu)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToMenu(rs.getString("id"), rs.getString("restaurant_id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById failed: " + e.getMessage(), e);
        }
        return null;
    }

    // Menü per Restaurant-ID finden
    @Override
    public Menu findByRestaurantId(String restaurantId) {
        String sqlMenu = "SELECT * FROM menus WHERE restaurant_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sqlMenu)) {
            pstmt.setString(1, restaurantId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToMenu(rs.getString("id"), restaurantId);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByRestaurantId failed: " + e.getMessage(), e);
        }
        return null;
    }

    // Hilfsmethode: Menü inkl. Items aus DB zusammenbauen
    private Menu mapToMenu(String menuId, String restaurantId) {
        Menu menu = new Menu(menuId, restaurantId);

        String sqlItems = "SELECT * FROM menu_items WHERE menu_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sqlItems)) {
            pstmt.setString(1, menuId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    MenuItem item = new MenuItem(
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            new Price(rs.getDouble("price"), rs.getString("currency"))
                    );
                    item.setAvailability(rs.getInt("available") == 1);
                    menu.addItem(item);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("mapToMenu failed: " + e.getMessage(), e);
        }
        return menu;
    }
}
