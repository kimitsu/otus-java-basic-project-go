package ru.otus.java.basic.project.api.messages.client;

import ru.otus.java.basic.project.api.messages.ClientServerMessage;

public class CancelChallengeClientMessage extends ClientServerMessage {
    public CancelChallengeClientMessage() {
        super();
    }
    public CancelChallengeClientMessage(Long context) {
        super(context);
    }
}
