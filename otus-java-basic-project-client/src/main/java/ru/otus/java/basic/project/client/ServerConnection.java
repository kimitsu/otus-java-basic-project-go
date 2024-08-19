package ru.otus.java.basic.project.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.project.api.MessageProcessor;
import ru.otus.java.basic.project.api.context.Context;
import ru.otus.java.basic.project.api.exceptions.MessageProcessingException;
import ru.otus.java.basic.project.api.messages.ClientServerMessage;
import ru.otus.java.basic.project.api.messages.server.ErrorServerMessage;
import ru.otus.java.basic.project.client.exceptions.ApplicationException;
import ru.otus.java.basic.project.client.exceptions.InvalidServerMessageException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Connects to the server, sends and receives messages.
 * Received messages are dispatched based on their classes and context ids to appropriate listeners.
 */
public class ServerConnection implements AutoCloseable {
    private static final Logger log = LogManager.getLogger(ServerConnection.class);
    private static final int DEFAULT_PORT = 35555;
    private String host;
    private int port;
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;

    private final Map<Long, Context> contexts = new HashMap<>();
    private Runnable disconnectListener = null;

    public ServerConnection(String hostPort, String name, String password, boolean register)
            throws IOException, InvalidServerMessageException, IllegalArgumentException, ApplicationException {
        int delimiter = hostPort.indexOf(':');
        if (delimiter == -1) {
            this.host = hostPort;
            this.port = DEFAULT_PORT;
        } else {
            this.host = hostPort.substring(0, delimiter);
            try {
                this.port = Integer.parseInt(hostPort.substring(delimiter + 1));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Incorrect port number");
            }
        }
        this.socket = new Socket(host, port);
        this.output = new DataOutputStream(this.socket.getOutputStream());
        this.input = new DataInputStream(this.socket.getInputStream());
        // Default context which will handle messages with null new context ids
        this.contexts.put(null, new Context(null));
        new Thread(this::listen).start();
    }

    private void dispatchMessage(ClientServerMessage message) throws IOException {
        try {
            Context context = contexts.get(message.getContextId());
            if (context == null) {
                log.trace("Context is not found, looking for listener in the default context");
                context = contexts.get(null);
            }
            MessageProcessor<ClientServerMessage> listener = context.getListener(message.getClass());
            if (listener == null) {
                throw (new ApplicationException("No listener for the message class " + message.getClass().getName()));
            }
            listener.process(message);
        } catch (ApplicationException | MessageProcessingException e) {
            log.error("Message dispatch error", e);
        }
    }

    private void listen() {
        while (!socket.isClosed()) {
            try {
                String json = input.readUTF();
                log.debug("Received message: {}", json);
                try {
                    ClientServerMessage message = ClientServerMessage.deserialize(json);
                    dispatchMessage(message);
                } catch (MessageProcessingException e) {
                    log.error("Invalid server message", e);
                }
            } catch (EOFException e) {
                log.warn("Connection reset");
                break;
            } catch (IOException e) {
                log.error("IO Error while reading server message", e);
                break;
            }
        }
        if (disconnectListener != null) disconnectListener.run();
    }

    public void send(ClientServerMessage message) throws MessageProcessingException, IOException {
        String json = message.serialize();
        log.debug("Sending message: {}", json);
        output.writeUTF(json);
    }

    @Override
    public void close() {
        try {
            if (output != null) output.close();
        } catch (IOException e) {
            log.error("Error while closing stream", e);
        }
        try {
            if (input != null) input.close();
        } catch (IOException e) {
            log.error("Error while closing stream", e);
        }
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            log.error("Error while closing socket", e);
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

    public Context getContext(Long id) {
        return contexts.get(id);
    }

    public Context getDefaultContext() {
        return getContext(null);
    }

    public void setDisconnectListener(Runnable disconnectListener) {
        this.disconnectListener = disconnectListener;
    }
}

