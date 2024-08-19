package ru.otus.java.basic.project.api.messages.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.otus.java.basic.project.api.enums.GameState;
import ru.otus.java.basic.project.api.enums.MoveType;
import ru.otus.java.basic.project.api.messages.ClientServerMessage;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;

public class GameStateServerMessage extends ClientServerMessage {
    @JsonProperty("gs")
    private GameState gameState;
    @JsonProperty("s")
    private int[][] stones;
    //    @JsonProperty("bl")
//    private int[][] boardLiberties;
    @JsonProperty("t")
    private int[][] territory;
    @JsonProperty("wc")
    private int whiteCaptures;
    @JsonProperty("bc")
    private int blackCaptures;
    @JsonProperty("wt")
    private int whiteTerritory;
    @JsonProperty("bt")
    private int blackTerritory;
    @JsonProperty("k")
    private BigDecimal komi;
    @JsonProperty("mt")
    private MoveType lastMoveType;
    @JsonProperty("mx")
    private int lastMoveX;
    @JsonProperty("my")
    private int lastMoveY;

    public GameStateServerMessage() {
        super();
    }

    public GameStateServerMessage(Long contextId, GameState gameState, int[][] stones, int[][] territory, int whiteCaptures, int blackCaptures, int whiteTerritory, int blackTerritory, BigDecimal komi, MoveType lastMoveType, int lastMoveX, int lastMoveY) {
        super(contextId);
        this.gameState = gameState;
        this.stones = stones;
        this.territory = territory;
//        this.boardLiberties = boardLiberties;
        this.whiteCaptures = whiteCaptures;
        this.blackCaptures = blackCaptures;
        this.whiteTerritory = whiteTerritory;
        this.blackTerritory = blackTerritory;
        this.komi = komi;
        this.lastMoveType = lastMoveType;
        this.lastMoveX = lastMoveX;
        this.lastMoveY = lastMoveY;
    }

    public GameState getGameState() {
        return gameState;
    }

    public int[][] getStones() {
        return stones;
    }

    public int[][] getTerritory() {
        return territory;
    }

    public int getWhiteCaptures() {
        return whiteCaptures;
    }

    public int getBlackCaptures() {
        return blackCaptures;
    }

    public int getWhiteTerritory() {
        return whiteTerritory;
    }

    public int getBlackTerritory() {
        return blackTerritory;
    }

    public BigDecimal getKomi() {
        return komi;
    }

    public MoveType getLastMoveType() {
        return lastMoveType;
    }

    public int getLastMoveX() {
        return lastMoveX;
    }

    public int getLastMoveY() {
        return lastMoveY;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        GameStateServerMessage message = (GameStateServerMessage) obj;
        if (!Objects.equals(this.gameState, message.gameState)) return false;
        if (!Objects.equals(this.whiteCaptures, message.whiteCaptures)) return false;
        if (!Objects.equals(this.blackCaptures, message.blackCaptures)) return false;
        if (!Objects.equals(this.whiteTerritory, message.whiteTerritory)) return false;
        if (!Objects.equals(this.blackTerritory, message.blackTerritory)) return false;
        if (!Objects.equals(this.komi, message.komi)) return false;
        if (!Objects.equals(this.lastMoveType, message.lastMoveType)) return false;
        if (!Objects.equals(this.lastMoveX, message.lastMoveX)) return false;
        if (!Objects.equals(this.lastMoveY, message.lastMoveY)) return false;
        if (!Arrays.deepEquals(message.stones, this.stones)) return false;
        if (!Arrays.deepEquals(message.territory, this.territory)) return false;
        //if (!Arrays.deepEquals(message.boardLiberties, this.boardLiberties)) return false;
        return true;
    }
}
