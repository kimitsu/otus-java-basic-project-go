package ru.otus.java.basic.project.client.windows;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.project.api.context.ChallengeContext;
import ru.otus.java.basic.project.api.enums.ChallengeResponse;
import ru.otus.java.basic.project.client.Client;
import ru.otus.java.basic.project.client.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class IncomingChallengeWindow {
    private final static Logger log = LogManager.getLogger(IncomingChallengeWindow.class);
    private final Client client;
    private JDialog frame;
    private JLabel nameLabel;
    private ChallengeContext challengeContext = null;


    public IncomingChallengeWindow(Client client, JFrame parent) {
        this.client = client;
        this.frame = new JDialog(parent, "Incoming Challenge", true);
        JPanel infoPanel = new JPanel(new SpringLayout());
        JLabel infoLabel = new JLabel("Received challenge from");
        infoLabel.setHorizontalAlignment(JLabel.CENTER);
        this.nameLabel = new JLabel("jZ");
        this.nameLabel.setHorizontalAlignment(JLabel.CENTER);
        this.nameLabel.setPreferredSize(new Dimension(250, 32));
        infoPanel.add(infoLabel);
        infoPanel.add(this.nameLabel);
        SwingUtils.makeCompactGrid(infoPanel, 2, 1, 8, 8);
        JPanel buttonsPanel = new JPanel(new SpringLayout());
        buttonsPanel.add(SwingUtils.makeButton("Accept challenge", (e) -> acceptChallenge()));
        buttonsPanel.add(SwingUtils.makeButton("Reject challenge", (e) -> rejectChallenge()));
        SwingUtils.makeCompactGrid(buttonsPanel, 1, 2, 8, 8);

        this.frame.getContentPane().setLayout(new BoxLayout(this.frame.getContentPane(), BoxLayout.Y_AXIS));
        this.frame.getContentPane().add(infoPanel);
        this.frame.getContentPane().add(buttonsPanel);
        this.frame.setResizable(false);
        this.frame.pack();
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                rejectChallenge();
            }
        });
    }

    public void show(ChallengeContext context) {
        log.trace("Showing incoming challenge window");
        challengeContext = context;
        nameLabel.setText(context.getChallenger());
        frame.setLocationRelativeTo(frame.getParent());
        frame.setVisible(true);
    }

    public void acceptChallenge() {
        client.respondChallengeAsync(challengeContext, ChallengeResponse.ACCEPTED)
                .thenApply((result) -> {
                    frame.setVisible(false);
                    client.showGameWindow();
                    return result;
                })
                .exceptionallyAsync((e) -> {
                    log.error("Challenge accept failed", e);
                    JOptionPane.showMessageDialog(frame, e.getCause().getCause().getMessage(), e.getCause().getMessage(), JOptionPane.ERROR_MESSAGE);
                    frame.setVisible(false);
                    return null;
                });
        ;
    }

    private void rejectChallenge() {
        client.respondChallengeAsync(challengeContext, ChallengeResponse.REJECTED)
                .thenApply((result) -> {
                    frame.setVisible(false);
                    return result;
                })
                .exceptionallyAsync((e) -> {
                    log.error("Challenge reject failed", e);
                    JOptionPane.showMessageDialog(frame, e.getCause().getCause().getMessage(), e.getCause().getMessage(), JOptionPane.ERROR_MESSAGE);
                    frame.setVisible(false);
                    return null;
                });
        ;
    }

    public void cancelChallenge() {
        log.error("Challenge cancelled");
        JOptionPane.showMessageDialog(frame, "Challenger has canceled the challenge", "Challenge", JOptionPane.INFORMATION_MESSAGE);
        frame.setVisible(false);
    }

    public void dispose() {
        frame.dispose();
    }
}
