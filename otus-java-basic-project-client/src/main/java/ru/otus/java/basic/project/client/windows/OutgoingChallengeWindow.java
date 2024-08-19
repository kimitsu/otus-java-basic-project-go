package ru.otus.java.basic.project.client.windows;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.project.api.enums.ChallengeResponse;
import ru.otus.java.basic.project.client.Client;
import ru.otus.java.basic.project.client.SwingUtils;
import ru.otus.java.basic.project.client.exceptions.ChallengeCancelledException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class OutgoingChallengeWindow {
    private final static Logger log = LogManager.getLogger(OutgoingChallengeWindow.class);
    private final Client client;
    private final JDialog frame;
    private final JLabel nameLabel;


    public OutgoingChallengeWindow(Client client, JFrame parent) {
        this.client = client;
        this.frame = new JDialog(parent, "Outgoing Challenge", true);
        JPanel infoPanel = new JPanel(new SpringLayout());
        JLabel infoLabel = new JLabel("Awaiting response from");
        infoLabel.setHorizontalAlignment(JLabel.CENTER);
        this.nameLabel = new JLabel("jZ");
        this.nameLabel.setHorizontalAlignment(JLabel.CENTER);
        this.nameLabel.setPreferredSize(new Dimension(250, 32));
        infoPanel.add(infoLabel);
        infoPanel.add(this.nameLabel);
        SwingUtils.makeCompactGrid(infoPanel, 2, 1, 8, 8);
        JPanel buttonsPanel = new JPanel(new SpringLayout());
        buttonsPanel.add(SwingUtils.makeButton("Cancel challenge", (_) -> cancelChallenge()));
        SwingUtils.makeCompactGrid(buttonsPanel, 1, 1, 8, 8);

        this.frame.getContentPane().setLayout(new BoxLayout(this.frame.getContentPane(), BoxLayout.Y_AXIS));
        this.frame.getContentPane().add(infoPanel);
        this.frame.getContentPane().add(buttonsPanel);
        this.frame.setResizable(false);
        this.frame.pack();
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelChallenge();
            }
        });
    }

    public void show(String challenged) {
        log.trace("Showing challenge window");
        nameLabel.setText(challenged);
        challenge(challenged);
        frame.setLocationRelativeTo(frame.getParent());
        frame.setVisible(true);
    }

    private void challenge(String challenged) {
        client.challengeAsync(challenged)
                .thenApply((result) -> {
                    switch (result) {
                        case REJECTED -> JOptionPane.showMessageDialog(frame, "Challenge has been rejected", "Challenge", JOptionPane.INFORMATION_MESSAGE);
                        case BUSY -> JOptionPane.showMessageDialog(frame, "Client is busy and cannot accept challenges at the moment", "Challenge", JOptionPane.INFORMATION_MESSAGE);
                    }
                    log.trace("Closing challenge window");
                    frame.setVisible(false);
                    if (result == ChallengeResponse.ACCEPTED) {
                        client.showGameWindow();
                    }
                    return result;
                })
                .exceptionally((e) -> {
                    switch (e.getCause()) {
                        case ChallengeCancelledException _ -> log.info("Challenge cancelled");
                        default -> {
                            log.error("Challenge failed", e);
                            JOptionPane.showMessageDialog(frame, e.getCause().getCause().getMessage(), e.getCause().getMessage(), JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    frame.setVisible(false);
                    return null;
                });
    }

    private void cancelChallenge() {
        client.cancelChallengeAsync();
    }

    public void dispose() {
        frame.dispose();
    }
}
