package ru.otus.java.basic.project.go.api.messages.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.otus.java.basic.project.go.api.messages.ClientServerMessage;

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
        this.clients = clients;
    }

    public Collection<String> getClients() {
        return clients;
    }

    public void setClients(Collection<String> clients) {
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
