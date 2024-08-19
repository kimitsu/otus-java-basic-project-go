package ru.otus.java.basic.project.api.messages.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.otus.java.basic.project.api.messages.ClientServerMessage;

import java.util.Objects;

public class GameInfoServerMessage extends ClientServerMessage {
    @JsonProperty("gc")
    private Long gameContext;
    @JsonProperty("wp")
    private String whitePlayer;
    @JsonProperty("bp")
    private String blackPlayer;

    public GameInfoServerMessage() {
        super();
    }

    public GameInfoServerMessage(Long context, Long gameContext, String whitePlayer, String blackPlayer) {
        super(context);
        this.gameContext = gameContext;
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
    }

    public void setGameContext(Long gameContext) {
        this.gameContext = gameContext;
    }

    public void setWhitePlayer(String whitePlayer) {
        this.whitePlayer = whitePlayer;
    }

    public void setBlackPlayer(String blackPlayer) {
        this.blackPlayer = blackPlayer;
    }

    public Long getGameContext() {
        return gameContext;
    }

    public String getWhitePlayer() {
        return whitePlayer;
    }

    public String getBlackPlayer() {
        return blackPlayer;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        GameInfoServerMessage message = (GameInfoServerMessage) obj;
        if (!Objects.equals(this.gameContext, message.gameContext)) return false;
        if (!Objects.equals(this.whitePlayer, message.whitePlayer)) return false;
        if (!Objects.equals(this.blackPlayer, message.blackPlayer)) return false;
        return true;
    }
}
