package com.fooddelivery.demo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class LieferanDDDOSwingUI extends JFrame {
    private JPanel contextPanel; // "Card" area that changes
    private JPanel restaurantPanel;
    private JPanel menuAndBasketPanel;
    private JPanel menuPanel;
    private JPanel basketPanel;

    // Basket data structure (map: item name -> quantity)
    private Map<String, Integer> basket = new LinkedHashMap<>();
    private DefaultListModel<String> basketListModel = new DefaultListModel<>();
    private JLabel basketTotalLabel;

    // Example menu data (you would fetch from your DDD model)
    private Map<String, List<String>> menus = new HashMap<>(); // restaurant -> items
    private String currentRestaurant = null;

    public LieferanDDDOSwingUI() {
        super("LieferanDDDo");

        // --- Orange Title Bar ---
        JPanel orangeBar = new JPanel(new BorderLayout());
        orangeBar.setBackground(new Color(255, 128, 0));
        orangeBar.setPreferredSize(new java.awt.Dimension(100, 38));

        JLabel title = new JLabel("LieferanDDDo", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.BLACK);
        orangeBar.add(title, BorderLayout.CENTER);

        // --- Action Buttons Panel ---
        JPanel actionsPanel = new JPanel(new FlowLayout());
        JButton placeOrderBtn = new JButton("Place Order");
        JButton payOrderBtn = new JButton("Pay Order");
        JButton deliverBtn = new JButton("Delivery");
        JButton cancelBtn = new JButton("Cancel Order");
        actionsPanel.add(placeOrderBtn);
        actionsPanel.add(payOrderBtn);
        actionsPanel.add(deliverBtn);
        actionsPanel.add(cancelBtn);

        // --- Stack Title Bar and Actions Panel ---
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.add(orangeBar);
        northPanel.add(actionsPanel);

        add(northPanel, BorderLayout.NORTH);

        // --- Data Setup ---
        menus.put("PizzaHut", Arrays.asList("Pizza Margherita", "Pizza Salami", "Pizza Hawaii"));
        menus.put("BurgerKing", Arrays.asList("Cheeseburger", "Hamburger", "Cola"));
        menus.put("SalatPlace", Arrays.asList("Caesar Salad", "Greek Salad", "Vegan Bowl"));

        // --- Context Panel with CardLayout ---
        contextPanel = new JPanel(new CardLayout());

        // Restaurant Selection View
        restaurantPanel = new JPanel(new FlowLayout());
        for (String rest : menus.keySet()) {
            JButton btn = new JButton(rest);
            btn.addActionListener(e -> showMenuForRestaurant(rest));
            restaurantPanel.add(btn);
        }
        contextPanel.add(restaurantPanel, "restaurants");

        // Menu + Basket View
        menuAndBasketPanel = new JPanel(new GridLayout(1, 2));

        // Menu list (left)
        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuAndBasketPanel.add(menuPanel);

        // Basket (right)
        basketPanel = new JPanel();
        basketPanel.setLayout(new BorderLayout());
        basketListModel = new DefaultListModel<>();
        JList<String> basketList = new JList<>(basketListModel);
        basketTotalLabel = new JLabel("Total: 0€");
        JButton placeBasketOrderBtn = new JButton("Place Order");

        basketPanel.add(new JLabel("Basket"), BorderLayout.NORTH);
        basketPanel.add(new JScrollPane(basketList), BorderLayout.CENTER);
        JPanel basketBottom = new JPanel(new BorderLayout());
        basketBottom.add(basketTotalLabel, BorderLayout.NORTH);
        basketBottom.add(placeBasketOrderBtn, BorderLayout.SOUTH);
        basketPanel.add(basketBottom, BorderLayout.SOUTH);

        menuAndBasketPanel.add(basketPanel);
        contextPanel.add(menuAndBasketPanel, "menu");

        add(contextPanel, BorderLayout.CENTER);

        // Show the restaurant selection first
        ((CardLayout) contextPanel.getLayout()).show(contextPanel, "restaurants");

        // --- Button Logic ---
        // Place order from basket
        placeBasketOrderBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Order placed!\n" + basket);
            // TODO: Call your DDD application service here
            basket.clear();
            basketListModel.clear();
            basketTotalLabel.setText("Total: 0€");
            // Return to restaurant selection
            ((CardLayout) contextPanel.getLayout()).show(contextPanel, "restaurants");
        });

        // ... implement other top action buttons as needed

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    private void showMenuForRestaurant(String restaurant) {
        menuPanel.removeAll();
        basket.clear();
        basketListModel.clear();
        basketTotalLabel.setText("Total: 0€");
        currentRestaurant = restaurant;

        // For each menu item, create a button
        for (String item : menus.get(restaurant)) {
            JButton btn = new JButton(item);
            btn.setMaximumSize(new Dimension(180, 40));
            btn.addActionListener(e -> {
                basket.merge(item, 1, Integer::sum);
                updateBasket();
            });
            menuPanel.add(btn);
            menuPanel.add(Box.createVerticalStrut(10)); // Add spacing between buttons
        }

        menuPanel.revalidate();
        menuPanel.repaint();

        // Switch the context panel to the menu/basket view
        ((CardLayout)contextPanel.getLayout()).show(contextPanel, "menu");
    }
    private void updateBasket() {
        basketListModel.clear();
        int total = 0;
        for (Map.Entry<String, Integer> entry : basket.entrySet()) {
            basketListModel.addElement(entry.getKey() + " x" + entry.getValue());
            // TODO: Calculate price using your DDD model!
            total += entry.getValue() * 6; // Placeholder: 6€/item
        }
        basketTotalLabel.setText("Total: " + total + "€");
    }
}