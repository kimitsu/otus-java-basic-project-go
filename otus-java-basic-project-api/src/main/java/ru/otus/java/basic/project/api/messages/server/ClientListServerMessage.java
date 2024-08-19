package ru.otus.java.basic.project.api.messages.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.otus.java.basic.project.api.messages.ClientServerMessage;
import ru.otus.java.basic.project.api.messages.client.OutgoingChallengeClientMessage;

import java.util.Collection;
import java.util.Objects;

public class ClientListServerMessage extends ClientServerMessage {
    @JsonProperty("c")
    private Collection<String> clients;

    public ClientListServerMessage() {
        super();
    }

    public ClientListServerMessage(Long context, Collection<String> clients) {
        super(context);
        if (clients == null) throw new IllegalArgumentException("clients is null");
        this.clients = clients;
    }

    public Collection<String> getClients() {
        return clients;
    }

    public void setClients(Collection<String> clients) {
        if (clients == null) throw new IllegalArgumentException("clients is null");
        this.clients = clients;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        ClientListServerMessage message = (ClientListServerMessage) obj;
        if (!Objects.equals(this.clients, message.clients)) return false;
        return true;
    }
}
