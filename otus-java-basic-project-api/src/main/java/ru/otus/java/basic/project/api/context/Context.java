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

    public <T extends ClientServerMessage> void setListener(Class<T> messageClass, MessageProcessor<T> listener) {
        listeners.put(messageClass, (MessageProcessor<ClientServerMessage>) listener);
    }

    public <T extends ClientServerMessage> void removeListener(Class<T> messageClass) {
        listeners.remove(messageClass);
    }

    public MessageProcessor<ClientServerMessage> getListener(Class<?> messageClass) {
        return listeners.get(messageClass);
    }
}



