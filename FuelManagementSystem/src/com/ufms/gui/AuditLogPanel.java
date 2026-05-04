package com.ufms.gui;

import com.ufms.service.DataStore;
import com.ufms.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AuditLogPanel extends JPanel {

    private final DataStore db = DataStore.getInstance();
    private JTextArea logArea;

    public AuditLogPanel() {
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_MAIN);
        header.setBorder(BorderFactory.createEmptyBorder(24, 28, 8, 28));
        JLabel title = new JLabel("System Audit Log");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);
        JLabel sub = new JLabel("Complete history of all system actions and user activity");
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

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(UITheme.FONT_MONO);
        logArea.setBackground(new Color(20, 25, 40));
        logArea.setForeground(new Color(130, 255, 130));
        logArea.setCaretColor(Color.WHITE);
        logArea.setMargin(new Insets(12, 14, 12, 14));

        JScrollPane sp = new JScrollPane(logArea);
        sp.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 28, 28, 28),
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR)
        ));
        add(sp, BorderLayout.CENTER);
        loadData();
    }

    private void loadData() {
        List<String> logs = db.getAuditLog();
        StringBuilder sb = new StringBuilder();
        // Show most recent first
        for (int i = logs.size() - 1; i >= 0; i--) {
            sb.append(logs.get(i)).append("\n");
        }
        logArea.setText(sb.toString());
        logArea.setCaretPosition(0);
    }
}
