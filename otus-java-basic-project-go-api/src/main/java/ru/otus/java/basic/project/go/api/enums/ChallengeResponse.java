package ru.otus.java.basic.project.go.api.enums;

/**
 * A kind of challenge response that the challenged client sends back to the server.
 */
public enum ChallengeResponse {
    /**
     * Challenge is accepted, a game should be started between the clients.
     */
    ACCEPTED,
    /**
     * Challenge is rejected, a message should be displayed to the challenger, and challenge windows should be closed.
     */
    REJECTED,
    /**
     * The challenged is unable to accept challenge, a message should be displayed to the challenger,
     * and challenge windows should be closed.
     */
    BUSY;
}
