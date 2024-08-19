package ru.otus.java.basic.project.go.api.messages.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.otus.java.basic.project.go.api.enums.ChallengeResponse;
import ru.otus.java.basic.project.go.api.messages.ClientServerMessage;

import java.util.Objects;

public class ChallengeResponseServerMessage extends ClientServerMessage {
    @JsonProperty("r")
    private ChallengeResponse response;

    public ChallengeResponseServerMessage() {
        super();
    }

    public ChallengeResponseServerMessage(Long context, ChallengeResponse response) {
        super(context);
        this.response = response;
    }

    public void setResponse(ChallengeResponse response) {
        this.response = response;
    }

    public ChallengeResponse getResponse() {
        return response;
    }
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        ChallengeResponseServerMessage message = (ChallengeResponseServerMessage) obj;
        if (!Objects.equals(this.response, message.response)) return false;
        return true;
    }
}