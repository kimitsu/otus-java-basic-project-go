package ru.otus.java.basic.project.api.messages.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.otus.java.basic.project.api.enums.ChallengeResponse;
import ru.otus.java.basic.project.api.messages.ClientServerMessage;

import java.util.Objects;

public class ChallengeResponseClientMessage extends ClientServerMessage {

    @JsonProperty("a")
    private ChallengeResponse response;

    public ChallengeResponseClientMessage() {
        super();
    }

    public ChallengeResponseClientMessage(Long context, ChallengeResponse response) {
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
        ChallengeResponseClientMessage message = (ChallengeResponseClientMessage) obj;
        if (!Objects.equals(this.response, message.response)) return false;
        return true;
    }
}
