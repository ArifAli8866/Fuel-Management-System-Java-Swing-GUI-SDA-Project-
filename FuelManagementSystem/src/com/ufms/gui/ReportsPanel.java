package com.ufms.gui;

import com.ufms.model.*;
import com.ufms.service.DataStore;
import com.ufms.util.UITheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use Case UC-004: Generate Reports
 */
public class ReportsPanel extends JPanel {

    private final User currentUser;
    private final DataStore db = DataStore.getInstance();
    private JTable table;
    private DefaultTableModel model;
    private JTextArea summaryArea;

    public ReportsPanel(User currentUser) {
        this.currentUser = currentUser;
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_MAIN);
        header.setBorder(BorderFactory.createEmptyBorder(24, 28, 8, 28));
        JLabel title = new JLabel("Reports & Analytics");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setBorder(BorderFactory.createEmptyBorder(0, 28, 28, 28));
        split.setDividerLocation(220);
        split.setBackground(UITheme.BG_MAIN);

        // ── Left: Report type buttons ────────────────────────────────────────
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(UITheme.BG_PANEL);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(16, 12, 16, 12)
        ));

        JLabel reportTypeLbl = new JLabel("Report Type");
        reportTypeLbl.setFont(UITheme.FONT_SUBTITLE);
        reportTypeLbl.setForeground(UITheme.TEXT_DARK);
        reportTypeLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(reportTypeLbl);
        leftPanel.add(Box.createVerticalStrut(12));

        String[] reportTypes = {
            "📋  All Transactions",
            "🚌  Vehicle-wise Report",
            "👤  Driver-wise Report",
            "💰  Cost Summary",
            "⛽  Fuel Efficiency"
        };

        for (String type : reportTypes) {
            JButton btn = new JButton(type);
            btn.setFont(UITheme.FONT_BODY);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setBackground(UITheme.BG_PANEL);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(UITheme.TABLE_HEADER); }
                public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(UITheme.BG_PANEL); }
            });
            btn.addActionListener(e -> generateReport(type));
            leftPanel.add(btn);
            leftPanel.add(Box.createVerticalStrut(4));
        }
        leftPanel.add(Box.createVerticalGlue());

        // ── Right: Report output ─────────────────────────────────────────────
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setBackground(UITheme.BG_MAIN);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        summaryArea = new JTextArea(4, 0);
        summaryArea.setEditable(false);
        summaryArea.setFont(UITheme.FONT_MONO);
        summaryArea.setBackground(new Color(240, 244, 250));
        summaryArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        summaryArea.setText("← Select a report type from the left panel.");

        String[] cols = {"#", "Col1", "Col2", "Col3", "Col4", "Col5"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        UITheme.styleTable(table);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        rightPanel.add(summaryArea, BorderLayout.NORTH);
        rightPanel.add(sp, BorderLayout.CENTER);

        split.setLeftComponent(leftPanel);
        split.setRightComponent(rightPanel);
        add(split, BorderLayout.CENTER);
    }

    private void generateReport(String type) {
        model.setRowCount(0);

        if (type.contains("All Transactions")) {
            generateAllTransactions();
        } else if (type.contains("Vehicle-wise")) {
            generateVehicleReport();
        } else if (type.contains("Driver-wise")) {
            generateDriverReport();
        } else if (type.contains("Cost Summary")) {
            generateCostSummary();
        } else if (type.contains("Fuel Efficiency")) {
            generateEfficiencyReport();
        }
    }

    private void generateAllTransactions() {
        List<FuelTransaction> txns = db.getAllTransactions();
        updateColumns(new String[]{"Txn #", "Vehicle", "Qty (L)", "Cost (Rs)", "Date"});
        for (FuelTransaction t : txns) {
            Vehicle v = db.getVehicleById(t.getVehicleId());
            model.addRow(new Object[]{
                "#" + t.getTransactionId(),
                v != null ? v.getRegistrationNumber() : "N/A",
                String.format("%.1f", t.getQuantityDispensed()),
                String.format("%.2f", t.getTotalCost()),
                t.getTransactionDateFormatted()
            });
        }
        summaryArea.setText(String.format(
            "ALL TRANSACTIONS REPORT\n" +
            "Total Transactions : %d\n" +
            "Total Fuel Dispensed: %.1f L\n" +
            "Total Cost          : Rs. %.2f",
            txns.size(), db.getTotalFuelDispensed(), db.getTotalCost()
        ));
    }

    private void generateVehicleReport() {
        List<Vehicle> vehicles = db.getAllVehicles();
        updateColumns(new String[]{"Vehicle", "Registration", "Transactions", "Total Fuel (L)", "Total Cost (Rs)"});
        double grandFuel = 0; double grandCost = 0;
        for (Vehicle v : vehicles) {
            List<FuelTransaction> txns = db.getTransactionsByVehicle(v.getVehicleId());
            double fuel = txns.stream().mapToDouble(FuelTransaction::getQuantityDispensed).sum();
            double cost = txns.stream().mapToDouble(FuelTransaction::getTotalCost).sum();
            grandFuel += fuel; grandCost += cost;
            model.addRow(new Object[]{
                v.getMake() + " " + v.getModel(), v.getRegistrationNumber(),
                txns.size(), String.format("%.1f", fuel), String.format("%.2f", cost)
            });
        }
        summaryArea.setText(String.format(
            "VEHICLE-WISE REPORT\nTotal Vehicles: %d | Total Fuel: %.1f L | Total Cost: Rs. %.2f",
            vehicles.size(), grandFuel, grandCost
        ));
    }

    private void generateDriverReport() {
        List<User> drivers = db.getAllUsers().stream()
            .filter(u -> u.getRole() == User.Role.DRIVER).collect(Collectors.toList());
        updateColumns(new String[]{"Driver", "Requests Submitted", "Approved", "Rejected", "Dispensed"});
        for (User d : drivers) {
            List<FuelRequest> reqs = db.getRequestsByDriver(d.getUserId());
            long approved = reqs.stream().filter(r -> r.getStatus() == FuelRequest.Status.APPROVED || r.getStatus() == FuelRequest.Status.DISPENSED).count();
            long rejected = reqs.stream().filter(r -> r.getStatus() == FuelRequest.Status.REJECTED).count();
            long dispensed = reqs.stream().filter(r -> r.getStatus() == FuelRequest.Status.DISPENSED).count();
            model.addRow(new Object[]{ d.getFullName(), reqs.size(), approved, rejected, dispensed });
        }
        summaryArea.setText("DRIVER-WISE REPORT\nTotal Drivers: " + drivers.size());
    }

    private void generateCostSummary() {
        List<FuelTransaction> txns = db.getAllTransactions();
        updateColumns(new String[]{"Vehicle", "Registration", "Avg Unit Price", "Total Fuel (L)", "Total Cost (Rs)"});
        List<Vehicle> vehicles = db.getAllVehicles();
        double totalCost = 0;
        for (Vehicle v : vehicles) {
            List<FuelTransaction> vt = db.getTransactionsByVehicle(v.getVehicleId());
            if (vt.isEmpty()) continue;
            double avgPrice = vt.stream().mapToDouble(FuelTransaction::getUnitPrice).average().orElse(0);
            double fuel = vt.stream().mapToDouble(FuelTransaction::getQuantityDispensed).sum();
            double cost = vt.stream().mapToDouble(FuelTransaction::getTotalCost).sum();
            totalCost += cost;
            model.addRow(new Object[]{
                v.getMake() + " " + v.getModel(), v.getRegistrationNumber(),
                String.format("Rs. %.2f", avgPrice),
                String.format("%.1f", fuel),
                String.format("Rs. %.2f", cost)
            });
        }
        summaryArea.setText(String.format(
            "COST SUMMARY REPORT\nTotal Expenditure: Rs. %.2f\nCurrent Fuel Price: Rs. %.2f/L",
            totalCost, db.getFuelStock().getPricePerLiter()
        ));
    }

    private void generateEfficiencyReport() {
        updateColumns(new String[]{"Vehicle", "Registration", "Total Km", "Total Fuel (L)", "Efficiency (km/L)"});
        List<Vehicle> vehicles = db.getAllVehicles();
        for (Vehicle v : vehicles) {
            List<FuelTransaction> txns = db.getTransactionsByVehicle(v.getVehicleId());
            if (txns.size() < 2) {
                model.addRow(new Object[]{ v.getMake() + " " + v.getModel(), v.getRegistrationNumber(), "N/A", "N/A", "Insufficient data" });
                continue;
            }
            int firstOdo = txns.get(0).getOdometerReading();
            int lastOdo = txns.get(txns.size() - 1).getOdometerReading();
            double totalFuel = txns.stream().mapToDouble(FuelTransaction::getQuantityDispensed).sum();
            int totalKm = lastOdo - firstOdo;
            double efficiency = totalFuel > 0 && totalKm > 0 ? totalKm / totalFuel : 0;
            model.addRow(new Object[]{
                v.getMake() + " " + v.getModel(), v.getRegistrationNumber(),
                totalKm + " km", String.format("%.1f", totalFuel),
                efficiency > 0 ? String.format("%.2f km/L", efficiency) : "N/A"
            });
        }
        summaryArea.setText("FUEL EFFICIENCY REPORT\nComparison of fuel efficiency across all university vehicles.");
    }

    private void updateColumns(String[] cols) {
        model.setColumnCount(0);
        for (String c : cols) model.addColumn(c);
    }
}
