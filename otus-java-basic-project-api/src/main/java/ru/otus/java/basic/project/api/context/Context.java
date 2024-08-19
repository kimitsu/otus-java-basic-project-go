package ru.otus.java.basic.project.api.context;

import ru.otus.java.basic.project.api.MessageProcessor;
import ru.otus.java.basic.project.api.messages.ClientServerMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * A context is supposed to keep track of a series of client-server messages that are related to a particular topic,
 * and may store some relevant information (see <code>ChallengeContext</code>, <code>GameContext</code>)
 * Contexts allow setting, removing and getting listeners for particular message classes,
 * which are then invoked by message dispatchers.
 * Each client-server message contains a context id, which helps to identify the relevant context.
 * If the context is not found, usually, some sort of default context will be used.
 */
public sealed class Context permits ChallengeContext, GameContext {
    private final Map<Class<? extends ClientServerMessage>, MessageProcessor<ClientServerMessage>> listeners = new HashMap<>();

    /**
     * Generates a hopefully unique id
     * @return a hopefully unique id
     */
    public static Long getNewId() {
        Random rng = new Random();
        return rng.nextLong();
    }

    private Long id;

    public Context(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    /**
     * Stores a listener for a particular message class
     *
     * @param messageClass a client-server message class to listen to
     * @param listener     a message processor
     */
    public <T extends ClientServerMessage> void setListener(Class<T> messageClass, MessageProcessor<T> listener) {
        listeners.put(messageClass, (MessageProcessor<ClientServerMessage>) listener);
    }

    /**
     * Removes a stored listener
     *
     * @param messageClass a client-server message class to stop listening to
     */
    public <T extends ClientServerMessage> void removeListener(Class<T> messageClass) {
        listeners.remove(messageClass);
    }

    /**
     * Retrieves a stored listener
     *
     * @param messageClass a client-server message class to get listening for
     * @return a message processor, listening to the specified message class
     */
    public MessageProcessor<ClientServerMessage> getListener(Class<?> messageClass) {
        return listeners.get(messageClass);
    }
}



