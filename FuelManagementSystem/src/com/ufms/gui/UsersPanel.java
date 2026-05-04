package com.ufms.gui;

import com.ufms.model.*;
import com.ufms.service.DataStore;
import com.ufms.util.UITheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class UsersPanel extends JPanel {

    private final User currentUser;
    private final DataStore db = DataStore.getInstance();
    private JTable table;
    private DefaultTableModel model;

    public UsersPanel(User currentUser) {
        this.currentUser = currentUser;
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_MAIN);
        header.setBorder(BorderFactory.createEmptyBorder(24, 28, 8, 28));
        JLabel title = new JLabel("User Management");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        JButton addBtn = UITheme.primaryButton("+ Add User");
        JButton refreshBtn = UITheme.warningButton("🔄 Refresh");
        btnRow.add(refreshBtn); btnRow.add(addBtn);
        header.add(title, BorderLayout.WEST);
        header.add(btnRow, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Username", "Full Name", "Email", "Role", "Status"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        UITheme.styleTable(table);

        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                lbl.setForeground("Active".equals(val) ? UITheme.SUCCESS : UITheme.DANGER);
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

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        actions.setBackground(UITheme.BG_MAIN);
        JButton toggleBtn = UITheme.warningButton("Toggle Active/Inactive");
        actions.add(toggleBtn);
        add(actions, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> showAddUserDialog());
        refreshBtn.addActionListener(e -> loadData());
        toggleBtn.addActionListener(e -> toggleUser());

        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        List<User> users = db.getAllUsers();
        for (User u : users) {
            model.addRow(new Object[]{
                u.getUserId(), u.getUsername(), u.getFullName(), u.getEmail(),
                u.getRole().toString().replace("_", " "),
                u.isActive() ? "Active" : "Inactive"
            });
        }
    }

    private void showAddUserDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New User", true);
        dialog.setSize(400, 420);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        panel.setBackground(UITheme.BG_PANEL);

        JTextField userField = UITheme.styledField(); userField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JPasswordField passField = new JPasswordField(); passField.setFont(UITheme.FONT_BODY);
        passField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR), BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JTextField nameField = UITheme.styledField(); nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JTextField emailField = UITheme.styledField(); emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        String[] roles = {"DRIVER", "FUEL_ATTENDANT", "TRANSPORT_ADMIN", "FINANCE_DEPT", "SYSTEM_ADMIN"};
        JComboBox<String> roleCombo = new JComboBox<>(roles);
        roleCombo.setFont(UITheme.FONT_BODY);
        roleCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        addFieldRow(panel, "Username *", userField);
        addFieldRow(panel, "Password *", passField);
        addFieldRow(panel, "Full Name *", nameField);
        addFieldRow(panel, "Email", emailField);
        addFieldRow(panel, "Role *", roleCombo);

        panel.add(Box.createVerticalStrut(16));
        JButton saveBtn = UITheme.primaryButton("Add User");
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(saveBtn);

        saveBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword());
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String roleStr = roleCombo.getSelectedItem().toString();

            if (username.isEmpty() || password.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Username, password, and name are required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            User.Role role = User.Role.valueOf(roleStr);
            User user = new User(db.getNextUserId(), username, password, name, email, role);
            if (!db.addUser(user)) {
                JOptionPane.showMessageDialog(dialog, "Username already exists.", "Duplicate", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(dialog, "User added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
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
        panel.add(Box.createVerticalStrut(10));
    }

    private void toggleUser() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a user first.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        List<User> users = db.getAllUsers();
        User u = users.get(row);
        if (u.getUserId() == currentUser.getUserId()) {
            JOptionPane.showMessageDialog(this, "You cannot deactivate your own account.", "Error", JOptionPane.ERROR_MESSAGE); return;
        }
        u.setActive(!u.isActive());
        db.addAuditLog(currentUser.getUsername(), (u.isActive() ? "Activated" : "Deactivated") + " user: " + u.getUsername());
        loadData();
    }
}
