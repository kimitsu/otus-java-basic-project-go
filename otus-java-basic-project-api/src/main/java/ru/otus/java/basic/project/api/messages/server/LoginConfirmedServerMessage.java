package ru.otus.java.basic.project.api.messages.server;

import ru.otus.java.basic.project.api.messages.ClientServerMessage;

public class LoginConfirmedServerMessage extends ClientServerMessage {
    public LoginConfirmedServerMessage() {
    }

    public LoginConfirmedServerMessage(Long context) {
        super(context);
    }
}
