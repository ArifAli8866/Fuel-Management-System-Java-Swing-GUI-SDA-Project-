package gui;

import javax.swing.*;
import service.FuelService;
import model.FuelRequest;

public class AdminFrame extends JFrame {

    JTextArea area;
    JButton refresh, approve;

    public AdminFrame() {

        setTitle("Admin Panel");
        setSize(400, 300);
        setLayout(null);

        area = new JTextArea();
        refresh = new JButton("Refresh");
        approve = new JButton("Approve ID=1");

        area.setBounds(20, 20, 350, 150);
        refresh.setBounds(50, 200, 100, 30);
        approve.setBounds(200, 200, 120, 30);

        add(area);
        add(refresh);
        add(approve);

        refresh.addActionListener(e -> {

            area.setText("");

            for (FuelRequest r : FuelService.getRequests()) {
                area.append(
                        "ID: " + r.requestId +
                                " Vehicle: " + r.vehicleId +
                                " Qty: " + r.quantity +
                                " Status: " + r.status + "\n");
            }
        });

        approve.addActionListener(e -> {
            FuelService.approveRequest(1);
            JOptionPane.showMessageDialog(this, "Approved!");
        });

        setVisible(true);
    }
}