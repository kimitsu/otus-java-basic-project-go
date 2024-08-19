package ru.otus.java.basic.project.api.messages.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.otus.java.basic.project.api.messages.ClientServerMessage;

import java.util.Objects;

public class OutgoingChallengeClientMessage extends ClientServerMessage {
    @JsonProperty("c")
    private String client;

    public OutgoingChallengeClientMessage() {
        super();
    }

    public OutgoingChallengeClientMessage(Long context, String client) {
        super(context);
        if (client == null) throw new IllegalArgumentException("challenger is null");
        this.client = client;
    }

    public void setClient(String client) {
        if (client == null) throw new IllegalArgumentException("challenger is null");
        this.client = client;
    }

    public String getClient() {
        return client;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        OutgoingChallengeClientMessage message = (OutgoingChallengeClientMessage) obj;
        if (!Objects.equals(this.client, message.client)) return false;
        return true;
    }
}
