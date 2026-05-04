package com.ufms.gui;

import com.ufms.model.*;
import com.ufms.service.DataStore;
import com.ufms.util.UITheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Use Case UC-002: Approve/Reject Fuel Requests (Transport Admin)
 */
public class ApprovalPanel extends JPanel {

    private final User currentUser;
    private final DataStore db = DataStore.getInstance();
    private JTable table;
    private DefaultTableModel model;
    private List<FuelRequest> pending;

    public ApprovalPanel(User currentUser) {
        this.currentUser = currentUser;
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_MAIN);
        header.setBorder(BorderFactory.createEmptyBorder(24, 28, 8, 28));
        JLabel title = new JLabel("Approve Fuel Requests");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);
        JLabel sub = new JLabel("Review and approve or reject pending fuel requests");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_MUTED);
        JPanel hp = new JPanel(); hp.setOpaque(false);
        hp.setLayout(new BoxLayout(hp, BoxLayout.Y_AXIS));
        hp.add(title); hp.add(sub);
        JButton refreshBtn = UITheme.primaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> loadData());
        header.add(hp, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"Req #", "Vehicle", "Registration", "Driver", "Qty (L)", "Submitted", "History (km/L)"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 28));
        add(sp, BorderLayout.CENTER);

        // Bottom action panel
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 16));
        actions.setBackground(UITheme.BG_MAIN);
        actions.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));

        JButton approveBtn = UITheme.successButton("✅  Approve Selected");
        JButton rejectBtn  = UITheme.dangerButton("❌  Reject Selected");
        JLabel hint = new JLabel("Select a request from the table then click Approve or Reject.");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setForeground(UITheme.TEXT_MUTED);

        actions.add(approveBtn);
        actions.add(rejectBtn);
        actions.add(hint);
        add(actions, BorderLayout.SOUTH);

        approveBtn.addActionListener(e -> handleApprove());
        rejectBtn.addActionListener(e -> handleReject());

        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        pending = db.getPendingRequests();
        for (FuelRequest r : pending) {
            Vehicle v = db.getVehicleById(r.getVehicleId());
            User driver = db.getUserById(r.getDriverId());
            String regNo = v != null ? v.getRegistrationNumber() : "N/A";
            String vDesc = v != null ? v.getMake() + " " + v.getModel() : "N/A";
            String driverName = driver != null ? driver.getFullName() : "N/A";
            // Calculate fuel efficiency from past transactions
            String efficiency = computeEfficiency(r.getVehicleId());
            model.addRow(new Object[]{
                "#" + r.getRequestId(), vDesc, regNo, driverName,
                r.getEstimatedQuantity(), r.getRequestDateFormatted(), efficiency
            });
        }
    }

    private String computeEfficiency(int vehicleId) {
        List<FuelTransaction> txns = db.getTransactionsByVehicle(vehicleId);
        if (txns.size() < 2) return "Insufficient data";
        double totalFuel = txns.stream().mapToDouble(FuelTransaction::getQuantityDispensed).sum();
        int firstOdometer = txns.get(0).getOdometerReading();
        int lastOdometer = txns.get(txns.size() - 1).getOdometerReading();
        if (totalFuel == 0 || lastOdometer <= firstOdometer) return "N/A";
        double kml = (lastOdometer - firstOdometer) / totalFuel;
        return String.format("%.2f km/L", kml);
    }

    private void handleApprove() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a request to approve.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        FuelRequest req = pending.get(row);

        // Check stock
        FuelStock stock = db.getFuelStock();
        if (req.getEstimatedQuantity() > stock.getCurrentLevel()) {
            JOptionPane.showMessageDialog(this,
                "⚠️ Insufficient fuel stock!\nRequested: " + req.getEstimatedQuantity() + " L\nAvailable: " + stock.getCurrentLevel() + " L",
                "Stock Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Approve Request #" + req.getRequestId() + " for " + req.getEstimatedQuantity() + " L?",
            "Confirm Approval", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            req.approve(currentUser.getUserId());
            db.addAuditLog(currentUser.getUsername(), "Approved fuel request #" + req.getRequestId());
            JOptionPane.showMessageDialog(this, "✅ Request #" + req.getRequestId() + " approved successfully!", "Approved", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        }
    }

    private void handleReject() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a request to reject.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        FuelRequest req = pending.get(row);
        String reason = JOptionPane.showInputDialog(this,
            "Enter rejection reason for Request #" + req.getRequestId() + ":",
            "Rejection Reason", JOptionPane.QUESTION_MESSAGE);
        if (reason != null && !reason.trim().isEmpty()) {
            req.reject(currentUser.getUserId(), reason.trim());
            db.addAuditLog(currentUser.getUsername(), "Rejected fuel request #" + req.getRequestId() + ". Reason: " + reason);
            JOptionPane.showMessageDialog(this, "Request #" + req.getRequestId() + " rejected.", "Rejected", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        }
    }
}
