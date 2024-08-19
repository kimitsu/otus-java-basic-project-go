package ru.otus.java.basic.project.client.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.function.Consumer;

/**
 * Custom component that displays a 19 by 19 go board, stones and territory markings,
 * also may draw a ghost stone under the mouse cursor and allows to set listener for clicking the board.
 */
public class Board extends JPanel {
    public static final int BOARD_SIZE = 19;
    private static final int[] HOSHI_POSITIONS = new int[]{3, 9, 15};
    private static final double ASPECT_RATIO = 12.0 / 13.0;
    private static final double HOSHI_RATIO = 1.0 / 10.0;
    private static final double STONE_RATIO = 10.0 / 21.0;
    private static final double LAST_MOVE_MARK_RATIO = 5.0 / 21.0;
    private static final double TERRITORY_MARK_RATIO = 4.0 / 21.0;
    private static final Color BOARD_COLOR = new Color(0xDDCC88);
    private static final Color LINE_COLOR = new Color(0x000000);
    private static final Color HOSHI_COLOR = new Color(0x000000);
    private static final int GHOST_STONE_ALPHA = 0x66000000;

    private boolean canPlayMove = false;
    private boolean canMarkDeadStones = false;
    private StoneColor moveColor = StoneColor.BLACK;
    private int ghostStoneX = -1;
    private int ghostStoneY = -1;
    private int lastGhostStoneX = 0;
    private int lastGhostStoneY = 0;
    private double gridWidth = 1.0;
    private double gridHeight = 1.0;
    private StoneColor[][] stones = new StoneColor[BOARD_SIZE][BOARD_SIZE];
    private Consumer<Point> moveListener = null;
    private Consumer<Point> markListener = null;
    private Point lastMove = null;
    private StoneColor[][] territory = null;

