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
public class AnonymousChatHandler extends AbstractWebSocketHandler {

  private final ChatService chatService;
  private final ChatSessionStorage storage;
  private final ChatPingPongService pingPongService;
  private final ChatWsMessageSerializer serializer;

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    UUID anonymousId = (UUID) session.getAttributes().get("anonymousId");
    UUID organizationId = (UUID) session.getAttributes().get("organizationId");
    String anonymousName = (String) session.getAttributes().get("anonymousName");

    ChatWebSocketSessionWrapper wrapper = storage.addAnonymousSession(anonymousId, session);
    pingPongService.startPingPong(wrapper);

    log.debug("Anonymous session established: sessionId={}, anonymousId={}, orgId={}",
        session.getId(), anonymousId, organizationId);

    chatService.initOrResumeChat(anonymousId, organizationId, anonymousName, session);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    UUID anonymousId = (UUID) session.getAttributes().get("anonymousId");
    try {
      ChatWsMessage wsMessage = serializer.deserialize(message.getPayload());
      switch (wsMessage.type()) {
        case SEND_MESSAGE -> chatService.handleAnonymousMessage(anonymousId, wsMessage);
        case CLOSE_CHAT -> chatService.handleCloseChat(anonymousId);
        default -> sendError(session, "Unsupported message type: " + wsMessage.type());
      }
    } catch (IOException e) {
      log.warn("Cannot deserialize message from anonymous session {}: {}",
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
    log.error("Transport error in anonymous session {}: {}",
        session.getId(), exception.getMessage());
    closeSession(session, CloseStatus.SERVER_ERROR);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    UUID anonymousId = (UUID) session.getAttributes().get("anonymousId");
    pingPongService.stopPingPong(session.getId());
    storage.removeAnonymousSession(anonymousId, session);
    log.debug("Anonymous session closed: sessionId={}, anonymousId={}, status={}",
        session.getId(), anonymousId, status);
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
