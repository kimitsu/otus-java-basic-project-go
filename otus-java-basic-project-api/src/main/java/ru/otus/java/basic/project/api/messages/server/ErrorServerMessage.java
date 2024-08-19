package ru.otus.java.basic.project.api.messages.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.otus.java.basic.project.api.messages.ClientServerMessage;

public class ErrorServerMessage extends ClientServerMessage {

    @JsonProperty("e")
    private String errorMessage;


    public ErrorServerMessage() {
        super();
    }

    public ErrorServerMessage(Long context, String errorMessage) {
        super(context);
        if (errorMessage == null) throw new IllegalArgumentException("errorMessage is null");
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        if (errorMessage == null) throw new IllegalArgumentException("errorMessage is null");
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        ErrorServerMessage message = (ErrorServerMessage) obj;
        if (!message.errorMessage.equals(this.errorMessage)) return false;
        return true;
    }
}
