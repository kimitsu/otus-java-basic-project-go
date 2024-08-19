package ru.otus.java.basic.project.api.context;

/**
 * This context is created on server when a game challenge is accepted.
 * It is then sent to the clients and used to communicate move messages to the server
 * and game state messages back to the clients.
 */
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
