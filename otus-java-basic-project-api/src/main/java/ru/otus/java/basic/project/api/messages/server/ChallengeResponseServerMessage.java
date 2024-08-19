package ru.otus.java.basic.project.api.messages.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.otus.java.basic.project.api.enums.ChallengeResponse;
import ru.otus.java.basic.project.api.messages.ClientServerMessage;

import java.util.Objects;

public class ChallengeResponseServerMessage extends ClientServerMessage {
    @JsonProperty("r")
    private ChallengeResponse response;

    public ChallengeResponseServerMessage() {
        super();
    }

    public ChallengeResponseServerMessage(Long context, ChallengeResponse response) {
        super(context);
        if (response == null) throw new IllegalArgumentException("response is null");
        this.response = response;
    }

    public void setResponse(ChallengeResponse response) {
        if (response == null) throw new IllegalArgumentException("response is null");
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