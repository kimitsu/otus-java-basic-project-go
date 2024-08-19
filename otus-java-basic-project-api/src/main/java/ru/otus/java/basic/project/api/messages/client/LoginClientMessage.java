package ru.otus.java.basic.project.api.messages.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.otus.java.basic.project.api.messages.ClientServerMessage;

import java.util.Objects;

public class LoginClientMessage extends ClientServerMessage {
    @JsonProperty("c")
    private String name;
    @JsonProperty("p")
    private String password;
    @JsonProperty("r")
    private boolean register;

    public LoginClientMessage() {
        super();
    }

    public LoginClientMessage(Long context, String name, String password, boolean register) {
        super(context);
        this.name = name;
        this.password = password;
        this.register = register;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public boolean isRegister() {
        return register;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        LoginClientMessage message = (LoginClientMessage) obj;
        if (!Objects.equals(this.name, message.name)) return false;
        if (!Objects.equals(this.password, message.password)) return false;
        if (!Objects.equals(this.register, message.register)) return false;
        return true;
    }
}
