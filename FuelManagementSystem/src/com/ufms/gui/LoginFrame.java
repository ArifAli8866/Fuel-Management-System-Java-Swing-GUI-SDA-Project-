package com.ufms.gui;

import com.ufms.model.User;
import com.ufms.service.DataStore;
import com.ufms.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;

    public LoginFrame() {
        setTitle("University Fuel Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        UITheme.applyGlobalDefaults();
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());

        // ── Left branding panel ──────────────────────────────────────────────
        JPanel left = new JPanel();
        left.setBackground(UITheme.BG_SIDEBAR);
        left.setPreferredSize(new Dimension(380, 0));
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(BorderFactory.createEmptyBorder(60, 40, 60, 40));

        JLabel logo = new JLabel("⛽");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("<html><center>University Fuel<br>Management System</center></html>");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("<html><center>FAST National University<br>of Computing & Emerging Sciences</center></html>");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_SIDEBAR);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(260, 1));
        sep.setForeground(new Color(255, 255, 255, 40));

        JLabel hint = new JLabel("<html><center><b>Demo Credentials</b><br>" +
                "Admin: admin / admin123<br>" +
                "Driver: driver1 / pass123<br>" +
                "Attendant: attendant1 / pass123<br>" +
                "Finance: finance1 / pass123</center></html>");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setForeground(new Color(150, 170, 200));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        left.add(Box.createVerticalGlue());
        left.add(logo);
        left.add(Box.createVerticalStrut(18));
        left.add(title);
        left.add(Box.createVerticalStrut(10));
        left.add(sub);
        left.add(Box.createVerticalStrut(30));
        left.add(sep);
        left.add(Box.createVerticalStrut(20));
        left.add(hint);
        left.add(Box.createVerticalGlue());

        // ── Right login form ─────────────────────────────────────────────────
        JPanel right = new JPanel();
        right.setBackground(UITheme.BG_MAIN);
        right.setLayout(new GridBagLayout());

        JPanel form = new JPanel();
        form.setBackground(UITheme.BG_PANEL);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));

        JLabel loginTitle = new JLabel("Sign In");
        loginTitle.setFont(UITheme.FONT_TITLE);
        loginTitle.setForeground(UITheme.TEXT_DARK);
        loginTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel loginSub = new JLabel("Enter your credentials to continue");
        loginSub.setFont(UITheme.FONT_SMALL);
        loginSub.setForeground(UITheme.TEXT_MUTED);
        loginSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userLabel = UITheme.fieldLabel("Username");
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField = UITheme.styledField();
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel passLabel = UITheme.fieldLabel("Password");
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField = new JPasswordField();
        passwordField.setFont(UITheme.FONT_BODY);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(UITheme.FONT_SMALL);
        errorLabel.setForeground(UITheme.DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginBtn = UITheme.primaryButton("Sign In");
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        form.add(loginTitle);
        form.add(Box.createVerticalStrut(4));
        form.add(loginSub);
        form.add(Box.createVerticalStrut(28));
        form.add(userLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(16));
        form.add(passLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(6));
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(10));
        form.add(loginBtn);
        form.setPreferredSize(new Dimension(320, 360));

        right.add(form);

        root.add(left, BorderLayout.WEST);
        root.add(right, BorderLayout.CENTER);
        add(root);

        // ── Actions ──────────────────────────────────────────────────────────
        ActionListener loginAction = e -> doLogin();
        loginBtn.addActionListener(loginAction);
        passwordField.addActionListener(loginAction);
        usernameField.addActionListener(loginAction);
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter username and password.");
            return;
        }

        User user = DataStore.getInstance().authenticate(username, password);
        if (user != null) {
            DataStore.getInstance().addAuditLog(username, "Logged in");
            dispose();
            SwingUtilities.invokeLater(() -> new MainFrame(user).setVisible(true));
        } else {
            errorLabel.setText("Invalid credentials. Please try again.");
            passwordField.setText("");
        }
    }
}
