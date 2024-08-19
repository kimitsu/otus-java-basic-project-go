package ru.otus.java.basic.project.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientApplication {
    private static final Logger log = LogManager.getLogger(ClientApplication.class);
    private static final Client client = new Client();
    public static void main(String[] args) {
        log.trace("Main thread started");
        javax.swing.SwingUtilities.invokeLater(client::showLoginWindow);
        log.trace("Main thread finished");
    }
}
