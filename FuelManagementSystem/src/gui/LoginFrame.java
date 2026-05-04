package gui;

import javax.swing.*;

public class LoginFrame extends JFrame {

    JTextField username;
    JPasswordField password;
    JButton loginBtn;

    public LoginFrame() {

        setTitle("Login");
        setSize(300, 200);
        setLayout(null);

        username = new JTextField();
        password = new JPasswordField();
        loginBtn = new JButton("Login");

        username.setBounds(50, 30, 200, 30);
        password.setBounds(50, 70, 200, 30);
        loginBtn.setBounds(90, 110, 100, 30);

        add(username);
        add(password);
        add(loginBtn);

        loginBtn.addActionListener(e -> {

            if (username.getText().equals("admin")) {
                new AdminFrame();
                dispose();
            } else {
                new DriverFrame();
                dispose();
            }

        });

        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
}