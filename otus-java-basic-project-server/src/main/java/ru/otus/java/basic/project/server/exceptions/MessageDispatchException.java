package ru.otus.java.basic.project.server.exceptions;

/**
 * An exception in case of a failure to find an appropriate listener for the message.
 */
public class MessageDispatchException extends Exception {
    public MessageDispatchException(String message) {
        super(message);
    }
}
