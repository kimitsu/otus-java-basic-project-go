package ru.otus.java.basic.project.api;

import ru.otus.java.basic.project.api.exceptions.MessageProcessingException;
import ru.otus.java.basic.project.api.messages.ClientServerMessage;

import java.io.IOException;

/**
 * Functional interface that contains a method to process a client-server message
 *
 * @param <T> Class of the message to process
 */
@FunctionalInterface
public interface MessageProcessor<T extends ClientServerMessage> {
    /**
     * Process an incoming client or server message. Perform any and all operations required in response to the message,
     * as well as send any necessary messages to the clients or the server, including error messages in reaction to
     * recoverable failure states.
     *
     * @param message a client-server message to process
     * @throws IOException                in case of network IO error while processing the message
     * @throws MessageProcessingException in case of any other error that resulted in unrecoverable failure
     */
    void process(T message) throws IOException, MessageProcessingException;
}
