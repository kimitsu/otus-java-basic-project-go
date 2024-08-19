package ru.otus.java.basic.project.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.project.api.MessageProcessor;
import ru.otus.java.basic.project.api.context.ChallengeContext;
import ru.otus.java.basic.project.api.context.Context;
import ru.otus.java.basic.project.api.context.GameContext;
import ru.otus.java.basic.project.api.enums.ChallengeResponse;
import ru.otus.java.basic.project.api.exceptions.InvalidContextException;
import ru.otus.java.basic.project.api.exceptions.MessageProcessingException;
import ru.otus.java.basic.project.api.messages.ClientServerMessage;
import ru.otus.java.basic.project.api.messages.client.*;
import ru.otus.java.basic.project.api.messages.server.*;
import ru.otus.java.basic.project.server.exceptions.*;
import ru.otus.java.basic.project.server.game.Game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a connected client.
 * Holds information about the client, its contexts and current game.
 * Dispatches client messages to their appropriate processors.
 * Sends server messages to the clients.
 */
public class ClientConnection implements AutoCloseable {
    private static final Logger log = LogManager.getLogger(ClientConnection.class);
    private static int idCounter = 0;
    private final int id;
    private final Thread listenerThread;
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private String name;
    private final Server server;
    private final Map<Long, Context> contexts = new HashMap<>();
    private Game game;
    private boolean isClosing = false;

    public ClientConnection(Server server, Socket socket) throws IOException {
        this.server = server;
        this.id = idCounter++;
        log.trace("Client {}, initializing connection from {}", id, socket.getInetAddress().getHostAddress());
        this.socket = socket;
        this.input = new DataInputStream(socket.getInputStream());
        this.output = new DataOutputStream(socket.getOutputStream());
        initDefaultContext();
        this.listenerThread = new Thread(this::listen);
        this.listenerThread.start();
    }

    private void listen() {
        log.trace("Listener thread started (client {})", id);
        try {
            while (!socket.isClosed() && socket.isConnected()) {
                processMessage();
            }
        } catch (ClientDisconnectedException e) {
            log.info("Client {} had disconnected", id);
        } finally {
            close();
            synchronized (listenerThread) {
                listenerThread.notifyAll();
            }
        }
    }

    private void processMessage() throws ClientDisconnectedException {
        try {
            try {
                String json = input.readUTF();
                log.debug("Received from client {}: {}", id, json);
                ClientServerMessage message = ClientServerMessage.deserialize(json);
                dispatchMessage(message);
            } catch (MessageProcessingException e) {
                log.error("Invalid message received from client {}", id, e);
                send(new ErrorServerMessage(null, "Invalid Message Error"));
            } catch (IOException e) {
                if (!isClosing) {
                    log.error("IO Error while reading from socket (client {})", id, e);
                    send(new ErrorServerMessage(null, "Server IO Error"));
                }
                throw new ClientDisconnectedException();
            }
        } catch (IOException e) {
            log.error("IO Error while writing to socket (client {})", id, e);
            throw new ClientDisconnectedException();
        }
    }

    private void dispatchMessage(ClientServerMessage message) throws IOException {
        try {
            if (name == null && !message.getClass().equals(LoginClientMessage.class)) {
                log.warn("Client {} is unauthorized to send {}", id, message.getClass().getName());
                send(new ErrorServerMessage(message.getContextId(), "Not logged in"));
                return;
            }
            Context context = contexts.get(message.getContextId());
            if (context == null) {
                log.trace("Context is not found, looking for listener in the default context");
                context = contexts.get(null);
            }
            MessageProcessor<ClientServerMessage> listener = context.getListener(message.getClass());
            if (listener == null) {
                throw (new MessageDispatchException("No listener for the message class " + message.getClass().getName()));
            }
            listener.process(message);
        } catch (MessageDispatchException e) {
            log.error("Message dispatch error", e);
            send(new ErrorServerMessage(message.getContextId(), "Invalid message"));
        } catch (MessageProcessingException e) {
            log.error("Message processing error", e);
            send(new ErrorServerMessage(message.getContextId(), "Invalid message"));
        }
    }

