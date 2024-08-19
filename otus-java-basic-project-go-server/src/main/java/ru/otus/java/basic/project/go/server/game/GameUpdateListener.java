package ru.otus.java.basic.project.go.server.game;

import ru.otus.java.basic.project.go.api.exceptions.MessageProcessingException;
import ru.otus.java.basic.project.go.server.exceptions.ClientNotFoundException;

@FunctionalInterface
public interface GameUpdateListener {
    void update(Game game) throws ClientNotFoundException, MessageProcessingException;
}
