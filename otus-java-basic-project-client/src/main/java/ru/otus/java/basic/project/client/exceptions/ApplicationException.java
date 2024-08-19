package ru.otus.java.basic.project.client.exceptions;

/**
 * Represents any exception cause by irregular application behaviour
 */
public class ApplicationException extends RuntimeException {
    public ApplicationException(String message) {
        super(message);
    }
    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