    private void initDefaultContext() {
        Context context = new Context(null);
        addContext(context);
        context.setListener(LoginClientMessage.class, this::process);
        context.setListener(GetListClientMessage.class, this::process);
        context.setListener(OutgoingChallengeClientMessage.class, this::process);
    }

    public void process(LoginClientMessage clientMessage) throws MessageProcessingException, IOException {
        if (hasContext(clientMessage.getContextId())) {
            throw new MessageProcessingException(new InvalidContextException("Context already exists"));
        }
        Context context = new Context(clientMessage.getContextId());
        try {
            server.authenticate(clientMessage.getName(), clientMessage.getPassword(), clientMessage.isRegister());
        } catch (AuthenticationException e) {
            send(new ErrorServerMessage(context.getId(), e.getMessage()));
            close();
            return;
        }
        name = clientMessage.getName();
        try {
            server.addClient(this);
        } catch (ClientNameAlreadyTaken e) {
            send(new ErrorServerMessage(context.getId(), "Client has already logged in"));
            this.name = null;
            close();
            return;
        }
        send(new LoginConfirmedServerMessage(context.getId()));
        getDefaultContext().removeListener(LoginClientMessage.class);
        log.info("Client {} has logged in as {} from {}", id, name, socket.getInetAddress().getHostAddress());
    }


    public void process(GetListClientMessage clientMessage) throws MessageProcessingException, IOException {
        Context context = getContext(clientMessage.getContextId());
        if (context == null) {
            context = new Context(clientMessage.getContextId());
            addContext(context);
        }
        Collection<String> clients = server.getClientNames();
        ClientServerMessage response = new ClientListServerMessage(context.getId(), clients);
        send(response);
    }

    public void process(OutgoingChallengeClientMessage clientMessage) throws MessageProcessingException, IOException {
        if (hasContext(clientMessage.getContextId())) {
            throw new MessageProcessingException(new InvalidContextException("Context already exists"));
        }
        ChallengeContext context = new ChallengeContext(clientMessage.getContextId());
        context.setChallenger(name);
        context.setChallenged(clientMessage.getClient());
        context.setListener(CancelChallengeClientMessage.class, this::process);
        try {
            server.sendChallenge(context);
            addContext(context);
        } catch (ClientNotFoundException e) {
            send(new ErrorServerMessage(context.getId(), "Client not found"));
        }
    }

    public void process(ChallengeResponseClientMessage clientMessage) throws MessageProcessingException, IOException {
        ChallengeContext context = getContext(clientMessage.getContextId(), ChallengeContext.class);
        if (context == null) {
            log.error("Client {} challenge response context not found", id);
            send(new ErrorServerMessage(clientMessage.getContextId(), "Context not found"));
            return;
        }
        try {
            server.sendChallengeResponse(context, clientMessage.getResponse());
        } catch (ClientNotFoundException e) {
            send(new ErrorServerMessage(context.getId(), "Client not found"));
        } finally {
            removeContext(context);
        }
    }

    public void process(CancelChallengeClientMessage clientMessage) throws MessageProcessingException, IOException {
        ChallengeContext context = getContext(clientMessage.getContextId(), ChallengeContext.class);
        if (context == null) {
            log.error("Client {} cancel challenge context not found", id);
            send(new ErrorServerMessage(clientMessage.getContextId(), "Context not found"));
            return;
        }
        try {
            server.sendChallengeCancel(context);
        } catch (ClientNotFoundException e) {
            send(new ErrorServerMessage(context.getId(), "Client not found"));
        } finally {
            removeContext(context);
        }
    }

    private void process(RequestGameInfoClientMessage clientMessage) throws IOException, MessageProcessingException {
        if (game == null) {
            send(new ErrorServerMessage(clientMessage.getContextId(), "Game is not found"));
            return;
        }
        send(game.toGameInfoMessage(clientMessage.getContextId()));
        try {
            server.updateGame(game);
        } catch (ClientNotFoundException e) {
            throw new MessageProcessingException(e);
        }
    }

    private void process(GameMoveClientMessage clientMessage) throws MessageProcessingException {
        try {
            game.processMessage(this, clientMessage);
        } catch (ClientNotFoundException e) {
            throw new MessageProcessingException(e);
        }
    }

