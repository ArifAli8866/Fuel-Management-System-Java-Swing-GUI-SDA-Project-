package gui;

import javax.swing.*;
import service.FuelService;
import model.FuelRequest;

public class DriverFrame extends JFrame {

    JTextField vehicleId, quantity;
    JButton submit;

    public DriverFrame() {

        setTitle("Driver Panel");
        setSize(300, 200);
        setLayout(null);

        vehicleId = new JTextField();
        quantity = new JTextField();
        submit = new JButton("Submit Request");

        vehicleId.setBounds(50, 30, 200, 30);
        quantity.setBounds(50, 70, 200, 30);
        submit.setBounds(70, 110, 150, 30);

        add(vehicleId);
        add(quantity);
        add(submit);

        submit.addActionListener(e -> {

            int v = Integer.parseInt(vehicleId.getText());
            double q = Double.parseDouble(quantity.getText());

            FuelRequest r = new FuelRequest(
                    FuelService.requests.size() + 1,
                    v,
                    1,
                    q);

            FuelService.addRequest(r);

            JOptionPane.showMessageDialog(this, "Request Submitted!");
        });

        setVisible(true);
    }
}