package ru.otus.java.basic.project.go.api.messages.client;

import ru.otus.java.basic.project.go.api.messages.ClientServerMessage;

public class GetListClientMessage extends ClientServerMessage {
    public GetListClientMessage() {
        super();
    }

    public GetListClientMessage(Long context) {
        super(context);
    }

}
