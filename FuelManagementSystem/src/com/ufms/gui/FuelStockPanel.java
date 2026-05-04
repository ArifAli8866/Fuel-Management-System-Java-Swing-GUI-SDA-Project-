package com.ufms.gui;

import com.ufms.model.*;
import com.ufms.service.DataStore;
import com.ufms.util.UITheme;

import javax.swing.*;
import java.awt.*;

public class FuelStockPanel extends JPanel {

    private final User currentUser;
    private final DataStore db = DataStore.getInstance();
    private JLabel currentLevelLabel;
    private JLabel percentageLabel;
    private JProgressBar stockBar;

    public FuelStockPanel(User currentUser) {
        this.currentUser = currentUser;
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_MAIN);
        header.setBorder(BorderFactory.createEmptyBorder(24, 28, 8, 28));
        JLabel title = new JLabel("Fuel Stock Management");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(UITheme.BG_MAIN);

        JPanel card = new JPanel();
        card.setBackground(UITheme.BG_PANEL);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(28, 32, 28, 32)
        ));
        card.setPreferredSize(new Dimension(500, 0));

        FuelStock stock = db.getFuelStock();

        // Current level display
        currentLevelLabel = new JLabel(String.format("%.0f / %.0f L", stock.getCurrentLevel(), stock.getCapacity()));
        currentLevelLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        currentLevelLabel.setForeground(UITheme.PRIMARY);
        currentLevelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        percentageLabel = new JLabel(String.format("%.1f%% Full", stock.getPercentage()));
        percentageLabel.setFont(UITheme.FONT_BODY);
        percentageLabel.setForeground(UITheme.TEXT_MUTED);
        percentageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        stockBar = new JProgressBar(0, 100);
        stockBar.setValue((int) stock.getPercentage());
        stockBar.setStringPainted(true);
        double pct = stock.getPercentage();
        stockBar.setForeground(pct > 40 ? UITheme.SUCCESS : pct > 20 ? UITheme.WARNING : UITheme.DANGER);
        stockBar.setPreferredSize(new Dimension(0, 30));
        stockBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        stockBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(currentLevelLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(percentageLabel);
        card.add(Box.createVerticalStrut(16));
        card.add(stockBar);
        card.add(Box.createVerticalStrut(24));

        // Separator
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(sep);
        card.add(Box.createVerticalStrut(20));

        // Replenish section
        JLabel repTitle = new JLabel("Replenish Stock");
        repTitle.setFont(UITheme.FONT_SUBTITLE);
        repTitle.setForeground(UITheme.TEXT_DARK);
        repTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(repTitle);
        card.add(Box.createVerticalStrut(12));

        JTextField replenishField = UITheme.styledField();
        replenishField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        replenishField.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel repLabel = UITheme.fieldLabel("Quantity to Add (Liters) *");
        repLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(repLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(replenishField);
        card.add(Box.createVerticalStrut(12));

        JButton replenishBtn = UITheme.successButton("+ Add Stock");
        replenishBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(replenishBtn);
        card.add(Box.createVerticalStrut(24));

        // Settings section
        JSeparator sep2 = new JSeparator();
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep2.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(sep2);
        card.add(Box.createVerticalStrut(20));

        JLabel settingsTitle = new JLabel("Settings");
        settingsTitle.setFont(UITheme.FONT_SUBTITLE);
        settingsTitle.setForeground(UITheme.TEXT_DARK);
        settingsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(settingsTitle);
        card.add(Box.createVerticalStrut(12));

        JTextField priceField = UITheme.styledField();
        priceField.setText(String.format("%.2f", stock.getPricePerLiter()));
        priceField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        priceField.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel priceLabel = UITheme.fieldLabel("Price per Liter (Rs.)");
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(priceLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(priceField);
        card.add(Box.createVerticalStrut(12));

        JTextField thresholdField = UITheme.styledField();
        thresholdField.setText(String.format("%.0f", stock.getAlertThreshold()));
        thresholdField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        thresholdField.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel threshLabel = UITheme.fieldLabel("Alert Threshold (Liters)");
        threshLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(threshLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(thresholdField);
        card.add(Box.createVerticalStrut(12));

        JButton saveSettingsBtn = UITheme.primaryButton("Save Settings");
        saveSettingsBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(saveSettingsBtn);

        wrapper.add(card);
        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        replenishBtn.addActionListener(e -> {
            String qtyStr = replenishField.getText().trim();
            if (qtyStr.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter quantity.", "Error", JOptionPane.WARNING_MESSAGE); return; }
            double qty;
            try { qty = Double.parseDouble(qtyStr); if (qty <= 0) throw new NumberFormatException(); }
            catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Invalid quantity.", "Error", JOptionPane.WARNING_MESSAGE); return; }
            stock.replenish(qty);
            db.addAuditLog(currentUser.getUsername(), "Fuel stock replenished by " + qty + " L");
            JOptionPane.showMessageDialog(this, "✅ Stock updated. New level: " + String.format("%.0f L", stock.getCurrentLevel()), "Updated", JOptionPane.INFORMATION_MESSAGE);
            replenishField.setText("");
            refreshDisplay(stock);
        });

        saveSettingsBtn.addActionListener(e -> {
            try {
                double price = Double.parseDouble(priceField.getText().trim());
                double threshold = Double.parseDouble(thresholdField.getText().trim());
                if (price <= 0 || threshold < 0) throw new NumberFormatException();
                stock.setPricePerLiter(price);
                stock.setAlertThreshold(threshold);
                db.addAuditLog(currentUser.getUsername(), "Stock settings updated. Price: " + price + ", Threshold: " + threshold);
                JOptionPane.showMessageDialog(this, "Settings saved.", "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid values.", "Error", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    private void refreshDisplay(FuelStock stock) {
        currentLevelLabel.setText(String.format("%.0f / %.0f L", stock.getCurrentLevel(), stock.getCapacity()));
        percentageLabel.setText(String.format("%.1f%% Full", stock.getPercentage()));
        stockBar.setValue((int) stock.getPercentage());
        double pct = stock.getPercentage();
        stockBar.setForeground(pct > 40 ? UITheme.SUCCESS : pct > 20 ? UITheme.WARNING : UITheme.DANGER);
        stockBar.setString(String.format("%.1f%%", pct));
    }
}
