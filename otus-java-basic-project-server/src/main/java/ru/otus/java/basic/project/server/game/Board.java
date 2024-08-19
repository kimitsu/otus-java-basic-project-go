package ru.otus.java.basic.project.server.game;

import ru.otus.java.basic.project.server.exceptions.IllegalMoveException;

import java.awt.*;
import java.util.ArrayList;

/**
 * Represents a 19 by 19 go board.
 * Stores board position (stones, last played move, dead stones and territory marking, amount of captured stones).
 * Keeps track of forbidden moves.
 * Allows to play moves, mark dead stones.
 * Calculates score.
 */
public class Board {
    public static final int BOARD_SIZE = 19;
    public static final int BOARD_EMPTY = 0;
    public static final int BOARD_FORBIDDEN_WHITE = 1;
    public static final int BOARD_FORBIDDEN_BLACK = 2;
    public static final int BOARD_WHITE = 3;
    public static final int BOARD_BLACK = 4;
    public static final int BOARD_WHITE_DEAD = 5;
    public static final int BOARD_BLACK_DEAD = 6;
    private static final int TERRITORY_NONE = 7;
    private static final int TERRITORY_WHITE = 8;
    private static final int TERRITORY_BLACK = 9;
    private static final int TERRITORY_CONFLICTED = 10;
    private final int[][] stones = new int[BOARD_SIZE][BOARD_SIZE];
    private final int[][] liberties = new int[BOARD_SIZE][BOARD_SIZE];
    private int[][] territory = null;
    private int whiteCaptures = 0;
    private int blackCaptures = 0;
    private int whiteTerritory = 0;
    private int blackTerritory = 0;

    private static final Point[] ADJACENT_POINTS = new Point[]{new Point(1, 0), new Point(0, -1), new Point(-1, 0), new Point(0, 1)};

    public void playMove(int moveX, int moveY, int stoneColor) throws IllegalMoveException {
        if (!isOnBoard(moveX, moveY) || !isBoardEmpty(moveX, moveY)) {
            throw new IllegalMoveException();
        }
        if (stones[moveY][moveX] == switch (stoneColor) {
            case BOARD_WHITE -> BOARD_FORBIDDEN_WHITE;
            case BOARD_BLACK -> BOARD_FORBIDDEN_BLACK;
            default -> throw new IllegalMoveException();
        }) {
            throw new IllegalMoveException();
        }
        stones[moveY][moveX] = stoneColor;
        updateAdjacentGroupLiberties(moveX, moveY, stoneColor);
        boolean[][] killed = new boolean[BOARD_SIZE][BOARD_SIZE];
        int killedStonesCount = killAdjacentGroups(moveX, moveY, stoneColor, killed);
        int moveGroupSize = recountLiberties(moveX, moveY, stoneColor);
        updateForbiddenMoves();
        if (killedStonesCount == 1 && moveGroupSize == 1) {
            updateForbiddenKo(killed, stoneColor);
        }
    }

