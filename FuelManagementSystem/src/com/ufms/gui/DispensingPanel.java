package com.ufms.gui;

import com.ufms.model.*;
import com.ufms.service.DataStore;
import com.ufms.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Use Case UC-003: Record Fuel Dispensing (Fuel Attendant)
 */
public class DispensingPanel extends JPanel {

    private final User currentUser;
    private final DataStore db = DataStore.getInstance();

    private JComboBox<String> requestCombo;
    private JTextField odometerField;
    private JTextField actualQtyField;
    private JLabel requestInfoLabel;
    private List<FuelRequest> approvedRequests;

    public DispensingPanel(User currentUser) {
        this.currentUser = currentUser;
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_MAIN);
        header.setBorder(BorderFactory.createEmptyBorder(24, 28, 8, 28));
        JLabel title = new JLabel("Record Fuel Dispensing");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);
        JLabel sub = new JLabel("Select an approved request and record the fuel dispensed");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_MUTED);
        JPanel hp = new JPanel(); hp.setOpaque(false);
        hp.setLayout(new BoxLayout(hp, BoxLayout.Y_AXIS));
        hp.add(title); hp.add(sub);
        header.add(hp);
        add(header, BorderLayout.NORTH);

        JPanel card = new JPanel();
        card.setBackground(UITheme.BG_PANEL);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(28, 32, 28, 32)
        ));

        // Stock indicator
        FuelStock stock = db.getFuelStock();
        JPanel stockRow = new JPanel(new BorderLayout());
        stockRow.setBackground(stock.isLow() ? new Color(255, 243, 205) : new Color(220, 248, 228));
        stockRow.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(stock.isLow() ? UITheme.WARNING : UITheme.SUCCESS),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        stockRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        JLabel stockInfo = new JLabel("⛽ Current Fuel Stock: " +
            String.format("%.0f L (%.1f%%)", stock.getCurrentLevel(), stock.getPercentage()) +
            " | Price: Rs. " + String.format("%.2f", stock.getPricePerLiter()) + "/L");
        stockInfo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        stockInfo.setForeground(stock.isLow() ? new Color(133, 77, 14) : new Color(22, 101, 52));
        stockRow.add(stockInfo);
        card.add(stockRow);
        card.add(Box.createVerticalStrut(20));

        // Approved request combo
        approvedRequests = db.getApprovedRequests();
        String[] options = approvedRequests.stream().map(r -> {
            Vehicle v = db.getVehicleById(r.getVehicleId());
            User d = db.getUserById(r.getDriverId());
            String vStr = v != null ? v.getRegistrationNumber() : "V#" + r.getVehicleId();
            String dStr = d != null ? d.getFullName() : "D#" + r.getDriverId();
            return "Request #" + r.getRequestId() + " | " + vStr + " | " + dStr + " | " + r.getEstimatedQuantity() + " L";
        }).toArray(String[]::new);

        requestCombo = new JComboBox<>(options.length > 0 ? options : new String[]{"No approved requests"});
        requestCombo.setFont(UITheme.FONT_BODY);
        requestCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        requestCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        requestInfoLabel = new JLabel(" ");
        requestInfoLabel.setFont(UITheme.FONT_SMALL);
        requestInfoLabel.setForeground(UITheme.TEXT_MUTED);
        requestInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        requestCombo.addActionListener(e -> updateRequestInfo());
        updateRequestInfo();

        odometerField = UITheme.styledField();
        odometerField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        odometerField.setAlignmentX(Component.LEFT_ALIGNMENT);

        actualQtyField = UITheme.styledField();
        actualQtyField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        actualQtyField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton dispenseBtn = UITheme.successButton("✅  Record Dispensing");
        dispenseBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        addRow(card, "Approved Request *", requestCombo);
        card.add(Box.createVerticalStrut(4));
        card.add(requestInfoLabel);
        card.add(Box.createVerticalStrut(16));
        addRow(card, "Current Odometer Reading (km) *", odometerField);
        card.add(Box.createVerticalStrut(16));
        addRow(card, "Actual Quantity Dispensed (Liters) *", actualQtyField);
        card.add(Box.createVerticalStrut(24));
        card.add(dispenseBtn);

        dispenseBtn.addActionListener(e -> recordDispensing());

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(UITheme.BG_MAIN);
        card.setPreferredSize(new Dimension(580, 0));
        wrapper.add(card);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
    }

    private void updateRequestInfo() {
        int idx = requestCombo.getSelectedIndex();
        if (!approvedRequests.isEmpty() && idx >= 0 && idx < approvedRequests.size()) {
            FuelRequest r = approvedRequests.get(idx);
            Vehicle v = db.getVehicleById(r.getVehicleId());
            if (v != null) {
                requestInfoLabel.setText("Vehicle: " + v.getMake() + " " + v.getModel() +
                    " | Current Odometer: " + v.getCurrentOdometer() + " km | Approved qty: " + r.getEstimatedQuantity() + " L");
            }
        }
    }

    private void addRow(JPanel panel, String label, JComponent field) {
        JLabel lbl = UITheme.fieldLabel(label);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(6));
        panel.add(field);
    }

    private void recordDispensing() {
        if (approvedRequests.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No approved requests to dispense.", "No Requests", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idx = requestCombo.getSelectedIndex();
        if (idx < 0 || idx >= approvedRequests.size()) return;

        String odomStr = odometerField.getText().trim();
        String qtyStr = actualQtyField.getText().trim();

        if (odomStr.isEmpty() || qtyStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int odometer;
        double qty;
        try {
            odometer = Integer.parseInt(odomStr);
            if (odometer < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid odometer reading.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            qty = Double.parseDouble(qtyStr);
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid quantity dispensed.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        FuelRequest req = approvedRequests.get(idx);
        FuelStock stock = db.getFuelStock();

        if (!stock.dispense(qty)) {
            JOptionPane.showMessageDialog(this,
                "⚠️ Insufficient fuel stock!\nRequested: " + qty + " L\nAvailable: " + stock.getCurrentLevel() + " L\nRecording partial dispensing not supported. Please restock.",
                "Insufficient Stock", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Vehicle v = db.getVehicleById(req.getVehicleId());
        if (v != null) {
            if (odometer < v.getCurrentOdometer()) {
                JOptionPane.showMessageDialog(this,
                    "⚠️ Odometer reading (" + odometer + " km) is less than current (" + v.getCurrentOdometer() + " km).",
                    "Odometer Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            v.setCurrentOdometer(odometer);
        }

        FuelTransaction txn = new FuelTransaction(
            db.getNextTransactionId(), req.getRequestId(),
            req.getVehicleId(), currentUser.getUserId(),
            odometer, qty, stock.getPricePerLiter()
        );
        db.addTransaction(txn);
        req.markDispensed();

        JOptionPane.showMessageDialog(this,
            "✅ Fuel dispensed successfully!\n\n" +
            "Transaction #" + txn.getTransactionId() + "\n" +
            "Vehicle: " + (v != null ? v.getRegistrationNumber() : "N/A") + "\n" +
            "Quantity: " + qty + " L\n" +
            "Cost: Rs. " + String.format("%.2f", txn.getTotalCost()) + "\n" +
            "Remaining Stock: " + String.format("%.0f L", stock.getCurrentLevel()),
            "Dispensing Recorded", JOptionPane.INFORMATION_MESSAGE);

        odometerField.setText("");
        actualQtyField.setText("");
        // Reload combo
        approvedRequests = db.getApprovedRequests();
        requestCombo.removeAllItems();
        for (FuelRequest r : approvedRequests) {
            Vehicle vv = db.getVehicleById(r.getVehicleId());
            User d = db.getUserById(r.getDriverId());
            String vStr = vv != null ? vv.getRegistrationNumber() : "V#" + r.getVehicleId();
            String dStr = d != null ? d.getFullName() : "D#" + r.getDriverId();
            requestCombo.addItem("Request #" + r.getRequestId() + " | " + vStr + " | " + dStr + " | " + r.getEstimatedQuantity() + " L");
        }
        if (approvedRequests.isEmpty()) requestCombo.addItem("No approved requests");
    }
}
