package com.ufms.gui;

import com.ufms.model.*;
import com.ufms.service.DataStore;
import com.ufms.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel {

    private final User currentUser;
    private final DataStore db = DataStore.getInstance();

    public DashboardPanel(User currentUser) {
        this.currentUser = currentUser;
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        // ── Page Header ──────────────────────────────────────────────────────
        JPanel pageHeader = new JPanel(new BorderLayout());
        pageHeader.setBackground(UITheme.BG_MAIN);
        pageHeader.setBorder(BorderFactory.createEmptyBorder(24, 28, 8, 28));

        JLabel title = new JLabel("Dashboard");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Welcome back, " + currentUser.getFullName());
        subtitle.setFont(UITheme.FONT_BODY);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        JPanel headerText = new JPanel();
        headerText.setOpaque(false);
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.add(title);
        headerText.add(subtitle);
        pageHeader.add(headerText, BorderLayout.WEST);

        add(pageHeader, BorderLayout.NORTH);

        // ── Scroll Content ───────────────────────────────────────────────────
        JPanel content = new JPanel();
        content.setBackground(UITheme.BG_MAIN);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(10, 28, 28, 28));

        // Stat Cards Row
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        statsRow.add(makeStatCard(
            String.valueOf(db.getAllVehicles().size()), "Total Vehicles", UITheme.PRIMARY
        ));
        statsRow.add(makeStatCard(
            String.valueOf(db.getPendingRequests().size()), "Pending Requests", UITheme.WARNING
        ));
        statsRow.add(makeStatCard(
            String.format("%.0f L", db.getTotalFuelDispensed()), "Total Fuel Dispensed", UITheme.SUCCESS
        ));
        statsRow.add(makeStatCard(
            String.format("Rs. %.0f", db.getTotalCost()), "Total Expenditure", UITheme.DANGER
        ));

        content.add(statsRow);
        content.add(Box.createVerticalStrut(20));

        // Stock Alert row
        FuelStock stock = db.getFuelStock();
        if (stock.isLow()) {
            JPanel alert = new JPanel(new FlowLayout(FlowLayout.LEFT));
            alert.setBackground(new Color(255, 243, 205));
            alert.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.WARNING),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
            ));
            alert.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            JLabel alertMsg = new JLabel("⚠️  LOW FUEL STOCK ALERT: Current stock is " +
                String.format("%.0f L (%.1f%%)", stock.getCurrentLevel(), stock.getPercentage()) +
                " — Please arrange replenishment.");
            alertMsg.setFont(new Font("Segoe UI", Font.BOLD, 13));
            alertMsg.setForeground(new Color(133, 77, 14));
            alert.add(alertMsg);
            content.add(alert);
            content.add(Box.createVerticalStrut(14));
        }

        // Two-column row: Pending Requests table + Fuel Stock card
        JPanel midRow = new JPanel(new GridLayout(1, 2, 16, 0));
        midRow.setOpaque(false);

        midRow.add(buildPendingRequestsCard());
        midRow.add(buildFuelStockCard(stock));

        content.add(midRow);
        content.add(Box.createVerticalStrut(16));

        // Recent transactions
        content.add(buildRecentTransactionsCard());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel makeStatCard(String value, String label, Color color) {
        JPanel card = new JPanel();
        card.setBackground(UITheme.BG_PANEL);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        JLabel top = new JLabel(value);
        top.setFont(new Font("Segoe UI", Font.BOLD, 24));
        top.setForeground(color);
        top.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel bot = new JLabel(label);
        bot.setFont(UITheme.FONT_SMALL);
        bot.setForeground(UITheme.TEXT_MUTED);
        bot.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(top);
        card.add(Box.createVerticalStrut(4));
        card.add(bot);
        return card;
    }

    private JPanel buildPendingRequestsCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UITheme.BG_PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel title = new JLabel("Pending Fuel Requests");
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.TEXT_DARK);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        card.add(title, BorderLayout.NORTH);

        List<FuelRequest> pending = db.getPendingRequests();
        String[] cols = {"ID", "Vehicle", "Driver", "Qty (L)", "Date"};
        Object[][] data = new Object[pending.size()][5];
        for (int i = 0; i < pending.size(); i++) {
            FuelRequest r = pending.get(i);
            data[i][0] = "#" + r.getRequestId();
            Vehicle v = db.getVehicleById(r.getVehicleId());
            data[i][1] = v != null ? v.getRegistrationNumber() : "N/A";
            User d = db.getUserById(r.getDriverId());
            data[i][2] = d != null ? d.getFullName() : "N/A";
            data[i][3] = r.getEstimatedQuantity();
            data[i][4] = r.getRequestDateFormatted();
        }
        JTable table = new JTable(data, cols) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        UITheme.styleTable(table);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildFuelStockCard(FuelStock stock) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UITheme.BG_PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel title = new JLabel("Fuel Stock Overview");
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.TEXT_DARK);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        card.add(title, BorderLayout.NORTH);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        double pct = stock.getPercentage();
        Color barColor = pct > 40 ? UITheme.SUCCESS : pct > 20 ? UITheme.WARNING : UITheme.DANGER;

        addInfoRow(info, "Current Level:", String.format("%.0f L", stock.getCurrentLevel()), barColor);
        addInfoRow(info, "Total Capacity:", String.format("%.0f L", stock.getCapacity()), UITheme.TEXT_DARK);
        addInfoRow(info, "Alert Threshold:", String.format("%.0f L", stock.getAlertThreshold()), UITheme.TEXT_MUTED);
        addInfoRow(info, "Price per Liter:", String.format("Rs. %.2f", stock.getPricePerLiter()), UITheme.TEXT_DARK);

        info.add(Box.createVerticalStrut(14));

        // Stock percentage bar
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue((int) pct);
        bar.setStringPainted(true);
        bar.setString(String.format("%.1f%% Full", pct));
        bar.setForeground(barColor);
        bar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bar.setPreferredSize(new Dimension(0, 28));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        info.add(bar);

        card.add(info, BorderLayout.CENTER);
        return card;
    }

    private void addInfoRow(JPanel panel, String label, String value, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setForeground(UITheme.TEXT_MUTED);
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 13));
        val.setForeground(valueColor);
        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        panel.add(row);
    }

    private JPanel buildRecentTransactionsCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UITheme.BG_PANEL);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel title = new JLabel("Recent Transactions");
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.TEXT_DARK);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        card.add(title, BorderLayout.NORTH);

        List<FuelTransaction> txns = db.getAllTransactions();
        int count = Math.min(txns.size(), 5);
        String[] cols = {"Txn #", "Vehicle", "Qty (L)", "Cost (Rs)", "Date"};
        Object[][] data = new Object[count][5];
        for (int i = 0; i < count; i++) {
            FuelTransaction t = txns.get(txns.size() - 1 - i); // latest first
            data[i][0] = "#" + t.getTransactionId();
            Vehicle v = db.getVehicleById(t.getVehicleId());
            data[i][1] = v != null ? v.getRegistrationNumber() : "N/A";
            data[i][2] = String.format("%.1f", t.getQuantityDispensed());
            data[i][3] = String.format("%.2f", t.getTotalCost());
            data[i][4] = t.getTransactionDateFormatted();
        }

        JTable table = new JTable(data, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        UITheme.styleTable(table);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }
}
