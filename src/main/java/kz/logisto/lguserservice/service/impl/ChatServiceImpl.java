package kz.logisto.lguserservice.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import kz.logisto.lguserservice.data.entity.ChatMessage;
import kz.logisto.lguserservice.data.entity.ChatSession;
import kz.logisto.lguserservice.data.enums.ChatSenderType;
import kz.logisto.lguserservice.data.enums.ChatSessionStatus;
import kz.logisto.lguserservice.data.repository.ChatMessageRepository;
import kz.logisto.lguserservice.data.repository.ChatSessionRepository;
import kz.logisto.lguserservice.data.repository.OrganizationRepository;
import kz.logisto.lguserservice.service.ChatService;
import kz.logisto.lguserservice.web.socket.dto.ChatSessionSummary;
import kz.logisto.lguserservice.web.socket.dto.ChatWsMessage;
import kz.logisto.lguserservice.web.socket.dto.ChatWsMessageSerializer;
import kz.logisto.lguserservice.web.socket.storage.ChatSessionStorage;
import kz.logisto.lguserservice.web.socket.wrapper.ChatWebSocketSessionWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

  private final ChatSessionStorage sessionStorage;
  private final ChatWsMessageSerializer serializer;
  private final ChatSessionRepository chatSessionRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final OrganizationRepository organizationRepository;

  @Override
  @Transactional
  public void initOrResumeChat(UUID anonymousId, UUID organizationId, String anonymousName,
      WebSocketSession anonymousSession) {
    var existing = chatSessionRepository
        .findByAnonymousIdAndStatus(anonymousId, ChatSessionStatus.ACTIVE);

    ChatSession chatSession;
    if (existing.isPresent()) {
      chatSession = existing.get();
      sendChatHistoryToSession(chatSession.getId(), anonymousSession);
    } else {
      chatSession = new ChatSession();
      chatSession.setOrganization(organizationRepository.getReferenceById(organizationId));
      chatSession.setAnonymousId(anonymousId);
      chatSession.setAnonymousName(anonymousName);
      chatSession.setStatus(ChatSessionStatus.ACTIVE);
      chatSession = chatSessionRepository.save(chatSession);
    }

    sendToSession(anonymousSession, ChatWsMessage.chatCreated(chatSession.getId()));

    ChatSessionSummary summary = new ChatSessionSummary(
        chatSession.getId(),
        chatSession.getAnonymousId(),
        chatSession.getAnonymousName(),
        chatSession.getStatus().name(),
        chatSession.getCreated(),
        null,
        null
    );
    broadcastToEmployees(organizationId, ChatWsMessage.chatCreated(chatSession.getId(), summary));
  }

  @Override
  @Transactional
  public void handleAnonymousMessage(UUID anonymousId, ChatWsMessage message) {
    var chatSession = chatSessionRepository
        .findByAnonymousIdAndStatus(anonymousId, ChatSessionStatus.ACTIVE)
        .orElse(null);

    if (chatSession == null) {
      ChatWebSocketSessionWrapper anonWs = sessionStorage.getAnonymousSession(anonymousId);
      if (anonWs != null) {
        sendToSession(anonWs, ChatWsMessage.error("No active chat session"));
      }
      return;
    }

    var chatMessage = new ChatMessage();
    chatMessage.setChatSession(chatSession);
    chatMessage.setSenderType(ChatSenderType.ANONYMOUS);
    chatMessage.setSenderId(anonymousId.toString());
    chatMessage.setContent(message.content());
    chatMessageRepository.save(chatMessage);

    String senderName = chatSession.getAnonymousName() != null
        ? chatSession.getAnonymousName() : "Anonymous";
    ChatWsMessage outbound = ChatWsMessage.newMessage(
        chatSession.getId(), message.content(), senderName);
    broadcastToEmployees(chatSession.getOrganization().getId(), outbound);
  }

  @Override
  @Transactional
  public void handleEmployeeMessage(String userId, UUID organizationId, String employeeName,
      ChatWsMessage message) {
    UUID chatSessionId = message.chatSessionId();
    if (chatSessionId == null) {
      return;
    }

    var chatSession = chatSessionRepository.findById(chatSessionId).orElse(null);
    if (chatSession == null || chatSession.getStatus() != ChatSessionStatus.ACTIVE) {
      return;
    }

    if (!chatSession.getOrganization().getId().equals(organizationId)) {
      return;
    }

    var chatMessage = new ChatMessage();
    chatMessage.setChatSession(chatSession);
    chatMessage.setSenderType(ChatSenderType.EMPLOYEE);
    chatMessage.setSenderId(userId);
    chatMessage.setContent(message.content());
    chatMessageRepository.save(chatMessage);

    String senderName = employeeName != null ? employeeName : "Support";
    ChatWsMessage outbound = ChatWsMessage.newMessage(chatSessionId, message.content(), senderName);

    ChatWebSocketSessionWrapper anonWs = sessionStorage.getAnonymousSession(
        chatSession.getAnonymousId());
    if (anonWs != null && anonWs.isOpen()) {
      sendToSession(anonWs, outbound);
    }

    broadcastToEmployees(organizationId, outbound);
  }

  @Override
  @Transactional
  public void handleCloseChat(UUID anonymousId) {
    var chatSession = chatSessionRepository
        .findByAnonymousIdAndStatus(anonymousId, ChatSessionStatus.ACTIVE)
        .orElse(null);
    if (chatSession == null) {
      return;
    }

    chatSession.setStatus(ChatSessionStatus.CLOSED);
    chatSession.setClosed(LocalDateTime.now());
    chatSessionRepository.save(chatSession);

    broadcastToEmployees(chatSession.getOrganization().getId(),
        ChatWsMessage.chatClosed(chatSession.getId()));
  }

  @Override
  @Transactional
  public void handleEmployeeCloseChat(String userId, UUID organizationId, ChatWsMessage message) {
    UUID chatSessionId = message.chatSessionId();
    if (chatSessionId == null) {
      return;
    }

    var chatSession = chatSessionRepository.findById(chatSessionId).orElse(null);
    if (chatSession == null || !chatSession.getOrganization().getId().equals(organizationId)) {
      return;
    }

    chatSession.setStatus(ChatSessionStatus.CLOSED);
    chatSession.setClosed(LocalDateTime.now());
    chatSessionRepository.save(chatSession);

    ChatWsMessage closedMsg = ChatWsMessage.chatClosed(chatSessionId);

    ChatWebSocketSessionWrapper anonWs = sessionStorage.getAnonymousSession(
        chatSession.getAnonymousId());
    if (anonWs != null && anonWs.isOpen()) {
      sendToSession(anonWs, closedMsg);
    }

    broadcastToEmployees(organizationId, closedMsg);
  }

  @Override
  public void sendActiveChatList(UUID organizationId, WebSocketSession session) {
    List<ChatSession> activeSessions = chatSessionRepository
        .findByOrganization_IdAndStatus(organizationId, ChatSessionStatus.ACTIVE);

    List<ChatSessionSummary> summaries = activeSessions.stream().map(cs -> {
      List<ChatMessage> messages = chatMessageRepository
          .findByChatSession_IdOrderByCreatedAsc(cs.getId());
      ChatMessage lastMsg = messages.isEmpty() ? null : messages.getLast();
      return new ChatSessionSummary(
          cs.getId(),
          cs.getAnonymousId(),
          cs.getAnonymousName(),
          cs.getStatus().name(),
          cs.getCreated(),
          lastMsg != null ? lastMsg.getContent() : null,
          lastMsg != null ? lastMsg.getCreated() : null
      );
    }).toList();

    sendToSession(session, ChatWsMessage.chatList(summaries));
  }

  @Override
  public void sendChatHistory(UUID organizationId, UUID chatSessionId, WebSocketSession session) {
    var chatSession = chatSessionRepository.findById(chatSessionId).orElse(null);
    if (chatSession == null || !chatSession.getOrganization().getId().equals(organizationId)) {
      sendToSession(session, ChatWsMessage.error("Chat session not found or access denied"));
      return;
    }
    sendChatHistoryToSession(chatSessionId, session);
  }

  private void broadcastToEmployees(UUID organizationId, ChatWsMessage message) {
    Set<ChatWebSocketSessionWrapper> employees = sessionStorage
        .getEmployeeSessions(organizationId);
    for (ChatWebSocketSessionWrapper wrapper : employees) {
      if (wrapper.isOpen()) {
        sendToSession(wrapper, message);
      }
    }
  }

  private void sendToSession(WebSocketSession session, ChatWsMessage message) {
    try {
      String payload = serializer.serialize(message);
      session.sendMessage(new TextMessage(payload));
    } catch (IOException e) {
      log.error("Cannot send message to session {}: {}", session.getId(), e.getMessage());
    }
  }

  private void sendChatHistoryToSession(UUID chatSessionId, WebSocketSession session) {
    List<ChatMessage> messages = chatMessageRepository
        .findByChatSession_IdOrderByCreatedAsc(chatSessionId);
    List<Map<String, Object>> history = messages.stream().map(m -> {
      Map<String, Object> map = new LinkedHashMap<>();
      map.put("id", m.getId());
      map.put("senderType", m.getSenderType().name());
      map.put("senderId", m.getSenderId());
      map.put("content", m.getContent());
      map.put("created", m.getCreated().toString());
      return map;
    }).toList();
    sendToSession(session, ChatWsMessage.chatHistory(chatSessionId, history));
  }
}
