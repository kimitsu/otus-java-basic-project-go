package ru.otus.java.basic.project.api.exceptions;

/**
 * Exception in case when a context id passed in a message is invalid.
 * For example, when a <code>ChallengeResponseClientMessage</code> specifies an id of non-existent challenge context.
 */
public class InvalidContextException extends Exception {
    public InvalidContextException(String message) {
        super(message);
    }
}
