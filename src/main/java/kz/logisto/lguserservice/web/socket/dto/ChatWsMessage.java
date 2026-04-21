package kz.logisto.lguserservice.web.socket.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatWsMessage(
    ChatMessageType type,
    UUID chatSessionId,
    String content,
    String senderName,
    Object data
) {

  public static ChatWsMessage error(String message) {
    return new ChatWsMessage(ChatMessageType.ERROR, null, message, null, null);
  }

  public static ChatWsMessage chatCreated(UUID chatSessionId) {
    return new ChatWsMessage(ChatMessageType.CHAT_CREATED, chatSessionId, null, null, null);
  }

  public static ChatWsMessage chatCreated(UUID chatSessionId, Object data) {
    return new ChatWsMessage(ChatMessageType.CHAT_CREATED, chatSessionId, null, null, data);
  }

  public static ChatWsMessage newMessage(UUID chatSessionId, String content, String senderName) {
    return new ChatWsMessage(ChatMessageType.NEW_MESSAGE, chatSessionId, content, senderName, null);
  }

  public static ChatWsMessage chatClosed(UUID chatSessionId) {
    return new ChatWsMessage(ChatMessageType.CHAT_CLOSED, chatSessionId, null, null, null);
  }

  public static ChatWsMessage chatList(Object list) {
    return new ChatWsMessage(ChatMessageType.CHAT_LIST, null, null, null, list);
  }

  public static ChatWsMessage chatHistory(UUID chatSessionId, Object messages) {
    return new ChatWsMessage(ChatMessageType.CHAT_HISTORY, chatSessionId, null, null, messages);
  }
}
