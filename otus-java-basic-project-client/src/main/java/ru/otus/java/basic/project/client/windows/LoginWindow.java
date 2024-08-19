package ru.otus.java.basic.project.client.windows;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.project.client.Client;
import ru.otus.java.basic.project.client.SwingUtils;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LoginWindow {
    private static final Logger log = LogManager.getLogger(LoginWindow.class);
    private final JFrame frame;
    private final JTextField nameField;
    private final JTextField passwordField;
    private final JTextField hostPortField;
    private final Client client;

    public LoginWindow(Client client) {
        this.client = client;
        this.frame = new JFrame("Simple Go Server");

        JPanel fieldsPanel = new JPanel(new SpringLayout());
        fieldsPanel.add(new JLabel("Name"));
        this.nameField = new JTextField(16);
        fieldsPanel.add(nameField);

        fieldsPanel.add(new JLabel("Password"));
        this.passwordField = new JPasswordField(16);
        fieldsPanel.add(passwordField);

        fieldsPanel.add(new JLabel("Server Host:Port"));
        this.hostPortField = new JTextField(16);
        this.hostPortField.setText("localhost");
        fieldsPanel.add(hostPortField);

        SwingUtils.makeCompactGrid(fieldsPanel, 3, 2, 8, 8);

        JPanel buttonsPanel = new JPanel(new SpringLayout());
        buttonsPanel.add(SwingUtils.makeButton("Login", (_) -> connectAndLogin(hostPortField.getText(), nameField.getText(), passwordField.getText(), false)));
        buttonsPanel.add(SwingUtils.makeButton("Register", (_) -> connectAndLogin(hostPortField.getText(), nameField.getText(), passwordField.getText(), true)));
        buttonsPanel.add(SwingUtils.makeButton("Exit", (_) -> SwingUtils.close(this.frame)));
        SwingUtils.makeCompactGrid(buttonsPanel, 1, 3, 8, 8);

        this.frame.getContentPane().setLayout(new BoxLayout(this.frame.getContentPane(), BoxLayout.Y_AXIS));
        this.frame.getContentPane().add(fieldsPanel);
        this.frame.getContentPane().add(buttonsPanel);
        this.frame.setResizable(false);
        this.frame.pack();
        frame.setLocationRelativeTo(null);
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeWindow();
            }
        });
    }

    private void closeWindow() {
        log.trace("Closing login window");
        client.close();
    }

    public void show() {
        log.trace("Showing login window");
        frame.setVisible(true);
    }

    public void connectAndLogin(String hostPort, String name, String password, boolean register) {
        SwingUtils.setEnabled(frame.getContentPane(), false);
        client.connectAndLoginAsync(hostPort, name, password, register)
                .thenRun(() -> {
                    log.trace("Hiding login window");
                    frame.setVisible(false);
                    client.showLobbyWindow();
                })
                .exceptionally((e) -> {
                    log.error("Error while logging in", e);
                    JOptionPane.showMessageDialog(this.frame, e.getCause().getCause().getMessage(), e.getCause().getMessage(), JOptionPane.ERROR_MESSAGE);
                    return null;
                })
                .whenComplete((result, e) -> {
                    log.trace("Login future complete ({}, {})", result, e);
                    SwingUtils.setEnabled(frame.getContentPane(), true);
                });
    }

    public void dispose() {
        frame.dispose();
    }
}
