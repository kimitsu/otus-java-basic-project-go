package ru.otus.java.basic.project.client.windows;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.project.client.Client;
import ru.otus.java.basic.project.client.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;

public class LobbyWindow {
    private static final Logger log = LogManager.getLogger(LobbyWindow.class);
    private final Client client;
    JFrame frame;
    JList<String> userList;
    JButton challengeButton;

    public LobbyWindow(Client client) {
        this.client = client;
        this.frame = new JFrame("Lobby");
        JPanel listPanel = new JPanel(new SpringLayout());
        this.userList = new JList<>();
        userList.setListData(new String[]{"One", "Two", "Three"});
        JScrollPane listScroller = new JScrollPane(this.userList);
        listScroller.setPreferredSize(new Dimension(400, 500));
        listPanel.add(listScroller);
        SwingUtils.makeCompactGrid(listPanel, 1, 1, 8, 8);
        JPanel buttonsPanel = new JPanel(new SpringLayout());
        this.challengeButton = SwingUtils.makeButton("Challenge", this::challengeButtonClick);
        buttonsPanel.add(this.challengeButton);
        buttonsPanel.add(SwingUtils.makeButton("Exit", (_) -> closeWindow()));
        SwingUtils.makeCompactGrid(buttonsPanel, 1, 2, 8, 8);

        this.frame.getContentPane().setLayout(new BoxLayout(this.frame.getContentPane(), BoxLayout.Y_AXIS));
        this.frame.getContentPane().add(listPanel);
        this.frame.getContentPane().add(buttonsPanel);
        this.frame.pack();
        this.frame.setLocationRelativeTo(null);
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeWindow();
            }
        });
    }

    private void closeWindow() {
        client.close();
    }

    public void show(String name) {
        log.trace("Showing lobby window");
        frame.setTitle("Lobby - " + name);
        frame.setVisible(true);
        String[] listData = new String[]{};
        userList.setListData(listData);
        client.listenToChallenges(true);
        client.updateClientsListAsync()
                .thenApply((result) -> {
                    refreshClientsList(result);
                    client.listenToClientListUpdated(true);
                    return result;
                })
                .exceptionally((e) -> {
                    log.error(e);
                    JOptionPane.showMessageDialog(frame, e.getCause().getMessage(), e.getMessage(), JOptionPane.ERROR_MESSAGE);
                    return null;
                });

    }

    public void refreshClientsList(Collection<String> result) {
        String[] listData = new String[]{};
        log.trace("Refreshing the clients list");
        String selection = userList.getSelectedValue();
        userList.setListData(result.toArray(listData));
        if (selection != null) {
            userList.setSelectedValue(selection, true);
        }
    }


    public void challengeButtonClick(ActionEvent event) {
        if (userList.getSelectedValue() == null) {
            JOptionPane.showMessageDialog(frame, "Select a client from the list to send a challenge", "Challenge", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (userList.getSelectedValue().equals(client.getName())) {
            JOptionPane.showMessageDialog(frame, "You may not challenge yourself", "Challenge", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        client.showOutgoingChallengeWindow(userList.getSelectedValue());
    }

    public JFrame getFrame() {
        return frame;
    }

    public void displayServerError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Server Error", JOptionPane.ERROR_MESSAGE);
    }

    public void dispose() {
        frame.dispose();
    }
}
