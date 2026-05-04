package com.ufms.gui;

import com.ufms.model.*;
import com.ufms.service.DataStore;
import com.ufms.util.UITheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class MyRequestsPanel extends JPanel {

    private final User currentUser;
    private final DataStore db = DataStore.getInstance();
    private JTable table;
    private DefaultTableModel model;

    public MyRequestsPanel(User currentUser) {
        this.currentUser = currentUser;
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_MAIN);
        header.setBorder(BorderFactory.createEmptyBorder(24, 28, 8, 28));
        JLabel title = new JLabel("My Fuel Requests");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);
        JButton refreshBtn = UITheme.primaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> loadData());
        header.add(title, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"Request #", "Vehicle", "Qty (L)", "Status", "Submitted", "Approval Date"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        UITheme.styleTable(table);

        // Color-code status column
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                String s = val != null ? val.toString() : "";
                switch (s) {
                    case "APPROVED":   lbl.setForeground(UITheme.SUCCESS); break;
                    case "REJECTED":   lbl.setForeground(UITheme.DANGER); break;
                    case "PENDING":    lbl.setForeground(UITheme.WARNING.darker()); break;
                    case "DISPENSED":  lbl.setForeground(UITheme.PRIMARY); break;
                    default:           lbl.setForeground(UITheme.TEXT_DARK);
                }
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                return lbl;
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 28, 28, 28),
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR)
        ));
        add(sp, BorderLayout.CENTER);
        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        List<FuelRequest> requests = db.getRequestsByDriver(currentUser.getUserId());
        for (int i = requests.size() - 1; i >= 0; i--) {
            FuelRequest r = requests.get(i);
            Vehicle v = db.getVehicleById(r.getVehicleId());
            String vStr = v != null ? v.getRegistrationNumber() : "N/A";
            String approvalDate = r.getApprovalDate() != null
                ? r.getApprovalDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "-";
            model.addRow(new Object[]{
                "#" + r.getRequestId(), vStr, r.getEstimatedQuantity(),
                r.getStatus().toString(), r.getRequestDateFormatted(), approvalDate
            });
        }
    }
}
