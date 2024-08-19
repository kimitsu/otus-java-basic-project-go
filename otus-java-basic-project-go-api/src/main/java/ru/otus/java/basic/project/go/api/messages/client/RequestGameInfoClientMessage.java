package ru.otus.java.basic.project.go.api.messages.client;

import ru.otus.java.basic.project.go.api.messages.ClientServerMessage;

public class RequestGameInfoClientMessage extends ClientServerMessage {

    public RequestGameInfoClientMessage() {
        super();
    }

    public RequestGameInfoClientMessage(Long context) {
        super(context);
    }

}
