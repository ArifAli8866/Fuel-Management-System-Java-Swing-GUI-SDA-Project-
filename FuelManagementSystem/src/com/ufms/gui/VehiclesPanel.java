package com.ufms.gui;

import com.ufms.model.*;
import com.ufms.service.DataStore;
import com.ufms.util.UITheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class VehiclesPanel extends JPanel {

    private final User currentUser;
    private final DataStore db = DataStore.getInstance();
    private JTable table;
    private DefaultTableModel model;

    public VehiclesPanel(User currentUser) {
        this.currentUser = currentUser;
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_MAIN);
        header.setBorder(BorderFactory.createEmptyBorder(24, 28, 8, 28));
        JLabel title = new JLabel("Vehicle Management");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        JButton addBtn = UITheme.primaryButton("+ Add Vehicle");
        JButton refreshBtn = UITheme.warningButton("🔄 Refresh");
        btnRow.add(refreshBtn);
        btnRow.add(addBtn);

        header.add(title, BorderLayout.WEST);
        header.add(btnRow, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Registration", "Make", "Model", "Year", "Capacity (L)", "Odometer (km)", "Status", "Assigned Driver"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        UITheme.styleTable(table);

        // Color status column
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                String s = val != null ? val.toString() : "";
                if ("ACTIVE".equals(s)) lbl.setForeground(UITheme.SUCCESS);
                else if ("MAINTENANCE".equals(s)) lbl.setForeground(UITheme.WARNING.darker());
                else lbl.setForeground(UITheme.DANGER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                return lbl;
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 28, 0, 28),
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR)
        ));
        add(sp, BorderLayout.CENTER);

        // Status change action panel
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        actions.setBackground(UITheme.BG_MAIN);
        JButton maintenanceBtn = UITheme.warningButton("🔧 Toggle Maintenance");
        JButton deactivateBtn = UITheme.dangerButton("🚫 Deactivate");
        actions.add(new JLabel("Selected vehicle:"));
        actions.add(maintenanceBtn);
        actions.add(deactivateBtn);
        add(actions, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> showAddVehicleDialog());
        refreshBtn.addActionListener(e -> loadData());
        maintenanceBtn.addActionListener(e -> toggleMaintenance());
        deactivateBtn.addActionListener(e -> deactivate());

        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        List<Vehicle> vehicles = db.getAllVehicles();
        for (Vehicle v : vehicles) {
            User driver = db.getUserById(v.getAssignedDriverId());
            String driverName = driver != null ? driver.getFullName() : "Unassigned";
            model.addRow(new Object[]{
                v.getVehicleId(), v.getRegistrationNumber(), v.getMake(), v.getModel(),
                v.getYear(), v.getFuelCapacity(), v.getCurrentOdometer(),
                v.getStatus().toString(), driverName
            });
        }
    }

    private void showAddVehicleDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register New Vehicle", true);
        dialog.setSize(420, 450);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        panel.setBackground(UITheme.BG_PANEL);

        JTextField regField = UITheme.styledField(); regField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JTextField makeField = UITheme.styledField(); makeField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JTextField modelField = UITheme.styledField(); modelField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JTextField yearField = UITheme.styledField(); yearField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JTextField capacityField = UITheme.styledField(); capacityField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        List<User> drivers = db.getAllUsers().stream()
            .filter(u -> u.getRole() == User.Role.DRIVER && u.isActive())
            .collect(java.util.stream.Collectors.toList());
        String[] driverOptions = drivers.stream().map(u -> u.getUserId() + " - " + u.getFullName()).toArray(String[]::new);
        JComboBox<String> driverCombo = new JComboBox<>(driverOptions);
        driverCombo.setFont(UITheme.FONT_BODY);
        driverCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        addFieldRow(panel, "Registration Number *", regField);
        addFieldRow(panel, "Make *", makeField);
        addFieldRow(panel, "Model *", modelField);
        addFieldRow(panel, "Year *", yearField);
        addFieldRow(panel, "Fuel Capacity (L) *", capacityField);
        addFieldRow(panel, "Assigned Driver *", driverCombo);

        panel.add(Box.createVerticalStrut(20));
        JButton saveBtn = UITheme.primaryButton("Register Vehicle");
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(saveBtn);

        saveBtn.addActionListener(e -> {
            String reg = regField.getText().trim();
            String make = makeField.getText().trim();
            String modelStr = modelField.getText().trim();
            String yearStr = yearField.getText().trim();
            String capStr = capacityField.getText().trim();
            if (reg.isEmpty() || make.isEmpty() || modelStr.isEmpty() || yearStr.isEmpty() || capStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int year; double cap;
            try { year = Integer.parseInt(yearStr); } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid year.", "Error", JOptionPane.ERROR_MESSAGE); return;
            }
            try { cap = Double.parseDouble(capStr); } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid capacity.", "Error", JOptionPane.ERROR_MESSAGE); return;
            }
            int driverIdx = driverCombo.getSelectedIndex();
            int driverId = driverIdx >= 0 && driverIdx < drivers.size() ? drivers.get(driverIdx).getUserId() : -1;

            Vehicle v = new Vehicle(db.getNextVehicleId(), reg, make, modelStr, year, cap, driverId);
            if (!db.addVehicle(v)) {
                JOptionPane.showMessageDialog(dialog, "Registration number already exists.", "Duplicate", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(dialog, "Vehicle registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
            loadData();
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void addFieldRow(JPanel panel, String label, JComponent field) {
        JLabel lbl = UITheme.fieldLabel(label);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(field);
        panel.add(Box.createVerticalStrut(12));
    }

    private void toggleMaintenance() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a vehicle.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        List<Vehicle> vehicles = db.getAllVehicles();
        Vehicle v = vehicles.get(row);
        if (v.getStatus() == Vehicle.Status.MAINTENANCE) {
            v.setStatus(Vehicle.Status.ACTIVE);
            JOptionPane.showMessageDialog(this, "Vehicle set to ACTIVE.", "Updated", JOptionPane.INFORMATION_MESSAGE);
        } else {
            v.setStatus(Vehicle.Status.MAINTENANCE);
            JOptionPane.showMessageDialog(this, "Vehicle set to MAINTENANCE.", "Updated", JOptionPane.INFORMATION_MESSAGE);
        }
        loadData();
    }

    private void deactivate() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a vehicle.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        List<Vehicle> vehicles = db.getAllVehicles();
        Vehicle v = vehicles.get(row);
        int confirm = JOptionPane.showConfirmDialog(this, "Deactivate vehicle " + v.getRegistrationNumber() + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            v.setStatus(Vehicle.Status.INACTIVE);
            loadData();
        }
    }
}
