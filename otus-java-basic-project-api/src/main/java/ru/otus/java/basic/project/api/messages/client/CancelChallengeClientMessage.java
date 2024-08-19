package ru.otus.java.basic.project.api.messages.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.otus.java.basic.project.api.messages.ClientServerMessage;

public class CancelChallengeClientMessage extends ClientServerMessage {
    public CancelChallengeClientMessage() {
        super();
    }
    public CancelChallengeClientMessage(Long context) {
        super(context);
    }
}
