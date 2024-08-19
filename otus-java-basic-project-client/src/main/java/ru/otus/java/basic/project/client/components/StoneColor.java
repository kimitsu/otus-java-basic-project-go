package ru.otus.java.basic.project.client.components;

import java.awt.*;

/**
 * Represents possible states of an intersection of a go board.
 * Contains information on how to draw it, such as fill and border color,
 * and also whether a move is allowed to be played in that intersection.
 */
public enum StoneColor {
    EMPTY(false, null, null, null, true, true),
    FORBIDDEN_WHITE(false, null, null, null, false, true),
    FORBIDDEN_BLACK(false, null, null, null, true, false),
    WHITE(true, new Color(0xEEEEEE), new Color(0x111111), new Color(0x666666), false, false),
    BLACK(true, new Color(0x111111), new Color(0x000000), new Color(0xAAAAAA), false, false),
    WHITE_DEAD(true, new Color(0x88EEEEEE, true), new Color(0x88111111, true), new Color(0x666666), false, false),
    BLACK_DEAD(true, new Color(0x88111111, true), new Color(0x88000000, true), new Color(0xAAAAAA), false, false),
    TERRITORY_NONE(false, null, null, null, false, false),
    TERRITORY_WHITE(true, new Color(0xEEEEEE), new Color(0x111111), null, false, false),
    TERRITORY_BLACK(true, new Color(0x111111), new Color(0x000000), null, false, false),
    TERRITORY_CONFLICTED(true, new Color(0x00000000, true), new Color(0x88000000, true), null, false, false);

    private final boolean shouldDraw;
    private final Color fillColor;
    private final Color borderColor;
    private final Color lastMoveMarkColor;
    private final boolean canPlayWhite;
    private final boolean canPlayBlack;

    StoneColor(boolean shouldDraw, Color fillColor, Color borderColor, Color lastMoveMarkColor, boolean canPlayWhite, boolean canPlayBlack) {
        this.shouldDraw = shouldDraw;
        this.fillColor = fillColor;
        this.borderColor = borderColor;
        this.lastMoveMarkColor = lastMoveMarkColor;
        this.canPlayWhite = canPlayWhite;
        this.canPlayBlack = canPlayBlack;
    }


    public Color getFillColor() {
        return fillColor;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public Color getLastMoveMarkColor() {
        return lastMoveMarkColor;
    }

    public boolean getShouldDraw() {
        return shouldDraw;
    }

    public boolean canPlay(StoneColor stoneColor) {
        return switch (stoneColor) {
            case WHITE -> canPlayWhite;
            case BLACK -> canPlayBlack;
            default -> throw new IllegalArgumentException("Invalid stone color");
        };
    }

}
