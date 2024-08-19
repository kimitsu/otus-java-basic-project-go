package ru.otus.java.basic.project.api.exceptions;

/**
 * Any exception during message processing, such as incorrect format, or application state, etc., except for IO exceptions
 */
public class MessageProcessingException extends Exception {
    public MessageProcessingException(String message) {
        super(message);
    }
    public MessageProcessingException(Throwable cause) {
        super(cause);
    }
}
