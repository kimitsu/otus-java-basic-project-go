package ru.otus.java.basic.project.go.api.messages.server;

import ru.otus.java.basic.project.go.api.messages.ClientServerMessage;

public class LoginConfirmedServerMessage extends ClientServerMessage {
    public LoginConfirmedServerMessage() {
    }

    public LoginConfirmedServerMessage(Long context) {
        super(context);
    }
}
