package ru.otus.java.basic.project.api.enums;

/**
 * Type of move sent in the <code>GameMoveClientMessage</code>.
 */
public enum MoveType {
    /**
     * Play a stone in a particular spot on the board.
     */
    STONE,
    /**
     * Pass the turn to the opponent.
     */
    PASS,
    /**
     * Mark or unmark a particular group of stones as dead.
     */
    MARK,
    /**
     * Finish the counting of the score and proceed to results.
     */
    DONE,
    /**
     * Abort score counting and return back to the game.
     */
    RESUME,
    /**
     * Resign the game.
     */
    RESIGN;
}
