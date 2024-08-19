package ru.otus.java.basic.project.go.client.exceptions;

/**
 * Used to complete futures exceptionally in cases when the server responds with an <code>ErrorServerMessage</code>.
 */
public class ServerError extends Exception {
    public ServerError(String string) {
        super(string);
    }
}
