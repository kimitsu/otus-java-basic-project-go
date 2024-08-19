package ru.otus.java.basic.project.go.server.game;

import ru.otus.java.basic.project.go.api.exceptions.MessageProcessingException;
import ru.otus.java.basic.project.go.server.exceptions.ClientNotFoundException;

/**
 * A functional interface with a method to react to game state update events by sending the game state to the clients.
 */
@FunctionalInterface
public interface GameUpdateListener {
    /**
     * Reacts to an update in a game state by sending the game state update events to the clients.
     *
     * @param game a new game state
     * @throws ClientNotFoundException    in case if one of the clients is not found
     * @throws MessageProcessingException in case of other message processing errors
     */
    void update(Game game) throws ClientNotFoundException, MessageProcessingException;
}
