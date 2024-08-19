package ru.otus.java.basic.project.server.game;

import ru.otus.java.basic.project.api.context.Context;
import ru.otus.java.basic.project.api.context.GameContext;
import ru.otus.java.basic.project.api.enums.GameState;
import ru.otus.java.basic.project.api.enums.MoveType;
import ru.otus.java.basic.project.api.exceptions.MessageProcessingException;
import ru.otus.java.basic.project.api.messages.ClientServerMessage;
import ru.otus.java.basic.project.api.messages.client.GameMoveClientMessage;
import ru.otus.java.basic.project.api.messages.server.GameInfoServerMessage;
import ru.otus.java.basic.project.api.messages.server.GameStateServerMessage;
import ru.otus.java.basic.project.server.ClientConnection;
import ru.otus.java.basic.project.server.exceptions.ClientNotFoundException;
import ru.otus.java.basic.project.server.exceptions.IllegalMoveException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a single go match.
 * Contains information about the game, manages game state, processes client messages related to the game.
 */
public class Game {
    private final Long contextId;
    private final String whitePlayer;
    private final String blackPlayer;

    private final Board board = new Board();

    private GameState state = GameState.BLACK_TO_MOVE;
    private GameUpdateListener updateListener;

    private MoveType lastMoveType = null;
    private int lastMoveX = 0;
    private int lastMoveY = 0;
    private final BigDecimal komi = new BigDecimal("6.5");

    public Game(String challenger, String challenged) {
        double random = Math.random();
        if (random < 0.5) {
            this.whitePlayer = challenger;
            this.blackPlayer = challenged;
        } else {
            this.whitePlayer = challenged;
            this.blackPlayer = challenger;
        }
        contextId = Context.getNewId();
    }

    public Long getContextId() {
        return contextId;
    }

    public GameContext toGameContext() {
        return new GameContext(contextId, whitePlayer, blackPlayer);
    }

    public void processMessage(ClientConnection client, GameMoveClientMessage gameMoveClientMessage) throws MessageProcessingException, ClientNotFoundException {
        synchronized (this) {
            switch (state) {
                case BLACK_TO_MOVE, WHITE_TO_MOVE -> processMovePassResign(client, gameMoveClientMessage);
                case COUNTING -> processMarkResumeDone(client, gameMoveClientMessage);
            }
            if (updateListener != null) updateListener.update(this);
        }
    }

    private void processMarkResumeDone(ClientConnection client, GameMoveClientMessage gameMoveClientMessage) throws MessageProcessingException {
        switch (gameMoveClientMessage.getMoveType()) {
            case MARK -> processMarkStone(client, gameMoveClientMessage);
            case DONE -> processMarkDone(client, gameMoveClientMessage);
            case RESUME -> processMarkResume(client, gameMoveClientMessage);
            default -> throw new MessageProcessingException("Illegal move type");
        }
    }

    private void processMarkStone(ClientConnection client, GameMoveClientMessage gameMoveClientMessage) throws MessageProcessingException {
        try {
            board.mark(gameMoveClientMessage.getX(), gameMoveClientMessage.getY());
        } catch (IllegalMoveException e) {
            throw new MessageProcessingException(e);
        }
    }
    private void processMarkDone(ClientConnection client, GameMoveClientMessage gameMoveClientMessage) {
        state = GameState.FINISHED;
    }

    private void processMarkResume(ClientConnection client, GameMoveClientMessage gameMoveClientMessage) {
        board.resetCounting();
        state = Objects.equals(client.getName(), whitePlayer) ? GameState.BLACK_TO_MOVE : GameState.WHITE_TO_MOVE;
    }

    private void processMovePassResign(ClientConnection client, GameMoveClientMessage gameMoveClientMessage) throws MessageProcessingException {
        switch (gameMoveClientMessage.getMoveType()) {
            case STONE -> processMoveStone(client, gameMoveClientMessage);
            case PASS -> processMovePass(client, gameMoveClientMessage);
            case RESIGN -> processResign(client, gameMoveClientMessage);
            default -> throw new MessageProcessingException("Illegal move type");
        }
        lastMoveType = gameMoveClientMessage.getMoveType();
        lastMoveX = gameMoveClientMessage.getX();
        lastMoveY = gameMoveClientMessage.getY();
    }


    private void processMoveStone(ClientConnection client, GameMoveClientMessage gameMoveClientMessage) throws MessageProcessingException {
        throwIfCannotMove(client);
        int x = gameMoveClientMessage.getX();
        int y = gameMoveClientMessage.getY();
        try {
            board.playMove(x, y, switch (state) {
                case BLACK_TO_MOVE -> Board.BOARD_BLACK;
                case WHITE_TO_MOVE -> Board.BOARD_WHITE;
                default -> throw new MessageProcessingException("Unexpected state: " + state);
            });
        } catch (IllegalMoveException e) {
            throw new MessageProcessingException(e);
        }
        state = switch (state) {
            case BLACK_TO_MOVE -> GameState.WHITE_TO_MOVE;
            case WHITE_TO_MOVE -> GameState.BLACK_TO_MOVE;
            default -> throw new MessageProcessingException("Unexpected state: " + state);
        };
    }

    private void processMovePass(ClientConnection client, GameMoveClientMessage gameMoveClientMessage) throws MessageProcessingException {
        throwIfCannotMove(client);
        if (lastMoveType == MoveType.PASS) {
            state = GameState.COUNTING;
            board.updateScore();
        } else {
            state = switch (state) {
                case BLACK_TO_MOVE -> GameState.WHITE_TO_MOVE;
                case WHITE_TO_MOVE -> GameState.BLACK_TO_MOVE;
                default -> throw new MessageProcessingException("Unexpected state: " + state);
            };
        }
    }

    private void processResign(ClientConnection client, GameMoveClientMessage gameMoveClientMessage) {
        state = Objects.equals(client.getName(), whitePlayer) ? GameState.WHITE_RESIGNED : GameState.BLACK_RESIGNED;
    }

    private void throwIfCannotMove(ClientConnection client) throws MessageProcessingException {
        if (switch (state) {
            case WHITE_TO_MOVE -> !whitePlayer.equals(client.getName());
            case BLACK_TO_MOVE -> !blackPlayer.equals(client.getName());
            default -> true;
        }) {
            throw new MessageProcessingException("Move out of order");
        }
    }

    public void setUpdateListener(GameUpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    public String getWhitePlayer() {
        return whitePlayer;
    }

    public String getBlackPlayer() {
        return blackPlayer;
    }

    public GameStateServerMessage toGameStateMessage() {
        return new GameStateServerMessage(
                contextId,
                state,
                board.getStones(),
                board.getTerritory(),
                board.getWhiteCaptures(),
                board.getBlackCaptures(),
                board.getWhiteTerritory(),
                board.getBlackTerritory(),
                komi,
                lastMoveType,
                lastMoveX,
                lastMoveY);
    }

    public ClientServerMessage toGameInfoMessage(Long requestContextId) {
        return new GameInfoServerMessage(requestContextId, contextId, whitePlayer, blackPlayer);
    }

    public boolean isFinished() {
        return state == GameState.FINISHED || state == GameState.BLACK_RESIGNED || state == GameState.WHITE_RESIGNED;
    }
}
