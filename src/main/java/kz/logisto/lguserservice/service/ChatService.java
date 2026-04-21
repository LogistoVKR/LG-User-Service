package kz.logisto.lguserservice.service;

import java.util.UUID;
import kz.logisto.lguserservice.web.socket.dto.ChatWsMessage;
import org.springframework.web.socket.WebSocketSession;

public interface ChatService {

  void initOrResumeChat(UUID anonymousId, UUID organizationId, String anonymousName,
      WebSocketSession anonymousSession);

  void handleAnonymousMessage(UUID anonymousId, ChatWsMessage message);

  void handleEmployeeMessage(String userId, UUID organizationId, String employeeName,
      ChatWsMessage message);

  void handleCloseChat(UUID anonymousId);

  void handleEmployeeCloseChat(String userId, UUID organizationId, ChatWsMessage message);

  void sendActiveChatList(UUID organizationId, WebSocketSession session);

  void sendChatHistory(UUID organizationId, UUID chatSessionId, WebSocketSession session);
}
