package ru.otus.java.basic.project.go.server.exceptions;

/**
 * Any exception during authentication, such as incorrect credentials,
 * or errors due to the authentication provider failures.
 */
public class AuthenticationException extends Exception {
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(Throwable cause) {
        super(cause);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
