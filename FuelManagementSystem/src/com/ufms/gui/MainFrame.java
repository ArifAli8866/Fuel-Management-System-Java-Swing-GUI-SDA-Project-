package com.ufms.gui;

import com.ufms.model.User;
import com.ufms.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame {

    private final User currentUser;
    private JPanel contentArea;
    private JLabel pageTitle;
    private List<JButton> sidebarButtons = new ArrayList<>();

    public MainFrame(User currentUser) {
        this.currentUser = currentUser;
        setTitle("UFMS - " + currentUser.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());

        // ── Header ───────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY);
        header.setPreferredSize(new Dimension(0, 55));
        header.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel appTitle = new JLabel("⛽  University Fuel Management System");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        appTitle.setForeground(Color.WHITE);

        JPanel userInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userInfo.setOpaque(false);
        JLabel userLabel = new JLabel(currentUser.getFullName() + "  |  " + currentUser.getRole().toString().replace("_", " "));
        userLabel.setFont(UITheme.FONT_SMALL);
        userLabel.setForeground(new Color(200, 220, 255));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(UITheme.FONT_SMALL);
        logoutBtn.setBackground(new Color(220, 53, 69));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> logout());

        userInfo.add(userLabel);
        userInfo.add(logoutBtn);

        header.add(appTitle, BorderLayout.WEST);
        header.add(userInfo, BorderLayout.EAST);

        // ── Sidebar ──────────────────────────────────────────────────────────
        JPanel sidebar = new JPanel();
        sidebar.setBackground(UITheme.BG_SIDEBAR);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 0));

        // ── Content Area ─────────────────────────────────────────────────────
        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(UITheme.BG_MAIN);

        // Build sidebar navigation based on role
        buildSidebarNav(sidebar);

        root.add(header, BorderLayout.NORTH);
        root.add(sidebar, BorderLayout.WEST);
        root.add(contentArea, BorderLayout.CENTER);
        add(root);

        // Show dashboard by default
        showDashboard();
    }

    private void buildSidebarNav(JPanel sidebar) {
        User.Role role = currentUser.getRole();

        addSidebarSection(sidebar, "MAIN");
        addNavButton(sidebar, "🏠  Dashboard", () -> showDashboard());

        if (role == User.Role.DRIVER) {
            addSidebarSection(sidebar, "DRIVER");
            addNavButton(sidebar, "⛽  Request Fuel", () -> showPanel(new FuelRequestPanel(currentUser)));
            addNavButton(sidebar, "📋  My Requests", () -> showPanel(new MyRequestsPanel(currentUser)));
        }

        if (role == User.Role.FUEL_ATTENDANT) {
            addSidebarSection(sidebar, "ATTENDANT");
            addNavButton(sidebar, "🔧  Dispense Fuel", () -> showPanel(new DispensingPanel(currentUser)));
            addNavButton(sidebar, "📋  Daily Transactions", () -> showPanel(new TransactionsPanel(currentUser)));
        }

        if (role == User.Role.TRANSPORT_ADMIN || role == User.Role.SYSTEM_ADMIN) {
            addSidebarSection(sidebar, "MANAGEMENT");
            addNavButton(sidebar, "✅  Approve Requests", () -> showPanel(new ApprovalPanel(currentUser)));
            addNavButton(sidebar, "🚌  Vehicles", () -> showPanel(new VehiclesPanel(currentUser)));
            addNavButton(sidebar, "👤  Drivers", () -> showPanel(new UsersPanel(currentUser)));
            addNavButton(sidebar, "📊  Reports", () -> showPanel(new ReportsPanel(currentUser)));
            addNavButton(sidebar, "⛽  Fuel Stock", () -> showPanel(new FuelStockPanel(currentUser)));
        }

        if (role == User.Role.FINANCE_DEPT) {
            addSidebarSection(sidebar, "FINANCE");
            addNavButton(sidebar, "💰  Expense Reports", () -> showPanel(new ReportsPanel(currentUser)));
            addNavButton(sidebar, "📋  All Transactions", () -> showPanel(new TransactionsPanel(currentUser)));
        }

        if (role == User.Role.SYSTEM_ADMIN) {
            addSidebarSection(sidebar, "SYSTEM");
            addNavButton(sidebar, "🗂️  Audit Log", () -> showPanel(new AuditLogPanel()));
            addNavButton(sidebar, "👥  User Management", () -> showPanel(new UsersPanel(currentUser)));
        }
    }

    private void addSidebarSection(JPanel sidebar, String label) {
        JLabel sectionLabel = new JLabel(label);
        sectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        sectionLabel.setForeground(new Color(100, 120, 155));
        sectionLabel.setBorder(BorderFactory.createEmptyBorder(16, 20, 4, 0));
        sectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(sectionLabel);
    }

    private void addNavButton(JPanel sidebar, String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_SIDEBAR);
        btn.setForeground(UITheme.TEXT_SIDEBAR);
        btn.setBackground(UITheme.BG_SIDEBAR);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(9, 22, 9, 10));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(40, 60, 100));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!btn.getBackground().equals(UITheme.PRIMARY)) {
                    btn.setBackground(UITheme.BG_SIDEBAR);
                }
            }
        });

        btn.addActionListener(e -> {
            sidebarButtons.forEach(b -> {
                b.setBackground(UITheme.BG_SIDEBAR);
                b.setForeground(UITheme.TEXT_SIDEBAR);
            });
            btn.setBackground(UITheme.PRIMARY);
            btn.setForeground(Color.WHITE);
            action.run();
        });

        sidebarButtons.add(btn);
        sidebar.add(btn);
    }

    private void showPanel(JPanel panel) {
        contentArea.removeAll();
        contentArea.add(panel, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    private void showDashboard() {
        showPanel(new DashboardPanel(currentUser));
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?", "Confirm Logout",
            JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}
