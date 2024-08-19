package ru.otus.java.basic.project.go.api.messages.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.otus.java.basic.project.go.api.messages.ClientServerMessage;

import java.util.Objects;

public class ErrorServerMessage extends ClientServerMessage {

    @JsonProperty("e")
    private String errorMessage;


    public ErrorServerMessage() {
        super();
    }

    public ErrorServerMessage(Long context, String errorMessage) {
        super(context);
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        ErrorServerMessage message = (ErrorServerMessage) obj;
        if (!Objects.equals(this.errorMessage, message.errorMessage)) return false;
        return true;
    }
}
