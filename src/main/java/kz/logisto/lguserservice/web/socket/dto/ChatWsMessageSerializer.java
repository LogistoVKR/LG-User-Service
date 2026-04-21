package kz.logisto.lguserservice.web.socket.dto;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class ChatWsMessageSerializer {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  static {
    MAPPER.registerModule(new JavaTimeModule());
    MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  public String serialize(ChatWsMessage message) throws IOException {
    return MAPPER.writeValueAsString(message);
  }

  public ChatWsMessage deserialize(String json) throws IOException {
    return MAPPER.readValue(json, ChatWsMessage.class);
  }
}
