package com.ufms.gui;

import com.ufms.model.*;
import com.ufms.service.DataStore;
import com.ufms.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Use Case UC-001: Submit Fuel Request (Driver)
 */
public class FuelRequestPanel extends JPanel {

    private final User currentUser;
    private final DataStore db = DataStore.getInstance();

    private JComboBox<String> vehicleCombo;
    private JTextField quantityField;
    private JTextArea notesArea;
    private JLabel currentOdometerLabel;

    public FuelRequestPanel(User currentUser) {
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
        JLabel title = new JLabel("Submit Fuel Request");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);
        JLabel sub = new JLabel("Fill in the details to request fuel for your vehicle");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_MUTED);
        JPanel hp = new JPanel(); hp.setOpaque(false);
        hp.setLayout(new BoxLayout(hp, BoxLayout.Y_AXIS));
        hp.add(title); hp.add(sub);
        header.add(hp, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Form card
        JPanel card = new JPanel();
        card.setBackground(UITheme.BG_PANEL);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(28, 32, 28, 32)
        ));

        // Vehicle selection
        List<Vehicle> myVehicles = db.getVehiclesByDriver(currentUser.getUserId());
        if (myVehicles.isEmpty()) {
            // show all active vehicles if none assigned
            myVehicles = db.getAllVehicles();
        }
        String[] vehicleOptions = myVehicles.stream()
            .map(v -> v.getVehicleId() + " - " + v.getRegistrationNumber() + " | " + v.getMake() + " " + v.getModel())
            .toArray(String[]::new);

        vehicleCombo = new JComboBox<>(vehicleOptions);
        vehicleCombo.setFont(UITheme.FONT_BODY);
        vehicleCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        vehicleCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        currentOdometerLabel = new JLabel("Current Odometer: --");
        currentOdometerLabel.setFont(UITheme.FONT_SMALL);
        currentOdometerLabel.setForeground(UITheme.TEXT_MUTED);
        currentOdometerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        final List<Vehicle> finalVehicles = myVehicles;
        vehicleCombo.addActionListener(e -> {
            int idx = vehicleCombo.getSelectedIndex();
            if (idx >= 0 && idx < finalVehicles.size()) {
                Vehicle v = finalVehicles.get(idx);
                currentOdometerLabel.setText("Current Odometer: " + v.getCurrentOdometer() + " km | Tank capacity: " + v.getFuelCapacity() + " L");
            }
        });
        if (!myVehicles.isEmpty()) {
            Vehicle v = myVehicles.get(0);
            currentOdometerLabel.setText("Current Odometer: " + v.getCurrentOdometer() + " km | Tank capacity: " + v.getFuelCapacity() + " L");
        }

        quantityField = UITheme.styledField();
        quantityField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        quantityField.setAlignmentX(Component.LEFT_ALIGNMENT);

        notesArea = new JTextArea(3, 40);
        notesArea.setFont(UITheme.FONT_BODY);
        notesArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane noteScroll = new JScrollPane(notesArea);
        noteScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        noteScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JButton submitBtn = UITheme.primaryButton("Submit Fuel Request");
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton clearBtn = UITheme.warningButton("Clear Form");
        clearBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.add(submitBtn);
        btnRow.add(clearBtn);

        addRow(card, "Select Vehicle *", vehicleCombo);
        card.add(Box.createVerticalStrut(4));
        card.add(currentOdometerLabel);
        card.add(Box.createVerticalStrut(16));
        addRow(card, "Estimated Fuel Quantity (Liters) *", quantityField);
        card.add(Box.createVerticalStrut(16));
        addRow(card, "Notes / Reason (optional)", noteScroll);
        card.add(Box.createVerticalStrut(24));
        card.add(btnRow);

        submitBtn.addActionListener(e -> submitRequest(finalVehicles));
        clearBtn.addActionListener(e -> {
            vehicleCombo.setSelectedIndex(0);
            quantityField.setText("");
            notesArea.setText("");
        });

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(UITheme.BG_MAIN);
        card.setPreferredSize(new Dimension(560, 0));
        wrapper.add(card);
        add(new JScrollPane(wrapper), BorderLayout.CENTER);
        ((JScrollPane)getComponent(1)).setBorder(null);
    }

    private void addRow(JPanel panel, String label, JComponent field) {
        JLabel lbl = UITheme.fieldLabel(label);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(6));
        panel.add(field);
    }

    private void submitRequest(List<Vehicle> vehicles) {
        int idx = vehicleCombo.getSelectedIndex();
        String qtyText = quantityField.getText().trim();

        if (qtyText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an estimated quantity.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double qty;
        try {
            qty = Double.parseDouble(qtyText);
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive number for quantity.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (idx < 0 || idx >= vehicles.size()) {
            JOptionPane.showMessageDialog(this, "Please select a vehicle.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Vehicle vehicle = vehicles.get(idx);
        if (qty > vehicle.getFuelCapacity()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "⚠️ Requested quantity (" + qty + " L) exceeds vehicle tank capacity (" + vehicle.getFuelCapacity() + " L).\nDo you still want to proceed?",
                "Quantity Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;
        }

        FuelRequest request = new FuelRequest(
            db.getNextRequestId(),
            vehicle.getVehicleId(),
            currentUser.getUserId(),
            qty
        );
        db.addRequest(request);

        JOptionPane.showMessageDialog(this,
            "✅ Fuel request #" + request.getRequestId() + " submitted successfully!\n" +
            "Vehicle: " + vehicle.getRegistrationNumber() + "\n" +
            "Quantity: " + qty + " L\n" +
            "Status: PENDING — Awaiting admin approval.",
            "Request Submitted", JOptionPane.INFORMATION_MESSAGE);

        quantityField.setText("");
        notesArea.setText("");
    }
}
