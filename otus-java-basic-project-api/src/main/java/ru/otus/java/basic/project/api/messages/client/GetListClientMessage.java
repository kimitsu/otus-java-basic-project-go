package ru.otus.java.basic.project.api.messages.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.otus.java.basic.project.api.messages.ClientServerMessage;

public class GetListClientMessage extends ClientServerMessage {
    public GetListClientMessage() {
        super();
    }

    public GetListClientMessage(Long context) {
        super(context);
    }

}
