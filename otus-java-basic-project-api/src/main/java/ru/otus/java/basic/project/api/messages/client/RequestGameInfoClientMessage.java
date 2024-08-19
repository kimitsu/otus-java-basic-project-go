package ru.otus.java.basic.project.api.messages.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.otus.java.basic.project.api.messages.ClientServerMessage;

public class RequestGameInfoClientMessage extends ClientServerMessage {

    public RequestGameInfoClientMessage() {
        super();
    }

    public RequestGameInfoClientMessage(Long context) {
        super(context);
    }

}
