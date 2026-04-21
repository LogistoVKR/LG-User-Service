package kz.logisto.lguserservice.web.socket.handler;

import java.io.IOException;
import java.util.UUID;
import kz.logisto.lguserservice.service.ChatService;
import kz.logisto.lguserservice.web.socket.dto.ChatWsMessage;
import kz.logisto.lguserservice.web.socket.dto.ChatWsMessageSerializer;
import kz.logisto.lguserservice.web.socket.service.ChatPingPongService;
import kz.logisto.lguserservice.web.socket.storage.ChatSessionStorage;
import kz.logisto.lguserservice.web.socket.wrapper.ChatWebSocketSessionWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeChatHandler extends AbstractWebSocketHandler {

  private final ChatService chatService;
  private final ChatSessionStorage storage;
  private final ChatPingPongService pingPongService;
  private final ChatWsMessageSerializer serializer;

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    String userId = (String) session.getAttributes().get("userId");
    UUID organizationId = (UUID) session.getAttributes().get("organizationId");

    ChatWebSocketSessionWrapper wrapper = storage.addEmployeeSession(organizationId, session);
    pingPongService.startPingPong(wrapper);

    log.debug("Employee session established: sessionId={}, userId={}, orgId={}",
        session.getId(), userId, organizationId);

    chatService.sendActiveChatList(organizationId, session);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    String userId = (String) session.getAttributes().get("userId");
    UUID organizationId = (UUID) session.getAttributes().get("organizationId");
    String employeeName = (String) session.getAttributes().get("employeeName");
    try {
      ChatWsMessage wsMessage = serializer.deserialize(message.getPayload());
      switch (wsMessage.type()) {
        case SEND_MESSAGE -> chatService.handleEmployeeMessage(userId, organizationId,
            employeeName, wsMessage);
        case LIST_CHATS -> chatService.sendActiveChatList(organizationId, session);
        case CLOSE_CHAT -> chatService.handleEmployeeCloseChat(userId, organizationId, wsMessage);
        case LOAD_HISTORY -> chatService.sendChatHistory(organizationId,
            wsMessage.chatSessionId(), session);
        default -> sendError(session, "Unsupported message type: " + wsMessage.type());
      }
    } catch (IOException e) {
      log.warn("Cannot deserialize message from employee session {}: {}",
          session.getId(), e.getMessage());
      sendError(session, "Invalid message format");
    }
  }

  @Override
  protected void handlePongMessage(WebSocketSession session, PongMessage message) {
    ChatWebSocketSessionWrapper wrapper = storage.getBySessionId(session.getId());
    if (wrapper != null) {
      wrapper.pongReceived();
    }
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) {
    log.error("Transport error in employee session {}: {}",
        session.getId(), exception.getMessage());
    closeSession(session, CloseStatus.SERVER_ERROR);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    UUID organizationId = (UUID) session.getAttributes().get("organizationId");
    pingPongService.stopPingPong(session.getId());
    storage.removeEmployeeSession(organizationId, session);
    log.debug("Employee session closed: sessionId={}, status={}", session.getId(), status);
  }

  private void sendError(WebSocketSession session, String msg) {
    try {
      String payload = serializer.serialize(ChatWsMessage.error(msg));
      session.sendMessage(new TextMessage(payload));
    } catch (IOException e) {
      log.error("Cannot send error to session {}", session.getId(), e);
    }
  }

  private void closeSession(WebSocketSession session, CloseStatus status) {
    try {
      if (session.isOpen()) {
        session.close(status);
      }
    } catch (IOException e) {
      log.error("Cannot close session {}", session.getId(), e);
    }
  }
}
