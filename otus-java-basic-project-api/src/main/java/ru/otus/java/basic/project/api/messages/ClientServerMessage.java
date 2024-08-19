package ru.otus.java.basic.project.api.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.otus.java.basic.project.api.messages.client.*;
import ru.otus.java.basic.project.api.exceptions.MessageProcessingException;
import ru.otus.java.basic.project.api.messages.server.*;

/**
 * A super-class for all client-server messages.
 * Uses jackson to serialize/deserialize messages to various classes according to "m" JSON field.
 * All messages contain contextId ("ctx") field, see <code>Context</code>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "m")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LoginClientMessage.class, name = "LOGIN"),
        @JsonSubTypes.Type(value = LoginConfirmedServerMessage.class, name = "LOGIN_OK"),
        @JsonSubTypes.Type(value = GetListClientMessage.class, name = "LIST_GET"),
        @JsonSubTypes.Type(value = ClientListServerMessage.class, name = "LIST"),
        @JsonSubTypes.Type(value = OutgoingChallengeClientMessage.class, name = "CHLNG_OUT"),
        @JsonSubTypes.Type(value = IncomingChallengeServerMessage.class, name = "CHLNG_INC"),
        @JsonSubTypes.Type(value = ChallengeResponseClientMessage.class, name = "CHLNG_RESP_OUT"),
        @JsonSubTypes.Type(value = ChallengeResponseServerMessage.class, name = "CHLNG_RESP_IN"),
        @JsonSubTypes.Type(value = CancelChallengeClientMessage.class, name = "CHLNG_CNCL_OUT"),
        @JsonSubTypes.Type(value = CancelChallengeServerMessage.class, name = "CHLNG_CNCL_IN"),
        @JsonSubTypes.Type(value = RequestGameInfoClientMessage.class, name = "GAME_GET"),
        @JsonSubTypes.Type(value = GameMoveClientMessage.class, name = "GAME_MOVE"),
        @JsonSubTypes.Type(value = GameInfoServerMessage.class, name = "GAME_INFO"),
        @JsonSubTypes.Type(value = GameStateServerMessage.class, name = "GAME_STATE"),
        @JsonSubTypes.Type(value = ErrorServerMessage.class, name = "ERROR"),
})
public abstract class ClientServerMessage {
    @JsonProperty("ctx")
    private Long contextId;

    protected ClientServerMessage() {
    }
    protected ClientServerMessage(Long contextId) {
        this.contextId = contextId;
    }

    public String serialize() throws MessageProcessingException {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new MessageProcessingException(e);
        }
    }

    public static ClientServerMessage deserialize(String json) throws MessageProcessingException {
        try {
            return new ObjectMapper().readValue(json, ClientServerMessage.class);
        } catch (JsonProcessingException e) {
            throw new MessageProcessingException(e);
        }
    }

    public Long getContextId() {
        return contextId;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass().equals(this.getClass());
    }
}
