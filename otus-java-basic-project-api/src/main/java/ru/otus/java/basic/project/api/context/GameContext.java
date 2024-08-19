package ru.otus.java.basic.project.api.context;

public final class GameContext extends Context {
    private String whitePlayer = null;
    private String blackPlayer = null;

    public GameContext(Long id) {
        super(id);
    }
    public GameContext(Long id, String whitePlayer, String blackPlayer) {
        super(id);
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
    }

    public void setWhitePlayer(String whitePlayer) {
        this.whitePlayer = whitePlayer;
    }

    public void setBlackPlayer(String blackPlayer) {
        this.blackPlayer = blackPlayer;
    }

    public String getWhitePlayer() {
        return whitePlayer;
    }

    public String getBlackPlayer() {
        return blackPlayer;
    }
}
