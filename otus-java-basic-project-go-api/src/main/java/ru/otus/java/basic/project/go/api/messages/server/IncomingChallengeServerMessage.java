package ru.otus.java.basic.project.go.api.messages.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.otus.java.basic.project.go.api.messages.ClientServerMessage;

import java.util.Objects;

public class IncomingChallengeServerMessage extends ClientServerMessage {
    @JsonProperty("c")
    private String challenger;


    public IncomingChallengeServerMessage() {
        super();
    }

    public IncomingChallengeServerMessage(Long context, String challenger) {
        super(context);
        this.challenger = challenger;
    }

    public String getChallenger() {
        return challenger;
    }

    public void setChallenger(String challenger) {
        this.challenger = challenger;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        IncomingChallengeServerMessage message = (IncomingChallengeServerMessage) obj;
        if (!Objects.equals(this.challenger, message.challenger)) return false;
        return true;
    }
}
