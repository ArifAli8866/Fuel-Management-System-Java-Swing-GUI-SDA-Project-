package com.ufms.gui;

import com.ufms.model.*;
import com.ufms.service.DataStore;
import com.ufms.util.UITheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class TransactionsPanel extends JPanel {

    private final User currentUser;
    private final DataStore db = DataStore.getInstance();
    private JTable table;
    private DefaultTableModel model;

    public TransactionsPanel(User currentUser) {
        this.currentUser = currentUser;
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_MAIN);
        header.setBorder(BorderFactory.createEmptyBorder(24, 28, 8, 28));
        JLabel title = new JLabel("Fuel Transactions");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);
        JButton refreshBtn = UITheme.primaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> loadData());
        header.add(title, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"Txn #", "Vehicle", "Registration", "Attendant", "Qty (L)", "Unit Price", "Total Cost", "Odometer", "Date"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        UITheme.styleTable(table);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 28, 0, 28),
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR)
        ));
        add(sp, BorderLayout.CENTER);

        // Summary footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        footer.setBackground(UITheme.TABLE_HEADER);
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 28, 8, 28),
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR)
        ));

        JLabel totalFuelLbl = new JLabel();
        totalFuelLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        totalFuelLbl.setForeground(UITheme.PRIMARY);

        JLabel totalCostLbl = new JLabel();
        totalCostLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        totalCostLbl.setForeground(UITheme.DANGER);

        footer.add(new JLabel("Summary:"));
        footer.add(totalFuelLbl);
        footer.add(totalCostLbl);
        add(footer, BorderLayout.SOUTH);

        loadData();
        totalFuelLbl.setText("Total Fuel: " + String.format("%.1f L", db.getTotalFuelDispensed()));
        totalCostLbl.setText("Total Cost: Rs. " + String.format("%.2f", db.getTotalCost()));
    }

    private void loadData() {
        model.setRowCount(0);
        List<FuelTransaction> txns = db.getAllTransactions();
        for (int i = txns.size() - 1; i >= 0; i--) {
            FuelTransaction t = txns.get(i);
            Vehicle v = db.getVehicleById(t.getVehicleId());
            User att = db.getUserById(t.getAttendantId());
            model.addRow(new Object[]{
                "#" + t.getTransactionId(),
                v != null ? v.getMake() + " " + v.getModel() : "N/A",
                v != null ? v.getRegistrationNumber() : "N/A",
                att != null ? att.getFullName() : "N/A",
                String.format("%.1f", t.getQuantityDispensed()),
                String.format("Rs. %.2f", t.getUnitPrice()),
                String.format("Rs. %.2f", t.getTotalCost()),
                t.getOdometerReading() + " km",
                t.getTransactionDateFormatted()
            });
        }
    }
}
