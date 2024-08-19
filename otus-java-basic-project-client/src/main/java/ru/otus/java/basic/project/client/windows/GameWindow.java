package ru.otus.java.basic.project.client.windows;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.project.api.context.GameContext;
import ru.otus.java.basic.project.api.enums.GameState;
import ru.otus.java.basic.project.api.enums.MoveType;
import ru.otus.java.basic.project.api.messages.server.ErrorServerMessage;
import ru.otus.java.basic.project.api.messages.server.GameStateServerMessage;
import ru.otus.java.basic.project.client.Client;
import ru.otus.java.basic.project.client.SwingUtils;
import ru.otus.java.basic.project.client.components.Board;
import ru.otus.java.basic.project.client.components.StoneColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class GameWindow {
    private final static Logger log = LogManager.getLogger(GameWindow.class);
    private final Client client;
    private final JFrame frame;
    private final Board board;
    private final JLabel titleLabel;
    private final JLabel moveLabel;
    private final JLabel scoreLabel;
    private final JButton passResumeButton;
    private final JButton resignDoneButton;
    private GameContext gameContext = null;
    private GameState state = null;
    private String whitePlayer = null;
    private String blackPlayer = null;
    private GameStateServerMessage lastMessage = null;

    public GameWindow(Client client) {
        this.client = client;
        this.frame = new JFrame("Game");
        this.titleLabel = new JLabel("Loading...");
        this.titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.moveLabel = new JLabel("Loading...");
        this.moveLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.scoreLabel = new JLabel("Loading...");
        this.scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.board = new Board();
        this.board.setPreferredSize(new Dimension(500, 500));
        this.board.setMoveListener(this::playMove);
        this.board.setMarkListener(this::markDeadStones);
        JPanel contentPanel = new JPanel(new SpringLayout());
        contentPanel.add(this.titleLabel);
        contentPanel.add(this.moveLabel);
        contentPanel.add(this.board);
        contentPanel.add(this.scoreLabel);
        SwingUtils.makeCompactGrid(contentPanel, 4, 1, 8, 8);
        JPanel buttonsPanel = new JPanel(new SpringLayout());
        this.passResumeButton = SwingUtils.makeButton("Resume", (_) -> playPassOrResume());
        this.resignDoneButton = SwingUtils.makeButton("Close", (_) -> resignOrDone());
        buttonsPanel.add(this.passResumeButton);
        buttonsPanel.add(this.resignDoneButton);
        SwingUtils.makeCompactGrid(buttonsPanel, 1, 2, 12, 8);
        this.frame.getContentPane().setLayout(new BoxLayout(this.frame.getContentPane(), BoxLayout.Y_AXIS));
        this.frame.getContentPane().add(contentPanel);
        this.frame.getContentPane().add(buttonsPanel);
        this.frame.pack();
        this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeWindow();
            }
        });
    }

    private void closeWindow() {
        if (state == GameState.WHITE_TO_MOVE || state == GameState.BLACK_TO_MOVE) {
            if (JOptionPane.showConfirmDialog(frame, "Resign the game?", "Abandon game", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                resignOrDone();
                frame.setVisible(false);
                client.enableLobby();
            }
        } else if (state == GameState.COUNTING) {
            if (JOptionPane.showConfirmDialog(frame, "Finish counting and accept results as is?", "Abandon game", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                resignOrDone();
                frame.setVisible(false);
                client.enableLobby();
            }
        } else if (state != null) {
            frame.setVisible(false);
            client.enableLobby();
        }
    }

    void setCanPlayMove(boolean canPlayMove) {
        board.setCanPlayMove(canPlayMove);
        passResumeButton.setEnabled(canPlayMove);
    }

    void setCanMarkDeadStones(boolean canMarkDeadStones) {
        board.setCanMarkDeadStones(canMarkDeadStones);
        passResumeButton.setEnabled(canMarkDeadStones);
    }

    private void setCanResignOrDone(boolean canResignOrDone) {
        resignDoneButton.setEnabled(canResignOrDone);
    }

    private void playMove(Point point) {
        setCanPlayMove(false);
        client.playMoveAsync(gameContext, MoveType.STONE, point.x, point.y);
    }

    private void playPassOrResume() {
        switch (state) {
            case WHITE_TO_MOVE, BLACK_TO_MOVE -> {
                setCanPlayMove(false);
                client.playMoveAsync(gameContext, MoveType.PASS, 0, 0);
            }
            case COUNTING -> {
                setCanMarkDeadStones(false);
                client.playMoveAsync(gameContext, MoveType.RESUME, 0, 0);
            }
        }
    }

    private void resignOrDone() {
        switch (state) {
            case WHITE_TO_MOVE, BLACK_TO_MOVE -> {
                setCanPlayMove(false);
                setCanResignOrDone(false);
                client.playMoveAsync(gameContext, MoveType.RESIGN, 0, 0);
            }
            case COUNTING -> {
                setCanMarkDeadStones(false);
                setCanResignOrDone(false);
                client.playMoveAsync(gameContext, MoveType.DONE, 0, 0);
            }
            case FINISHED, WHITE_RESIGNED, BLACK_RESIGNED -> {
                closeWindow();
            }
        }
    }

    private void markDeadStones(Point point) {
        setCanMarkDeadStones(false);
        client.playMoveAsync(gameContext, MoveType.MARK, point.x, point.y);
    }

    public void show(Component parent) {
        log.trace("Showing game window");
        frame.setTitle(STR."Game - \{client.getName()}");
        titleLabel.setText("Loading...");
        scoreLabel.setText("");
        moveLabel.setText("");
        passResumeButton.setText("Pass");
        resignDoneButton.setText("Resign");
        whitePlayer = null;
        blackPlayer = null;
        state = null;
        lastMessage = null;
        setCanMarkDeadStones(false);
        setCanPlayMove(false);
        setCanResignOrDone(false);
        frame.setLocationRelativeTo(parent);
        frame.setVisible(true);
        client.requestGameInfoAsync()
                .thenApply((result) -> {
                    gameContext = result;
                    gameContext.setListener(ErrorServerMessage.class, this::displayError);
                    whitePlayer = gameContext.getWhitePlayer();
                    blackPlayer = gameContext.getBlackPlayer();
                    titleLabel.setText(STR."(W) \{whitePlayer} | (B) \{blackPlayer}");
                    return result;
                })
                .exceptionallyAsync((e) -> {
                    JOptionPane.showMessageDialog(frame, e.getCause().getCause().getMessage(), e.getCause().getMessage(), JOptionPane.ERROR_MESSAGE);
                    frame.setVisible(false);
                    client.enableLobby();
                    return null;
                });
    }

    private void displayError(ErrorServerMessage message) {
        JOptionPane.showMessageDialog(frame, message.getErrorMessage(), "Game Error", JOptionPane.ERROR_MESSAGE);
        if (lastMessage != null) {
            update(lastMessage);
        } else {
            frame.setVisible(false);
            client.enableLobby();
        }
    }

    public void update(GameStateServerMessage message) {
        updateBoardStones(message);
        updateBoardTerritory(message);
        updateGameState(message);
        lastMessage = message;
        // Debug: random play
        // if (message.getLastMoveType() != MoveType.PASS) playRandomMove();
    }

    private void updateBoardStones(GameStateServerMessage message) {
        int[][] stones = message.getStones();
        StoneColor[][] boardStones = new StoneColor[Board.BOARD_SIZE][Board.BOARD_SIZE];
        for (int y = 0; y < Board.BOARD_SIZE; y++) {
            for (int x = 0; x < Board.BOARD_SIZE; x++) {
                boardStones[y][x] = StoneColor.values()[stones[y][x]];
            }
        }
        if (message.getLastMoveType() == MoveType.STONE) {
            board.setLastMove(new Point(message.getLastMoveX(), message.getLastMoveY()));
        } else {
            board.setLastMove(null);
        }
        board.setStones(boardStones);
    }

    private void updateBoardTerritory(GameStateServerMessage message) {
        int[][] territory = message.getTerritory();
        StoneColor[][] boardTerritory = null;
        if (territory != null) {
            boardTerritory = new StoneColor[Board.BOARD_SIZE][Board.BOARD_SIZE];
            for (int y = 0; y < Board.BOARD_SIZE; y++) {
                for (int x = 0; x < Board.BOARD_SIZE; x++) {
                    boardTerritory[y][x] = StoneColor.values()[territory[y][x]];
                }
            }
        }
        board.setTerritory(boardTerritory);
    }

    private void updateGameState(GameStateServerMessage message) {
        state = message.getGameState();
        switch (state) {
            case WHITE_TO_MOVE, BLACK_TO_MOVE, WHITE_RESIGNED, BLACK_RESIGNED ->
                    scoreLabel.setText(STR."(W) \{message.getWhiteCaptures()} captures | (B) \{message.getBlackCaptures()} captures");
            case COUNTING, FINISHED -> scoreLabel.setText(STR."""
                    (W)\s
                    \{message.getWhiteCaptures()} captures,\s
                    \{message.getWhiteTerritory()} territory,\s
                    \{message.getKomi()} komi\s
                    | (B)\s
                    \{message.getBlackCaptures()} captures,\s
                    \{message.getBlackTerritory()} territory
                    """);
        }
        switch (state) {
            case WHITE_TO_MOVE, BLACK_TO_MOVE -> {
                setCanPlayMove(Objects.equals(state == GameState.WHITE_TO_MOVE ?
                        whitePlayer : blackPlayer, client.getName()));
                setCanResignOrDone(true);
                board.setMoveColor(state == GameState.WHITE_TO_MOVE ? StoneColor.WHITE : StoneColor.BLACK);
                passResumeButton.setText("Pass");
                resignDoneButton.setText("Resign");
                moveLabel.setText(STR."""
                    \{state == GameState.WHITE_TO_MOVE ? "White" : "Black"} to move
                    \{message.getLastMoveType() == MoveType.PASS ?
                        STR." (\{state == GameState.WHITE_TO_MOVE ? "Black" : "White"} have passed)" :
                        ""}
                    """);
            }
            case COUNTING -> {
                setCanMarkDeadStones(true);
                setCanResignOrDone(true);
                passResumeButton.setText("Resume");
                resignDoneButton.setText("Done");
                moveLabel.setText("Mark captured stones");
            }
            case FINISHED, WHITE_RESIGNED, BLACK_RESIGNED -> {
                setCanPlayMove(false);
                setCanResignOrDone(true);
                passResumeButton.setText("Resume");
                resignDoneButton.setText("Close");
                String info;
                if (state == GameState.FINISHED) {
                    BigDecimal score = BigDecimal.valueOf(message.getWhiteCaptures() + message.getWhiteTerritory() -
                            message.getBlackCaptures() - message.getBlackTerritory()).add(message.getKomi());
                    info = switch (score.compareTo(BigDecimal.ZERO)) {
                        case 1 -> STR."White (\{whitePlayer}) have won by \{score} points";
                        case 0 -> "Draw";
                        case -1 -> STR."Black (\{blackPlayer}) have won by \{score.negate()} points";
                        default ->
                                throw new IllegalStateException(STR."Unexpected value: \{score.compareTo(BigDecimal.ZERO)}");
                    };
                } else {
                    info = switch (state) {
                        case WHITE_RESIGNED -> STR."Black (\{blackPlayer}) has won by resignation";
                        case BLACK_RESIGNED -> STR."White (\{whitePlayer}) has won by resignation";
                        default -> throw new IllegalStateException(STR."Unexpected value: \{state}");
                    };
                }
                moveLabel.setText(info);
                JOptionPane.showMessageDialog(frame, info, "Game results", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void playRandomMove() {
        if (!board.getCanPlayMove()) return;
        ArrayList<Point> points = new ArrayList<>();
        for (int i = 0; i < Board.BOARD_SIZE; i++) {
            for (int j = 0; j < Board.BOARD_SIZE; j++) {
                if (board.getStones()[i][j].canPlay(board.getMoveColor())) points.add(new Point(j, i));
            }
        }
        if (!points.isEmpty()) {
            int i = (int) (Math.random() * points.toArray().length);
            CompletableFuture.runAsync(() -> {
                playMove(points.get(i));
            });
        }
    }

    public void dispose() {
        frame.dispose();
    }
}
