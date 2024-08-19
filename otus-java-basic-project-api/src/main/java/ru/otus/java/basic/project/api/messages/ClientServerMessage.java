package ru.otus.java.basic.project.api.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.otus.java.basic.project.api.exceptions.MessageProcessingException;
import ru.otus.java.basic.project.api.messages.client.*;
import ru.otus.java.basic.project.api.messages.server.*;

import java.util.Objects;

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

    /**
     * Create a new message with associated context id
     *
     * @param contextId an associated context id
     */
    protected ClientServerMessage(Long contextId) {
        this.contextId = contextId;
    }

    /**
     * Serialize the client-server message into appropriate JSON string
     *
     * @return the JSON string
     * @throws MessageProcessingException in case of some problem with the JSON serializer
     */
    public String serialize() throws MessageProcessingException {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new MessageProcessingException(e);
        }
    }

    /**
     * Deserialize a client-server message into a new object of an appropriate Java class
     *
     * @param json a JSON string
     * @return a new deserialized object of the appropriate Java class
     * @throws MessageProcessingException in case of some problem with the JSON serializer
     */
    public static ClientServerMessage deserialize(String json) throws MessageProcessingException {
        try {
            return new ObjectMapper().readValue(json, ClientServerMessage.class);
        } catch (JsonProcessingException e) {
            throw new MessageProcessingException(e);
        }
    }

    /**
     * @return associated context id
     */
    public Long getContextId() {
        return contextId;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null &&
                Objects.equals(this.getClass(), obj.getClass()) &&
                Objects.equals(this.contextId, ((ClientServerMessage) obj).contextId);
    }
}
