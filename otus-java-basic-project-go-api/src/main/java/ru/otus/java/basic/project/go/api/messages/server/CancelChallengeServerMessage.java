package ru.otus.java.basic.project.go.api.messages.server;

import ru.otus.java.basic.project.go.api.messages.ClientServerMessage;

public class CancelChallengeServerMessage extends ClientServerMessage {
    public CancelChallengeServerMessage() {
        super();
    }

    public CancelChallengeServerMessage(Long context) {
        super(context);
    }

}
