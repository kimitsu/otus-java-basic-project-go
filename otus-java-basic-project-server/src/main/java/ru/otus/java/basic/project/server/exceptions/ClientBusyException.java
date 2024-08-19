package ru.otus.java.basic.project.server.exceptions;


/**
 * An exception in case when the client turns out to be unable to perform in a desired way due to a previous engagement.
 */
public class ClientBusyException extends Exception {
    public ClientBusyException() {
        super();
    }
}
