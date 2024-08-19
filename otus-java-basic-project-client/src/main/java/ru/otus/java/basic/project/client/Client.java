package ru.otus.java.basic.project.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.project.api.context.ChallengeContext;
import ru.otus.java.basic.project.api.context.Context;
import ru.otus.java.basic.project.api.context.GameContext;
import ru.otus.java.basic.project.api.enums.ChallengeResponse;
import ru.otus.java.basic.project.api.enums.MoveType;
import ru.otus.java.basic.project.api.exceptions.MessageProcessingException;
import ru.otus.java.basic.project.api.messages.ClientServerMessage;
import ru.otus.java.basic.project.api.messages.client.*;
import ru.otus.java.basic.project.api.messages.server.*;
import ru.otus.java.basic.project.client.exceptions.ApplicationException;
import ru.otus.java.basic.project.client.exceptions.ChallengeCancelledException;
import ru.otus.java.basic.project.client.exceptions.InvalidServerMessageException;
import ru.otus.java.basic.project.client.exceptions.ServerError;
import ru.otus.java.basic.project.client.windows.*;

import javax.swing.*;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Serves as a central controller of the application, as well as mediator between the UI and IO.
 * Manages <code>ServerConnection</code> to communicate with the server, sets up appropriate message listeners.
 * Initializes, shows and eventually disposes of the UI windows.
 * Provides methods to send messages to the server which return <code>CompletableFuture</code>
 * that complete after receiving appropriate responses or errors.
 */
public class Client implements AutoCloseable {
    private static final Logger log = LogManager.getLogger(Client.class);

    private final LoginWindow loginWindow;
    private final LobbyWindow lobbyWindow;
    private final OutgoingChallengeWindow outgoingChallengeWindow;
    private final IncomingChallengeWindow incomingChallengeWindow;
    private final GameWindow gameWindow;
    private ServerConnection serverConnection;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private ChallengeContext challengeContext = null;
    private CompletableFuture<ChallengeResponse> challengeFuture = null;
    private String name;

    public Client() {
        this.loginWindow = new LoginWindow(this);
        this.lobbyWindow = new LobbyWindow(this);
        this.outgoingChallengeWindow = new OutgoingChallengeWindow(this, this.lobbyWindow.getFrame());
        this.incomingChallengeWindow = new IncomingChallengeWindow(this, this.lobbyWindow.getFrame());
        this.gameWindow = new GameWindow(this);
    }

    public void close() {
        serverConnection.setDisconnectListener(null);
        if (serverConnection != null) serverConnection.close();
        executorService.shutdownNow();
        executorService.close();
        loginWindow.dispose();
        lobbyWindow.dispose();
        outgoingChallengeWindow.dispose();
        incomingChallengeWindow.dispose();
        gameWindow.dispose();
    }

    public void showLoginWindow() {
        loginWindow.show();
    }

    public void showLobbyWindow() {
        lobbyWindow.show(name);
        serverConnection.getDefaultContext().setListener(ErrorServerMessage.class, (message) -> {
            CompletableFuture.runAsync(() -> lobbyWindow.displayServerError(message.getErrorMessage()));
        });
    }

    public void showOutgoingChallengeWindow(String challenged) {
        outgoingChallengeWindow.show(challenged);
    }

    private void showIncomingChallengeWindow(ChallengeContext context) {
        incomingChallengeWindow.show(context);
    }

    public void showGameWindow() {
        disableLobby();
        gameWindow.show(lobbyWindow.getFrame());
    }

