package ru.otus.java.basic.project.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.project.api.context.ChallengeContext;
import ru.otus.java.basic.project.api.enums.ChallengeResponse;
import ru.otus.java.basic.project.api.exceptions.MessageProcessingException;
import ru.otus.java.basic.project.api.messages.server.GameStateServerMessage;
import ru.otus.java.basic.project.server.exceptions.AuthenticationException;
import ru.otus.java.basic.project.server.exceptions.ClientBusyException;
import ru.otus.java.basic.project.server.exceptions.ClientNameAlreadyTaken;
import ru.otus.java.basic.project.server.exceptions.ClientNotFoundException;
import ru.otus.java.basic.project.server.game.Game;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Acts as a central controller of the server application.
 * Listens to incoming connections, spawning a <code>ClientConnection</code> for each.
 * Manages games, and acts as a mediator between different clients.
 */
public class Server implements AutoCloseable {
    private static final Logger log = LogManager.getLogger(Server.class);
    private final AuthenticationProvider authenticationProvider;
    private final int port;
    private final ServerSocket serverSocket;
    private final Map<String, ClientConnection> clients = new HashMap<>();
    private boolean isClosing = false;

    public Server(int port) throws IOException, AuthenticationException {
        this.port = port;
        this.serverSocket = new ServerSocket(port);
        this.authenticationProvider = new JDBCAuthenticationProvider();
    }

    public void start() {
        log.info("Server started on port {}", port);
        while (!serverSocket.isClosed()) {
            acceptConnection(serverSocket);
        }
    }

    private void acceptConnection(ServerSocket serverSocket) {
        try {
            log.trace("Awaiting connection");
            new ClientConnection(this, serverSocket.accept());
        } catch (IOException e) {
            if (!isClosing) log.error("Error while accepting socket connection", e);
        }
    }

    @Override
    public void close() {
        log.info("Server is closing");
        isClosing = true;
        for (ClientConnection client : clients.values()) {
            client.close();
        }
        log.trace("Client connections closed");
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            log.error("Error while closing socket", e);
        }
    }

    public synchronized void addClient(ClientConnection clientConnection) throws ClientNameAlreadyTaken {
        synchronized (clients) {
            if (clients.containsKey(clientConnection.getName())) {
                throw new ClientNameAlreadyTaken();
            }
            clients.put(clientConnection.getName(), clientConnection);
            for (ClientConnection client : clients.values()) {
                if (!client.getName().equals(clientConnection.getName())) {
                    client.sendClientListUpdate();
                }
            }
        }
    }

    public void removeClient(ClientConnection clientConnection) {
        if (isClosing) return;
        synchronized (clients) {
            if (clientConnection.getName() == null) return;
            clients.remove(clientConnection.getName());
            for (ClientConnection client : clients.values()) {
                client.sendClientListUpdate();
            }
        }
    }

    public Collection<String> getClientNames() {
        return clients.keySet();
    }

    public void sendChallenge(ChallengeContext context) throws ClientNotFoundException, IOException {
        synchronized (clients) {
            log.trace("Sending challenge from {} to {}", context.getChallenger(), context.getChallenged());
            ClientConnection clientConnection = clients.get(context.getChallenged());
            if (clientConnection == null) throw new ClientNotFoundException();
            clientConnection.sendChallenge(context);
        }
    }

    public void sendChallengeResponse(ChallengeContext context, ChallengeResponse response) throws ClientNotFoundException, IOException, MessageProcessingException {
        synchronized (clients) {
            ClientConnection clientConnection = clients.get(context.getChallenger());
            if (clientConnection == null) throw new ClientNotFoundException();
            clientConnection.sendChallengeResponse(context, response);
            if (response == ChallengeResponse.ACCEPTED) {
                try {
                    createGame(context.getChallenger(), context.getChallenged());
                } catch (ClientBusyException e) {
                    throw new MessageProcessingException(e);
                }
            }
        }
    }

    public void sendChallengeCancel(ChallengeContext context) throws ClientNotFoundException, IOException, MessageProcessingException {
        synchronized (clients) {
            ClientConnection clientConnection = clients.get(context.getChallenged());
            if (clientConnection == null) throw new ClientNotFoundException();
            clientConnection.sendChallengeCancel(context);
        }
    }

    public void createGame(String challenger, String challenged) throws ClientNotFoundException, ClientBusyException {
        synchronized (clients) {
            ClientConnection challengerConnection = clients.get(challenger);
            ClientConnection challengedConnection = clients.get(challenged);
            if (challengerConnection == null || challengedConnection == null) throw new ClientNotFoundException();
            Game game = new Game(challenger, challenged);
            try {
                challengerConnection.setGame(game);
                challengedConnection.setGame(game);
            } catch (ClientBusyException e) {
                challengerConnection.removeGame(game);
                challengedConnection.removeGame(game);
                throw e;
            }
            game.setUpdateListener(this::updateGame);
        }
    }

    public void updateGame(Game game) throws ClientNotFoundException {
        synchronized (clients) {
            ClientConnection whiteConnection = clients.get(game.getWhitePlayer());
            ClientConnection blackConnection = clients.get(game.getBlackPlayer());
            if (whiteConnection == null || blackConnection == null) throw new ClientNotFoundException();
            GameStateServerMessage message = game.toGameStateMessage();
            try {
                whiteConnection.send(message);
                blackConnection.send(message);
            } catch (IOException e) {
                log.error("Socket error while sending game state update", e);
            }
            if (game.isFinished()) {
                whiteConnection.removeGame();
                blackConnection.removeGame();
            }
        }
    }

    public void authenticate(String name, String password, boolean register) throws AuthenticationException {
        if (register) {
            authenticationProvider.register(name, password);
        } else {
            authenticationProvider.authenticate(name, password);
        }
    }
}
