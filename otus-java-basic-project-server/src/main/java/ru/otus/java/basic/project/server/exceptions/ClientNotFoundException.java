package ru.otus.java.basic.project.server.exceptions;

/**
 * An exception in case if the client is not found on the server's clients list.
 */
public class ClientNotFoundException extends Exception {
    public ClientNotFoundException() {
        super();
    }
}
