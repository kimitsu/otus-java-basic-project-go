package ru.otus.java.basic.project.go.api.messages.client;

import ru.otus.java.basic.project.go.api.messages.ClientServerMessage;

public class CancelChallengeClientMessage extends ClientServerMessage {
    public CancelChallengeClientMessage() {
        super();
    }
    public CancelChallengeClientMessage(Long context) {
        super(context);
    }
}