    private void updateAdjacentGroupLiberties(int moveX, int moveY, int stoneColor) {
        boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];
        for (Point dPoint : ADJACENT_POINTS) {
            int dx = moveX + dPoint.x;
            int dy = moveY + dPoint.y;
            if (isOnBoard(dx, dy) && !isBoardEmpty(dx, dy) && stones[dy][dx] != stoneColor) {
                modifyLiberties(dx, dy, -1, true, visited);
            }
        }
    }

    private int killAdjacentGroups(int moveX, int moveY, int stoneColor, boolean[][] killed) {
        int killedStonesCount = 0;
        for (Point dPoint : ADJACENT_POINTS) {
            int dx = moveX + dPoint.x;
            int dy = moveY + dPoint.y;
            if (isOnBoard(dx, dy) && !isBoardEmpty(dx, dy) && stones[dy][dx] != stoneColor && liberties[dy][dx] == 0) {
                killedStonesCount += killStones(dx, dy, killed);
            }
        }
        switch (stoneColor) {
            case BOARD_WHITE -> whiteCaptures += killedStonesCount;
            case BOARD_BLACK -> blackCaptures += killedStonesCount;
        }
        return killedStonesCount;
    }

    private int recountLiberties(int moveX, int moveY, int stoneColor) {
        int stoneLiberties = 0;
        boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];
        ArrayList<Point> flood = new ArrayList<>();
        ArrayList<Point> history = new ArrayList<>();
        flood.add(new Point(moveX, moveY));
        while (!flood.isEmpty()) {
            Point point = flood.removeLast();
            history.add(point);
            visited[point.y][point.x] = true;
            for (Point dPoint : ADJACENT_POINTS) {
                int dx = point.x + dPoint.x;
                int dy = point.y + dPoint.y;
                if (isOnBoard(dx, dy) && !visited[dy][dx]) {
                    if (isBoardEmpty(dx, dy)) {
                        visited[dy][dx] = true;
                        stoneLiberties++;
                    } else if (stones[dy][dx] == stoneColor) {
                        flood.add(new Point(dx, dy));
                    }
                }
            }
        }
        for (Point point : history) {
            liberties[point.y][point.x] = stoneLiberties;
        }
        return history.size();
    }

    private void updateForbiddenMoves() {
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                if (!isBoardEmpty(x, y)) continue;
                boolean hasEmptyNeighbor = false;
                boolean hasLiveWhite = false;
                boolean hasLiveBlack = false;
                boolean hasWhiteInAtari = false;
                boolean hasBlackInAtari = false;
                for (Point dPoint : ADJACENT_POINTS) {
                    int dx = x + dPoint.x;
                    int dy = y + dPoint.y;
                    if (!isOnBoard(dx, dy)) continue;
                    if (isBoardEmpty(dx, dy)) {
                        hasEmptyNeighbor = true;
                        break;
                    } else if (liberties[dy][dx] == 1) {
                        switch (stones[dy][dx]) {
                            case BOARD_WHITE -> hasWhiteInAtari = true;
                            case BOARD_BLACK -> hasBlackInAtari = true;
                        }
                    } else {
                        switch (stones[dy][dx]) {
                            case BOARD_WHITE -> hasLiveWhite = true;
                            case BOARD_BLACK -> hasLiveBlack = true;
                        }
                    }
                }
                if (hasEmptyNeighbor) {
                    stones[y][x] = BOARD_EMPTY;
                } else if (!hasLiveWhite && !hasBlackInAtari) {
                    stones[y][x] = BOARD_FORBIDDEN_WHITE;
                } else if (!hasLiveBlack && !hasWhiteInAtari) {
                    stones[y][x] = BOARD_FORBIDDEN_BLACK;
                } else {
                    stones[y][x] = BOARD_EMPTY;
                }
            }
        }
    }

    private void updateForbiddenKo(boolean[][] killed, int stoneColor) {
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                if (killed[y][x]) stones[y][x] = switch (stoneColor) {
                    case BOARD_WHITE -> BOARD_FORBIDDEN_BLACK;
                    case BOARD_BLACK -> BOARD_FORBIDDEN_WHITE;
                    default -> throw new IllegalStateException("Unexpected value: " + stoneColor);
                };
            }
        }
    }

    private void modifyLiberties(int x, int y, int libs, boolean first, boolean[][] visited) {
        if (visited[y][x]) return;
        visited[y][x] = true;
        int stoneColor = stones[y][x];
        if (first) {
            liberties[y][x] += libs;
        } else {
            liberties[y][x] = libs;
        }
        for (Point point : ADJACENT_POINTS) {
            int dx = x + point.x;
            int dy = y + point.y;
            if (isOnBoard(dx, dy) && stones[dy][dx] == stoneColor) {
                modifyLiberties(dx, dy, liberties[y][x], false, visited);
            }
        }
    }

    private int killStones(int x, int y, boolean[][] killed) {
        if (killed[y][x]) return 0;
        int stoneCount = 1;
        int stoneColor = stones[y][x];
        stones[y][x] = BOARD_EMPTY;
        liberties[y][x] = 0;
        killed[y][x] = true;
        boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];
        for (Point point : ADJACENT_POINTS) {
            int dx = x + point.x;
            int dy = y + point.y;
            if (isOnBoard(dx, dy) && !isBoardEmpty(dx, dy) && stones[dy][dx] != stoneColor) {
                modifyLiberties(dx, dy, 1, true, visited);
            }
        }
        for (Point point : ADJACENT_POINTS) {
            int dx = x + point.x;
            int dy = y + point.y;
            if (isOnBoard(dx, dy) && stones[dy][dx] == stoneColor) {
                stoneCount += killStones(dx, dy, killed);
            }
        }
        return stoneCount;
    }

    public void resetCounting() {
        territory = null;
        whiteTerritory = 0;
        blackTerritory = 0;
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                switch (stones[y][x]) {
                    case BOARD_WHITE_DEAD -> {
                        stones[y][x] = BOARD_WHITE;
                        blackCaptures--;
                    }
                    case BOARD_BLACK_DEAD -> {
                        stones[y][x] = BOARD_BLACK;
                        whiteCaptures--;
                    }
                }
            }
        }
    }

    public void mark(int moveX, int moveY) throws IllegalMoveException {
        if (!isOnBoard(moveX, moveY) || isBoardEmpty(moveX, moveY)) {
            throw new IllegalMoveException();
        }
        int stoneColor = stones[moveY][moveX];
        int markedColor = switch (stoneColor) {
            case BOARD_WHITE -> BOARD_WHITE_DEAD;
            case BOARD_BLACK -> BOARD_BLACK_DEAD;
            case BOARD_WHITE_DEAD -> BOARD_WHITE;
            case BOARD_BLACK_DEAD -> BOARD_BLACK;
            default -> throw new IllegalStateException("Unexpected value: " + stoneColor);
        };
        int stoneCount = markStones(moveX, moveY, stoneColor, markedColor, new boolean[BOARD_SIZE][BOARD_SIZE]);
        switch (markedColor) {
            case BOARD_WHITE_DEAD -> blackCaptures += stoneCount;
            case BOARD_BLACK_DEAD -> whiteCaptures += stoneCount;
            case BOARD_WHITE -> blackCaptures -= stoneCount;
            case BOARD_BLACK -> whiteCaptures -= stoneCount;
        }
        updateScore();
    }

    public void updateScore() {
        boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];
        territory = new int[BOARD_SIZE][BOARD_SIZE];
        whiteTerritory = 0;
        blackTerritory = 0;
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                if (isBoardEmpty(x, y) || isDead(x, y)) {
                    ArrayList<Point> marked = new ArrayList<>();
                    int territoryColor = markTerritory(x, y, TERRITORY_NONE, visited, marked);
                    while (!marked.isEmpty()) {
                        Point point = marked.removeLast();
                        territory[point.y][point.x] = territoryColor;
                        switch (territoryColor) {
                            case TERRITORY_WHITE -> whiteTerritory++;
                            case TERRITORY_BLACK -> blackTerritory++;
                        }
                    }
                }
            }

        }
    }

    private int markTerritory(int x, int y, int territoryColor, boolean[][] visited, ArrayList<Point> marked) {
        if (visited[y][x]) return territoryColor;
        int newTerritoryColor = switch (stones[y][x]) {
            case BOARD_EMPTY, BOARD_FORBIDDEN_BLACK, BOARD_FORBIDDEN_WHITE -> TERRITORY_NONE;
            case BOARD_WHITE, BOARD_BLACK_DEAD -> TERRITORY_WHITE;
            case BOARD_BLACK, BOARD_WHITE_DEAD -> TERRITORY_BLACK;
            default -> throw new IllegalStateException("Unexpected value: " + stones[y][x]);
        };
        int updatedTerritoryColor = switch (territoryColor) {
            case TERRITORY_NONE -> newTerritoryColor;
            case TERRITORY_CONFLICTED -> territoryColor;
            case TERRITORY_WHITE, TERRITORY_BLACK ->
                    isBoardEmpty(x, y) || territoryColor == newTerritoryColor ? territoryColor : TERRITORY_CONFLICTED;
            default -> throw new IllegalStateException("Unexpected value: " + territoryColor);
        };
        if (!isBoardEmpty(x, y) && !isDead(x, y)) return updatedTerritoryColor;
        visited[y][x] = true;
        marked.add(new Point(x, y));
        for (Point point : ADJACENT_POINTS) {
            int dx = x + point.x;
            int dy = y + point.y;
            if (isOnBoard(dx, dy)) {
                updatedTerritoryColor = markTerritory(dx, dy, updatedTerritoryColor, visited, marked);
            }
        }
        return updatedTerritoryColor;
    }

    private int markStones(int x, int y, int stoneColor, int markedColor, boolean[][] visited) {
        if (visited[y][x]) return 0;
        int stoneCount = 0;
        visited[y][x] = true;
        if (!isBoardEmpty(x, y)) {
            stones[y][x] = markedColor;
            stoneCount++;
        }
        for (Point point : ADJACENT_POINTS) {
            int dx = x + point.x;
            int dy = y + point.y;
            if (isOnBoard(dx, dy) && (isBoardEmpty(dx, dy) || stones[dy][dx] == stoneColor)) {
                stoneCount += markStones(dx, dy, stoneColor, markedColor, visited);
            }
        }
        return stoneCount;
    }

    public boolean isBoardEmpty(int x, int y) {
        return stones[y][x] < BOARD_WHITE;
    }

    private boolean isOnBoard(int x, int y) {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
    }

    private boolean isDead(int x, int y) {
        return stones[y][x] == BOARD_WHITE_DEAD || stones[y][x] == BOARD_BLACK_DEAD;
    }

    public int[][] getStones() {
        return stones;
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

    public int[][] getTerritory() {
        return territory;
    }

}
