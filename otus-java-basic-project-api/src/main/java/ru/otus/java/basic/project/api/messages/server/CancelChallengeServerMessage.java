package ru.otus.java.basic.project.api.messages.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.otus.java.basic.project.api.messages.ClientServerMessage;

public class CancelChallengeServerMessage extends ClientServerMessage {
    public CancelChallengeServerMessage() {
        super();
    }

    public CancelChallengeServerMessage(Long context) {
        super(context);
    }

}