    public CompletableFuture<Void> connectAndLoginAsync(String hostPort, String name, String password, boolean register) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                log.trace("Creating server connection");
                serverConnection = new ServerConnection(hostPort, name, password, register);
                log.trace("Server connection created");
            } catch (IllegalArgumentException e) {
                throw new ApplicationException("Incorrect input", e);
            } catch (IOException e) {
                throw new ApplicationException("Connection error", e);
            } catch (InvalidServerMessageException e) {
                throw new ApplicationException("Server response error", e);
            } catch (ApplicationException e) {
                throw new ApplicationException("Application error", e);
            }
        });
        return future.thenCompose((result) -> loginAsync(name, password, register));
    }

    public CompletableFuture<Void> loginAsync(String name, String password, boolean register) {
        log.trace("Setting up future login");
        CompletableFuture<Void> future = new CompletableFuture<>();
        Context context = new Context(Context.getNewId());
        serverConnection.addContext(context);
        context.setListener(LoginConfirmedServerMessage.class, (LoginConfirmedServerMessage message) -> {
            this.name = name;
            serverConnection.setDisconnectListener(() -> {
                JOptionPane.showMessageDialog(null, "Disconnected from server", "Error", JOptionPane.ERROR_MESSAGE);
                close();
            });
            future.complete(null);
        });
        context.setListener(ErrorServerMessage.class, (ErrorServerMessage message) -> {
            future.completeExceptionally(new ExecutionException("Login Error", new ServerError(message.getErrorMessage())));
        });
        submitFutureSend(future, new LoginClientMessage(context.getId(), name, password, register));
        return future.whenComplete((result, e) -> {
            log.trace("Login future complete ({}, {})", result, e);
            serverConnection.removeContext(context);
        });
    }

    public CompletableFuture<Collection<String>> updateClientsListAsync() {
        log.trace("Setting up future update clients list");
        CompletableFuture<Collection<String>> future = new CompletableFuture<>();
        Context context = new Context(Context.getNewId());
        context.setListener(ClientListServerMessage.class, (ClientListServerMessage message) -> {
            future.complete(message.getClients());
        });
        context.setListener(ErrorServerMessage.class, (ErrorServerMessage message) -> {
            future.completeExceptionally(new ExecutionException("Server Error", new ServerError(message.getErrorMessage())));
        });
        serverConnection.addContext(context);
        submitFutureSend(future, new GetListClientMessage(context.getId()));
        return future.whenComplete((result, e) -> {
            log.trace("Update clients future complete ({}, {})", result, e);
            serverConnection.removeContext(context);
        });
    }

    public void listenToClientListUpdated(boolean allow) {
        log.trace("Setting listening to client list updates to {}", allow);
        if (allow) {
            serverConnection.getDefaultContext().setListener(ClientListServerMessage.class, (message) -> {
                lobbyWindow.refreshClientsList(message.getClients());
            });
        } else {
            serverConnection.getDefaultContext().setListener(ClientListServerMessage.class, (message) -> {
                log.trace("Not currently listening to client list updates");
            });
        }
    }

    public void listenToChallenges(boolean allow) {
        log.trace("Setting listening to challenges to {}", allow);
        if (allow) {
            serverConnection.getDefaultContext().setListener(IncomingChallengeServerMessage.class, this::process);
        } else {
            serverConnection.getDefaultContext().setListener(IncomingChallengeServerMessage.class, (message) -> {
                try {
                    serverConnection.send(new ChallengeResponseClientMessage(message.getContextId(), ChallengeResponse.BUSY));
                } catch (IOException e) {
                    throw new MessageProcessingException(e);
                }
            });
        }
    }

    public void process(IncomingChallengeServerMessage serverMessage) {
        listenToChallenges(false);
        if (challengeContext != null) {
            log.error("Challenge context already exists");
        }
        challengeContext = new ChallengeContext(serverMessage.getContextId());
        challengeContext.setChallenger(serverMessage.getChallenger());
        challengeContext.setListener(CancelChallengeServerMessage.class, (message) -> {
            log.trace("Challenge is cancelled");
            serverConnection.removeContext(challengeContext);
            challengeContext = null;
            CompletableFuture.runAsync(incomingChallengeWindow::cancelChallenge)
                    .thenRun(() -> listenToChallenges(true));

        });
        serverConnection.addContext(challengeContext);
        CompletableFuture.runAsync(() -> showIncomingChallengeWindow(challengeContext));
    }

    public CompletableFuture<ChallengeResponse> challengeAsync(String client) {
        log.trace("Setting up future challenge to {}", client);
        if (challengeContext != null) throw new IllegalStateException("Challenge context is already present");
        listenToChallenges(false);
        challengeFuture = new CompletableFuture<>();
        challengeContext = new ChallengeContext(Context.getNewId());

        challengeContext.setChallenged(client);
        challengeContext.setListener(ErrorServerMessage.class, (ErrorServerMessage message) -> {
            challengeFuture.completeExceptionally(new ExecutionException("Challenge Error", new ServerError(message.getErrorMessage())));
        });
        challengeContext.setListener(ChallengeResponseServerMessage.class, (ChallengeResponseServerMessage message) -> {
            challengeFuture.complete(message.getResponse());
        });
        serverConnection.addContext(challengeContext);
        submitFutureSend(challengeFuture, new OutgoingChallengeClientMessage(challengeContext.getId(), client));
        return challengeFuture.whenComplete((result, e) -> {
            log.trace("Challenge future complete ({}, {})", result, e);
            serverConnection.removeContext(challengeContext);
            challengeContext = null;
            if (result != ChallengeResponse.ACCEPTED) {
                listenToChallenges(true);
            }
        });
    }

    public CompletableFuture<Void> cancelChallengeAsync() {
        log.trace("Setting up future challenge cancel");
        if (challengeContext == null) throw new IllegalStateException("Challenge context is not found");
        challengeContext.removeListener(ChallengeResponseServerMessage.class);
        CompletableFuture<Void> future = new CompletableFuture<>();
        submitFutureSend(future, new CancelChallengeClientMessage(challengeContext.getId()), true);
        return future.thenApply((result) -> {
                    challengeFuture.completeExceptionally(new ChallengeCancelledException());
                    return result;
                })
                .exceptionally((e) -> {
                    challengeFuture.completeExceptionally(e);
                    return null;
                });
    }

    public CompletableFuture<Void> respondChallengeAsync(ChallengeContext context, ChallengeResponse response) {
        log.trace("Setting up future challenge response");
        CompletableFuture<Void> future = new CompletableFuture<>();
        context.setListener(ErrorServerMessage.class, (ErrorServerMessage message) -> {
            future.completeExceptionally(new ExecutionException("Challenge Error", new ServerError(message.getErrorMessage())));
        });
        submitFutureSend(future, new ChallengeResponseClientMessage(context.getId(), response), true);
        return future.whenComplete((result, e) -> {
            serverConnection.removeContext(context);
            challengeContext = null;
            if (response != ChallengeResponse.ACCEPTED) {
                listenToChallenges(true);
            }
        });
    }

    public CompletableFuture<GameContext> requestGameInfoAsync() {
        log.trace("Setting up future request game info");
        CompletableFuture<GameContext> future = new CompletableFuture<>();
        Context context = new Context(Context.getNewId());
        context.setListener(GameInfoServerMessage.class, (message) -> {
            GameContext gameContext = new GameContext(message.getGameContext());
            gameContext.setWhitePlayer(message.getWhitePlayer());
            gameContext.setBlackPlayer(message.getBlackPlayer());
            gameContext.setListener(GameStateServerMessage.class, this::processGameState);
            serverConnection.addContext(gameContext);
            future.complete(gameContext);
        });
        context.setListener(ErrorServerMessage.class, (message) -> {
            future.completeExceptionally(new ExecutionException("Game Error", new ServerError(message.getErrorMessage())));
        });
        serverConnection.addContext(context);
        submitFutureSend(future, new RequestGameInfoClientMessage(context.getId()));
        return future.whenComplete((result, e) -> {
            serverConnection.removeContext(context);
        });
    }

    private void processGameState(GameStateServerMessage message) {
        CompletableFuture.runAsync(() -> gameWindow.update(message));
    }

    private void submitFutureSend(CompletableFuture<?> future, ClientServerMessage message, boolean shouldCompleteFuture) {
        log.trace("Submitting future send of {}", message.getClass());
        executorService.submit(() -> {
            try {
                serverConnection.send(message);
                if (shouldCompleteFuture) {
                    future.complete(null);
                }
            } catch (MessageProcessingException e) {
                future.completeExceptionally(new ExecutionException("Application Error", e));
            } catch (IOException e) {
                future.completeExceptionally(new ExecutionException("IO Error", e));
            }
        });
    }

    private void submitFutureSend(CompletableFuture<?> future, ClientServerMessage message) {
        submitFutureSend(future, message, false);
    }

    public ServerConnection getServerConnection() {
        return serverConnection;
    }

    public String getName() {
        return name;
    }

    public void playMoveAsync(GameContext gameContext, MoveType moveType, int x, int y) {
        log.trace("Setting up future play move");
        CompletableFuture<Void> future = new CompletableFuture<>();
        submitFutureSend(future, new GameMoveClientMessage(gameContext.getId(), moveType, x, y), true);
    }

    public void disableLobby() {
        listenToChallenges(false);
        lobbyWindow.getFrame().setEnabled(false);
    }

    public void enableLobby() {
        lobbyWindow.getFrame().setEnabled(true);
        listenToChallenges(true);
    }
}
