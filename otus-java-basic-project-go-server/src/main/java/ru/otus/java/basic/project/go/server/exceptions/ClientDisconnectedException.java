package ru.otus.java.basic.project.go.server.exceptions;

/**
 * An exception to signal that the client connection is closed.
 */
public class ClientDisconnectedException extends Exception {
    public ClientDisconnectedException() {
        super();
    }
}
