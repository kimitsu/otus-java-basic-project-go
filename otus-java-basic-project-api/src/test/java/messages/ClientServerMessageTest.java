package messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.otus.java.basic.project.api.enums.ChallengeResponse;
import ru.otus.java.basic.project.api.enums.MoveType;
import ru.otus.java.basic.project.api.exceptions.MessageProcessingException;
import ru.otus.java.basic.project.api.messages.ClientServerMessage;
import ru.otus.java.basic.project.api.messages.client.ChallengeResponseClientMessage;
import ru.otus.java.basic.project.api.messages.client.GameMoveClientMessage;
import ru.otus.java.basic.project.api.messages.client.GetListClientMessage;
import ru.otus.java.basic.project.api.messages.client.OutgoingChallengeClientMessage;
import ru.otus.java.basic.project.api.messages.server.ClientListServerMessage;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ClientServerMessageTest {
    @Test
    public void shouldDeserializeGetList() throws MessageProcessingException {
        String json = "{\"m\":\"LIST_GET\",\"ctx\":123}";
        ClientServerMessage message = new GetListClientMessage(123L);
        assertEquals(message, ClientServerMessage.deserialize(json));
    }

    @Test
    public void shouldSerializeGetList() throws MessageProcessingException {
        String json = "{\"m\":\"LIST_GET\",\"ctx\":123}";
        ClientServerMessage message = new GetListClientMessage(123L);
        assertEquals(json, message.serialize());
    }

    @Test
    public void shouldDeserializeChallenge() throws MessageProcessingException {
        String json = "{\"m\":\"CHLNG_OUT\",\"ctx\":123,\"c\":\"Jebrony\"}";
        ClientServerMessage message = new OutgoingChallengeClientMessage(123L, "Jebrony");
        assertEquals(message, ClientServerMessage.deserialize(json));
    }

    @Test
    public void shouldSerializeChallenge() throws MessageProcessingException {
        String json = "{\"m\":\"CHLNG_OUT\",\"ctx\":123,\"c\":\"Jebrony\"}";
        ClientServerMessage message = new OutgoingChallengeClientMessage(123L, "Jebrony");
        assertEquals(json, message.serialize());
    }

    @Test
    public void shouldSerializeChallengeResponse() throws MessageProcessingException {
        String json = "{\"m\":\"CHLNG_RESP_OUT\",\"ctx\":123,\"a\":\"REJECTED\"}";
        ClientServerMessage message = new ChallengeResponseClientMessage(123L, ChallengeResponse.REJECTED);
        assertEquals(json, message.serialize());
    }

    @Test
    public void shouldDeserializeChallengeResponse() throws MessageProcessingException {
        String json = "{\"m\":\"CHLNG_RESP_OUT\",\"ctx\":123,\"a\":\"REJECTED\"}";
        ClientServerMessage message = new ChallengeResponseClientMessage(123L, ChallengeResponse.REJECTED);
        assertEquals(message, ClientServerMessage.deserialize(json));
    }

    @Test
    public void shouldSerializeMove() throws MessageProcessingException {
        String json = "{\"m\":\"GAME_MOVE\",\"ctx\":123,\"m\":\"STONE\",\"x\":2,\"y\":3}";
        ClientServerMessage message = new GameMoveClientMessage(123L, MoveType.STONE, 2, 3);
        assertEquals(json, message.serialize());
    }
    @Test
    public void shouldDeserializeMove() throws MessageProcessingException {
        String json = "{\"m\":\"GAME_MOVE\",\"ctx\":123,\"m\":\"STONE\",\"x\":2,\"y\":3}";
        ClientServerMessage message = new GameMoveClientMessage(123L, MoveType.STONE, 2, 3);
        assertEquals(message, ClientServerMessage.deserialize(json));
    }
    @Test
    public void shouldDistinguishMoves() throws MessageProcessingException {
        String json = "{\"m\":\"GAME_MOVE\",\"ctx\":123,\"m\":\"STONE\",\"x\":2,\"y\":3}";
        ClientServerMessage message = new GameMoveClientMessage(123L, MoveType.STONE, 2, 4);
        assertNotEquals(message, ClientServerMessage.deserialize(json));
    }

    @Test
    public void shouldThrowOnBadMessage() {
        String json = "{\"m\":\"GET_LOST\"}";
        assertThrows(MessageProcessingException.class, () -> {
            ClientServerMessage.deserialize(json);
        });
    }
    @Test
    public void shouldSerializeClientList() throws JsonProcessingException {
        String json = "{\"m\":\"LIST\",\"ctx\":223,\"c\":[\"One\",\"Two\",\"Three\"]}";
        ClientServerMessage message = new ClientListServerMessage(223L, Arrays.asList("One", "Two", "Three"));
        Assertions.assertEquals(json, new ObjectMapper().writeValueAsString(message));
    }

    @Test
    public void shouldDeserializeClientList() throws JsonProcessingException {
        String json = "{\"m\":\"LIST\",\"ctx\":223,\"c\":[\"One\",\"Two\",\"Three\"]}";
        ClientServerMessage message = new ClientListServerMessage(223L, Arrays.asList("One", "Two", "Three"));
        Assertions.assertEquals(message, new ObjectMapper().readValue(json, ClientServerMessage.class));
    }

    @Test
    public void shouldDistinguishClientList() throws JsonProcessingException {
        String json = "{\"m\":\"LIST\",\"ctx\":444,\"c\":[\"Three\",\"Two\",\"One\"]}";
        ClientServerMessage message = new ClientListServerMessage(444L, Arrays.asList("One", "Two", "Three"));
        Assertions.assertNotEquals(message, new ObjectMapper().readValue(json, ClientServerMessage.class));
    }

}