package ru.otus.java.basic.project.api.enums;

/**
 * Represents a phase of the game.
 */
public enum GameState {
    /**
     * It's black's turn, black can play a move or pass. Both players may resign.
     */
    BLACK_TO_MOVE,
    /**
     * It's white's turn, white can play a move or pass. Both players may resign.
     */
    WHITE_TO_MOVE,
    /**
     * It's score counting phase. Both players can mark or unmark dead stones.
     * Both players may finish counting by clicking Done button.
     */
    COUNTING,
    /**
     * The counting is finished, a message should be displayed with the score,
     * and the game should be removed server-side and eventually client-side.
     */
    FINISHED,
    /**
     * White has resigned, a message should be displayed with the score,
     * and the game should be removed server-side and eventually client-side.
     */
    WHITE_RESIGNED,
    /**
     * Black has resigned, a message should be displayed with the score,
     * and the game should be removed server-side and eventually client-side.
     */
    BLACK_RESIGNED;
}
