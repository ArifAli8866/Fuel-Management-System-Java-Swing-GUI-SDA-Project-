package com.ufms;

import com.ufms.gui.LoginFrame;
import com.ufms.util.UITheme;

import javax.swing.*;

/**
 * University Fuel Management System
 * FAST National University of Computing and Emerging Sciences
 *
 * Group Members:
 *   Saad Ahmed Ijaz    (24P-0669)
 *   Arif Ali           (24P-0736)
 *   Arslan Tariq       (24P-0610)
 *
 * Course: Software Design & Analysis (SDA)
 * Instructor: Engr. Muhammad Umer Haroon
 *
 * Design Patterns Used:
 *   1. Singleton  — DataStore.java
 *   2. Observer   — UI refresh via loadData() calls
 *   3. Strategy   — Role-based navigation in MainFrame
 */
public class Main {
    public static void main(String[] args) {
        // Set system look and feel for native appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fall back to default
        }
        UITheme.applyGlobalDefaults();

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