    public void sendChallenge(ChallengeContext context) throws IOException {
        ChallengeContext challengeContext = new ChallengeContext(context.getId());
        challengeContext.setChallenger(context.getChallenger());
        challengeContext.setChallenged(context.getChallenged());
        challengeContext.setListener(ChallengeResponseClientMessage.class, this::process);
        send(new IncomingChallengeServerMessage(context.getId(), context.getChallenger()));
        addContext(challengeContext);
    }

    public void sendChallengeResponse(ChallengeContext context, ChallengeResponse response) throws IOException, MessageProcessingException {
        ChallengeContext challengeContext = getContext(context.getId(), ChallengeContext.class);
        if (challengeContext == null)
            throw new MessageProcessingException(new InvalidContextException("Context not found"));
        try {
            send(new ChallengeResponseServerMessage(context.getId(), response));
        } finally {
            removeContext(challengeContext);
        }
    }

    public void sendChallengeCancel(ChallengeContext context) throws MessageProcessingException, IOException {
        ChallengeContext challengeContext = getContext(context.getId(), ChallengeContext.class);
        if (challengeContext == null)
            throw new MessageProcessingException(new InvalidContextException("Context not found"));
        try {
            send(new CancelChallengeServerMessage(context.getId()));
        } finally {
            removeContext(challengeContext);
        }
    }

    public void sendClientListUpdate() {
        Collection<String> clients = server.getClientNames();
        try {
            send(new ClientListServerMessage(null, clients));
        } catch (IOException e) {
            log.error("Could not send clients list update to client {}", id);
        }
    }

    public void send(ClientServerMessage message) throws IOException {
        if (socket == null || socket.isClosed() || !socket.isConnected() || output == null) {
            throw new IOException("Connection not ready");
        }
        String json = new ObjectMapper().writeValueAsString(message);
        log.debug("Sending to client {}: {}", id, json);
        output.writeUTF(json);
        log.trace("Sending complete to client {}: {}", id, json);
    }

    @Override
    public void close() {
        synchronized (listenerThread) {
            isClosing = true;
            log.info("Closing client connection {}", id);
            try {
                if (output != null) output.close();
            } catch (IOException e) {
                log.error("Error while closing data stream", e);
            }
            try {
                if (input != null) input.close();
            } catch (IOException e) {
                log.error("Error while closing data stream", e);
            }
            try {
                if (socket != null) socket.close();
            } catch (IOException e) {
                log.error("Error while closing socket", e);
            }
            server.removeClient(this);
            try {
                if (!listenerThread.equals(Thread.currentThread())) {
                    listenerThread.wait();
                }
            } catch (InterruptedException e) {
                log.error("Thread interrupted", e);
            }
        }
    }

    private <T extends Context> T getContext(Long id, Class<T> contextClass) throws MessageProcessingException {
        try {
            return contextClass.cast(contexts.get(id));
        } catch (ClassCastException e) {
            log.error("Context id {} could not be cast to {}", id, contextClass.getName());
            throw new MessageProcessingException(e);
        }
    }

    public void addContext(Context context) {
        log.trace("Context id {} added ({})", context.getId(), context.getClass());
        contexts.put(context.getId(), context);
    }

    public void removeContext(Context context) {
        log.trace("Context id {} removed ({})", context.getId(), context.getClass());
        contexts.remove(context.getId());
    }

    public void removeContext(Long contextId) {
        Context context = contexts.get(contextId);
        if (context != null) removeContext(context);
    }

    public Context getContext(Long id) {
        return contexts.get(id);
    }

    public Context getDefaultContext() {
        return contexts.get(null);
    }

    public boolean hasContext(Long id) {
        return contexts.containsKey(id);
    }

    public void setGame(Game game) throws ClientBusyException {
        if (this.game != null) throw new ClientBusyException();
        this.game = game;
        GameContext context = this.game.toGameContext();
        context.setListener(GameMoveClientMessage.class, this::process);
        getDefaultContext().setListener(RequestGameInfoClientMessage.class, this::process);
        addContext(context);
    }

    public void removeGame(Game game) {
        if (this.game == game) removeGame();
    }

    public void removeGame() {
        removeContext(game.getContextId());
        game = null;
    }

    public Server getServer() {
        return this.server;
    }

    public String getName() {
        return name;
    }
}
