package ru.otus.java.basic.project.api.messages.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.otus.java.basic.project.api.enums.MoveType;
import ru.otus.java.basic.project.api.messages.ClientServerMessage;

import java.util.Objects;

public class GameMoveClientMessage extends ClientServerMessage {
    @JsonProperty("m")
    private MoveType moveType;
    @JsonProperty("x")
    private int x;
    @JsonProperty("y")
    private int y;

    public GameMoveClientMessage() {
        super();
    }

    public GameMoveClientMessage(Long context, MoveType moveType, int x, int y) {
        super(context);
        this.moveType = moveType;
        this.x = x;
        this.y = y;
    }

    public void setMoveType(MoveType moveType) {
        this.moveType = moveType;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public MoveType getMoveType() {
        return moveType;
    }

    public Integer getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        GameMoveClientMessage message = (GameMoveClientMessage) obj;
        if (!Objects.equals(this.moveType, message.moveType)) return false;
        if (!Objects.equals(this.x, message.x)) return false;
        if (!Objects.equals(this.y, message.y)) return false;
        return true;
    }
}
