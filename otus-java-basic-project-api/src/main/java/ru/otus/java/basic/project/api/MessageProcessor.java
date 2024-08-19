package ru.otus.java.basic.project.api;

import ru.otus.java.basic.project.api.exceptions.MessageProcessingException;

import java.io.IOException;

/**
 * Functional interface that contains a method to process a client-server message
 *
 * @param <T> Class of the message to process
 */
@FunctionalInterface
public interface MessageProcessor<T> {
    void process(T message) throws MessageProcessingException, IOException;
}
