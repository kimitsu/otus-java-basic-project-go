package ru.otus.java.basic.project.server.exceptions;

/**
 * An exception to indicate a move illegal in relevance to the current game board and state.
 */
public class IllegalMoveException extends Exception {
    public IllegalMoveException() {
        super();
    }
}
