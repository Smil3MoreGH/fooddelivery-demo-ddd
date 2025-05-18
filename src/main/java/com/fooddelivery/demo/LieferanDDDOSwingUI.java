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
        JButton placeOrderBtn = createStyledButton("Place Order");
        JButton payOrderBtn = createStyledButton("Pay Order");
        JButton deliverBtn = createStyledButton("Delivery");
        JButton cancelBtn = createStyledButton("Cancel Order");
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
        restaurantPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        for (String rest : menus.keySet()) {
            JButton btn = createStyledButton(rest);
            btn.setPreferredSize(new Dimension(180, 50)); // Optional: makes all buttons same size
            btn.addActionListener(e -> showMenuForRestaurant(rest));
            restaurantPanel.add(btn);
        }

        // Wrapper panel to center restaurantPanel vertically and horizontally
        JPanel wrapperPanel = new JPanel();
        wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.Y_AXIS));
        wrapperPanel.add(Box.createVerticalGlue());    // Pushes down
        wrapperPanel.add(restaurantPanel);
        wrapperPanel.add(Box.createVerticalGlue());    // Pushes up

        // Make sure the buttons are centered horizontally too
        restaurantPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contextPanel.add(wrapperPanel, "restaurants");

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
        JButton placeBasketOrderBtn = createStyledButton("Place Order");

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
            JButton btn = createStyledButton(item);
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

    private JButton createStyledButton(String text) {
        JButton button = new RoundedButton(text, new Color(255, 128, 0));
        // No need for MouseListener—the hover color is in the paintComponent!
        return button;
    }

    private static class RoundedButton extends JButton {
        private final Color backgroundColor;

        public RoundedButton(String text, Color bgColor) {
            super(text);
            this.backgroundColor = bgColor;
            setFocusPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.BOLD, 15));
            setContentAreaFilled(false); // We'll handle background painting
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(10, 22, 10, 22));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isRollover() ? new Color(255, 150, 40) : backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        public void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.GRAY); // Use a light or dark grey as you prefer
            g2.setStroke(new BasicStroke(3)); // Thicker border (optional)
            g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 24, 24);
            g2.dispose();
        }

        @Override
        public boolean isContentAreaFilled() {
            return false; // We paint it ourselves
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LieferanDDDOSwingUI().setVisible(true));
    }
}