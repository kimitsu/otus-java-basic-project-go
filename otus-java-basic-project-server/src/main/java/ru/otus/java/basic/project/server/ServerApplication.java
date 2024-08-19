package ru.otus.java.basic.project.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.project.server.exceptions.AuthenticationException;

import java.io.IOException;

/**
 * Software is never finished, only abandoned.
 */
public class ServerApplication {
    private static final Logger log = LogManager.getLogger(ServerApplication.class);
    private static final int DEFAULT_PORT = 35555;
    private static final Thread mainThread = Thread.currentThread();
    private static Server server;

    public static void main(String[] args) {
        try {
            int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
            server = new Server(port);
            Runtime.getRuntime().addShutdownHook(new Thread(ServerApplication::shutdown));
            server.start();
        } catch (NumberFormatException e) {
            log.fatal("Invalid port number");
        } catch (IOException e) {
            log.fatal("Server socket error", e);
        } catch (AuthenticationException e) {
            log.fatal("Authentication provider error", e);
        }
        log.info("Server stopped");
        synchronized (mainThread) {
            mainThread.notifyAll();
        }
    }

    private static void shutdown() {
        synchronized (mainThread) {
            log.info("Shutdown signal received");
            server.close();
            try {
                mainThread.wait();
            } catch (InterruptedException e) {
                log.error("Thread interrupted", e);
            }
            log.trace("Shutdown complete");
            LogManager.shutdown(false, true);
        }
    }
}