    public Board() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                stones[i][j] = StoneColor.EMPTY;
            }
        }
        this.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point mousePosition = getMousePosition();
                if (mousePosition == null || !canPlayMove) {
                    ghostStoneX = -1;
                    ghostStoneY = -1;
                } else {
                    ghostStoneX = (int) Math.round(20.0 * (mousePosition.x - (getWidth() - gridWidth) / 2.0) / gridWidth) - 1;
                    ghostStoneY = (int) Math.round(20.0 * (mousePosition.y - (getHeight() - gridHeight) / 2.0) / gridHeight) - 1;
                    if (ghostStoneX < 0 || ghostStoneX >= BOARD_SIZE || ghostStoneY < 0 || ghostStoneY >= BOARD_SIZE) {
                        ghostStoneX = -1;
                        ghostStoneY = -1;
                    }
                }
                if (ghostStoneX != lastGhostStoneX || ghostStoneY != lastGhostStoneY) {
                    repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
            }
        });
        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (!canPlayMove && !canMarkDeadStones) return;
                int stoneX = (int) Math.round(20.0 * (e.getPoint().x - (getWidth() - gridWidth) / 2.0) / gridWidth) - 1;
                int stoneY = (int) Math.round(20.0 * (e.getPoint().y - (getHeight() - gridHeight) / 2.0) / gridHeight) - 1;
                if (stoneX < 0 || stoneX >= BOARD_SIZE || stoneY < 0 || stoneY >= BOARD_SIZE) return;
                if (canPlayMove) {
                    if (!(stones[stoneY][stoneX] == StoneColor.EMPTY ||
                            stones[stoneY][stoneX] == StoneColor.FORBIDDEN_BLACK && moveColor == StoneColor.WHITE ||
                            stones[stoneY][stoneX] == StoneColor.FORBIDDEN_WHITE && moveColor == StoneColor.BLACK)) {

                        return;
                    }
                    if (moveListener != null) moveListener.accept(new Point(stoneX, stoneY));

                } else if (canMarkDeadStones) {
                    if (stones[stoneY][stoneX] != StoneColor.WHITE &&
                            stones[stoneY][stoneX] != StoneColor.BLACK &&
                            stones[stoneY][stoneX] != StoneColor.WHITE_DEAD &&
                            stones[stoneY][stoneX] != StoneColor.BLACK_DEAD) {
                        return;
                    }
                    if (markListener != null) markListener.accept(new Point(stoneX, stoneY));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ghostStoneX = -1;
                ghostStoneY = -1;
                if (ghostStoneX != lastGhostStoneX || ghostStoneY != lastGhostStoneY) {
                    repaint();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics context) {
        Graphics2D g = (Graphics2D) context.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(BOARD_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(LINE_COLOR);
        gridWidth = Math.min(getWidth(), getHeight() * ASPECT_RATIO);
        gridHeight = gridWidth / ASPECT_RATIO;
        double xStep = gridWidth / 20.0;
        double yStep = gridHeight / 20.0;
        int xBegin = (int) Math.round((getWidth() - gridWidth) / 2.0 + gridWidth / 20.0);
        int yBegin = (int) Math.round((getHeight() - gridHeight) / 2.0 + gridHeight / 20.0);
        int xEnd = getWidth() - xBegin;
        int yEnd = getHeight() - yBegin;
        double xPosition = xBegin;
        double yPosition = yBegin;
        for (int x = 0; x < BOARD_SIZE; x++) {
            g.drawLine((int) Math.round(xPosition), yBegin, (int) Math.round(xPosition), yEnd);
            xPosition += xStep;
        }
        for (int y = 0; y < BOARD_SIZE; y++) {
            g.drawLine(xBegin, (int) Math.round(yPosition), xEnd, (int) Math.round(yPosition));
            yPosition += yStep;
        }
        int hoshiRadius = (int) Math.round((Math.min(xStep, yStep) - 2) * HOSHI_RATIO);
        g.setColor(HOSHI_COLOR);
        for (int y = 0; y < HOSHI_POSITIONS.length; y++) {
            for (int x = 0; x < HOSHI_POSITIONS.length; x++) {
                xPosition = xBegin + xStep * HOSHI_POSITIONS[x];
                yPosition = yBegin + yStep * HOSHI_POSITIONS[y];
                g.fillArc((int) Math.round(xPosition) - hoshiRadius, (int) Math.round(yPosition) - hoshiRadius, hoshiRadius * 2 + 1, hoshiRadius * 2 + 1, 0, 360);

            }
        }
        drawStones(xStep, yStep, yBegin, xBegin, g);
        if (territory != null) drawTerritory(xStep, yStep, yBegin, xBegin, g);
        g.dispose();
        lastGhostStoneX = ghostStoneX;
        lastGhostStoneY = ghostStoneY;
    }

    private void drawStones(double xStep, double yStep, int yBegin, int xBegin, Graphics2D g) {
        double yPosition;
        double xPosition;
        int stoneRadius = (int) Math.round((Math.min(xStep, yStep) - 2) * STONE_RATIO);
        int lastMoveMarkRadius = (int) Math.round((Math.min(xStep, yStep) - 2) * LAST_MOVE_MARK_RATIO);
        yPosition = yBegin;
        for (int y = 0; y < BOARD_SIZE; y++) {
            xPosition = xBegin;
            for (int x = 0; x < BOARD_SIZE; x++) {
                if (stones[y][x].getShouldDraw()) {
                    g.setColor(stones[y][x].getFillColor());
                    g.fillArc((int) Math.round(xPosition) - stoneRadius, (int) Math.round(yPosition) - stoneRadius, stoneRadius * 2 + 1, stoneRadius * 2 + 1, 0, 360);
                    g.setColor(stones[y][x].getBorderColor());
                    g.drawArc((int) Math.round(xPosition) - stoneRadius, (int) Math.round(yPosition) - stoneRadius, stoneRadius * 2 + 1, stoneRadius * 2 + 1, 0, 360);
                    if (lastMove != null && lastMove.x == x && lastMove.y == y) {
                        g.setColor(stones[y][x].getLastMoveMarkColor());
                        g.drawArc((int) Math.round(xPosition) - lastMoveMarkRadius, (int) Math.round(yPosition) - lastMoveMarkRadius, lastMoveMarkRadius * 2 + 1, lastMoveMarkRadius * 2 + 1, 0, 360);
                    }
                } else if (canPlayMove && ghostStoneX == x && ghostStoneY == y && stones[y][x].canPlay(moveColor)) {
                    g.setColor(new Color(moveColor.getFillColor().getRGB() + GHOST_STONE_ALPHA, true));
                    g.fillArc((int) Math.round(xPosition) - stoneRadius, (int) Math.round(yPosition) - stoneRadius, stoneRadius * 2 + 1, stoneRadius * 2 + 1, 0, 360);
                    g.setColor(new Color(moveColor.getBorderColor().getRGB() + GHOST_STONE_ALPHA, true));
                    g.drawArc((int) Math.round(xPosition) - stoneRadius, (int) Math.round(yPosition) - stoneRadius, stoneRadius * 2 + 1, stoneRadius * 2 + 1, 0, 360);
                }
                xPosition += xStep;
            }
            yPosition += yStep;
        }
    }

    private void drawTerritory(double xStep, double yStep, int yBegin, int xBegin, Graphics2D g) {
        double yPosition;
        double xPosition;
        int markRadius = (int) Math.round((Math.min(xStep, yStep) - 2) * TERRITORY_MARK_RATIO);
        yPosition = yBegin;
        for (int y = 0; y < BOARD_SIZE; y++) {
            xPosition = xBegin;
            for (int x = 0; x < BOARD_SIZE; x++) {
                if (territory[y][x].getShouldDraw()) {
                    g.setColor(territory[y][x].getFillColor());
                    g.fillArc((int) Math.round(xPosition) - markRadius, (int) Math.round(yPosition) - markRadius, markRadius * 2 + 1, markRadius * 2 + 1, 0, 360);
                    g.setColor(territory[y][x].getBorderColor());
                    g.drawArc((int) Math.round(xPosition) - markRadius, (int) Math.round(yPosition) - markRadius, markRadius * 2 + 1, markRadius * 2 + 1, 0, 360);
                }
                xPosition += xStep;
            }
            yPosition += yStep;
        }
    }

    public void setMoveListener(Consumer<Point> moveListener) {
        this.moveListener = moveListener;
    }

    public void setMarkListener(Consumer<Point> markListener) {
        this.markListener = markListener;
    }

    public StoneColor getMoveColor() {
        return moveColor;
    }

    public void setMoveColor(StoneColor moveColor) {
        this.moveColor = moveColor;
    }

    public void setStone(Point point, StoneColor moveColor) {
        stones[point.y][point.x] = moveColor;
        repaint();
    }

    public void setStones(StoneColor[][] stones) {
        this.stones = stones;
        repaint();
    }

    public StoneColor[][] getStones() {
        return stones;
    }

    public void setCanPlayMove(boolean canPlayMove) {
        this.canPlayMove = canPlayMove;
    }

    public boolean getCanPlayMove() {
        return canPlayMove;
    }

    public void setCanMarkDeadStones(boolean canMarkDeadStones) {
        this.canMarkDeadStones = canMarkDeadStones;
    }

    public void setLastMove(Point point) {
        this.lastMove = point;
    }

    public void setTerritory(StoneColor[][] territory) {
        this.territory = territory;
    }
}
