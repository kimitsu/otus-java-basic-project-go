package ru.otus.java.basic.project.go.server.exceptions;

/**
 * An exception to indicate that the client is already logged in
 */
public class ClientAlreadyLoggedIn extends Exception {
    public ClientAlreadyLoggedIn() {
        super();
    }
}
