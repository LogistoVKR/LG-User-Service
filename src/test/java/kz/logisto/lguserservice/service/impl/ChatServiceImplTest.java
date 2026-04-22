package kz.logisto.lguserservice.service.impl;

import kz.logisto.lguserservice.data.entity.ChatMessage;
import kz.logisto.lguserservice.data.entity.ChatSession;
import kz.logisto.lguserservice.data.entity.Organization;
import kz.logisto.lguserservice.data.enums.ChatSenderType;
import kz.logisto.lguserservice.data.enums.ChatSessionStatus;
import kz.logisto.lguserservice.data.repository.ChatMessageRepository;
import kz.logisto.lguserservice.data.repository.ChatSessionRepository;
import kz.logisto.lguserservice.data.repository.OrganizationRepository;
import kz.logisto.lguserservice.web.socket.dto.ChatMessageType;
import kz.logisto.lguserservice.web.socket.dto.ChatWsMessage;
import kz.logisto.lguserservice.web.socket.dto.ChatWsMessageSerializer;
import kz.logisto.lguserservice.web.socket.storage.ChatSessionStorage;
import kz.logisto.lguserservice.web.socket.wrapper.ChatWebSocketSessionWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

  @Mock
  private ChatSessionStorage sessionStorage;
  @Mock
  private ChatWsMessageSerializer serializer;
  @Mock
  private ChatSessionRepository chatSessionRepository;
  @Mock
  private ChatMessageRepository chatMessageRepository;
  @Mock
  private OrganizationRepository organizationRepository;

  @InjectMocks
  private ChatServiceImpl chatService;

  @Test
  void initOrResumeChat_newSession_createsAndBroadcasts() throws IOException {
    UUID anonymousId = UUID.randomUUID();
    UUID orgId = UUID.randomUUID();
    Organization org = new Organization();
    org.setId(orgId);
    WebSocketSession wsSession = mock(WebSocketSession.class);

    when(chatSessionRepository.findByAnonymousIdAndStatus(anonymousId, ChatSessionStatus.ACTIVE))
        .thenReturn(Optional.empty());
    when(organizationRepository.getReferenceById(orgId)).thenReturn(org);

    ChatSession savedSession = new ChatSession();
    savedSession.setId(UUID.randomUUID());
    savedSession.setAnonymousId(anonymousId);
    savedSession.setAnonymousName("Guest");
    savedSession.setStatus(ChatSessionStatus.ACTIVE);
    savedSession.setOrganization(org);
    savedSession.setCreated(LocalDateTime.now());
    when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(savedSession);
    when(serializer.serialize(any(ChatWsMessage.class))).thenReturn("{}");
    when(sessionStorage.getEmployeeSessions(orgId)).thenReturn(Set.of());

    chatService.initOrResumeChat(anonymousId, orgId, "Guest", wsSession);

    verify(chatSessionRepository).save(any(ChatSession.class));
    verify(wsSession).sendMessage(any(TextMessage.class));
  }

  @Test
  void initOrResumeChat_existingSession_resumesAndSendsHistory() throws IOException {
    UUID anonymousId = UUID.randomUUID();
    UUID orgId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    Organization org = new Organization();
    org.setId(orgId);
    WebSocketSession wsSession = mock(WebSocketSession.class);

    ChatSession existingSession = new ChatSession();
    existingSession.setId(sessionId);
    existingSession.setAnonymousId(anonymousId);
    existingSession.setAnonymousName("Guest");
    existingSession.setStatus(ChatSessionStatus.ACTIVE);
    existingSession.setOrganization(org);
    existingSession.setCreated(LocalDateTime.now());

    when(chatSessionRepository.findByAnonymousIdAndStatus(anonymousId, ChatSessionStatus.ACTIVE))
        .thenReturn(Optional.of(existingSession));
    when(chatMessageRepository.findByChatSession_IdOrderByCreatedAsc(sessionId))
        .thenReturn(List.of());
    when(serializer.serialize(any(ChatWsMessage.class))).thenReturn("{}");
    when(sessionStorage.getEmployeeSessions(orgId)).thenReturn(Set.of());

    chatService.initOrResumeChat(anonymousId, orgId, "Guest", wsSession);

    verify(chatSessionRepository, never()).save(any(ChatSession.class));
    verify(chatMessageRepository).findByChatSession_IdOrderByCreatedAsc(sessionId);
  }

  @Test
  void handleAnonymousMessage_activeSession_savesAndBroadcasts() throws IOException {
    UUID anonymousId = UUID.randomUUID();
    UUID orgId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    Organization org = new Organization();
    org.setId(orgId);

    ChatSession session = new ChatSession();
    session.setId(sessionId);
    session.setAnonymousId(anonymousId);
    session.setAnonymousName("Guest");
    session.setOrganization(org);
    session.setStatus(ChatSessionStatus.ACTIVE);

    ChatWsMessage message = new ChatWsMessage(ChatMessageType.SEND_MESSAGE, sessionId,
        "Hello!", null, null);

    when(chatSessionRepository.findByAnonymousIdAndStatus(anonymousId, ChatSessionStatus.ACTIVE))
        .thenReturn(Optional.of(session));
    when(chatMessageRepository.save(any(ChatMessage.class)))
        .thenAnswer(inv -> inv.getArgument(0));
    when(sessionStorage.getEmployeeSessions(orgId)).thenReturn(Set.of());

    chatService.handleAnonymousMessage(anonymousId, message);

    ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
    verify(chatMessageRepository).save(captor.capture());
    ChatMessage saved = captor.getValue();
    assertEquals(ChatSenderType.ANONYMOUS, saved.getSenderType());
    assertEquals("Hello!", saved.getContent());
    assertEquals(anonymousId.toString(), saved.getSenderId());
  }

  @Test
  void handleAnonymousMessage_noSession_sendsError() throws IOException {
    UUID anonymousId = UUID.randomUUID();
    ChatWsMessage message = new ChatWsMessage(ChatMessageType.SEND_MESSAGE, null, "Hello!",
        null, null);

    when(chatSessionRepository.findByAnonymousIdAndStatus(anonymousId, ChatSessionStatus.ACTIVE))
        .thenReturn(Optional.empty());

    ChatWebSocketSessionWrapper wrapper = mock(ChatWebSocketSessionWrapper.class);
    when(sessionStorage.getAnonymousSession(anonymousId)).thenReturn(wrapper);
    when(serializer.serialize(any(ChatWsMessage.class))).thenReturn("{}");

    chatService.handleAnonymousMessage(anonymousId, message);

    verify(chatMessageRepository, never()).save(any());
    verify(wrapper).sendMessage(any(TextMessage.class));
  }

  @Test
  void handleEmployeeMessage_activeSession_savesAndSendsToAnonymous() throws IOException {
    UUID orgId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    UUID anonymousId = UUID.randomUUID();
    Organization org = new Organization();
    org.setId(orgId);

    ChatSession session = new ChatSession();
    session.setId(sessionId);
    session.setAnonymousId(anonymousId);
    session.setOrganization(org);
    session.setStatus(ChatSessionStatus.ACTIVE);

    ChatWsMessage message = new ChatWsMessage(ChatMessageType.SEND_MESSAGE, sessionId,
        "Response", null, null);

    when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
    when(chatMessageRepository.save(any(ChatMessage.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    ChatWebSocketSessionWrapper anonWrapper = mock(ChatWebSocketSessionWrapper.class);
    when(anonWrapper.isOpen()).thenReturn(true);
    when(sessionStorage.getAnonymousSession(anonymousId)).thenReturn(anonWrapper);
    when(serializer.serialize(any(ChatWsMessage.class))).thenReturn("{}");
    when(sessionStorage.getEmployeeSessions(orgId)).thenReturn(Set.of());

    chatService.handleEmployeeMessage("emp-1", orgId, "Employee", message);

    ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
    verify(chatMessageRepository).save(captor.capture());
    ChatMessage saved = captor.getValue();
    assertEquals(ChatSenderType.EMPLOYEE, saved.getSenderType());
    assertEquals("Response", saved.getContent());
    verify(anonWrapper).sendMessage(any(TextMessage.class));
  }

  @Test
  void handleEmployeeMessage_nullSessionId_doesNothing() {
    ChatWsMessage message = new ChatWsMessage(ChatMessageType.SEND_MESSAGE, null,
        "Response", null, null);

    chatService.handleEmployeeMessage("emp-1", UUID.randomUUID(), "Employee", message);

    verify(chatSessionRepository, never()).findById(any());
    verify(chatMessageRepository, never()).save(any());
  }

  @Test
  void handleEmployeeMessage_wrongOrganization_doesNothing() {
    UUID sessionId = UUID.randomUUID();
    UUID orgId = UUID.randomUUID();
    UUID wrongOrgId = UUID.randomUUID();
    Organization org = new Organization();
    org.setId(orgId);

    ChatSession session = new ChatSession();
    session.setId(sessionId);
    session.setOrganization(org);
    session.setStatus(ChatSessionStatus.ACTIVE);

    ChatWsMessage message = new ChatWsMessage(ChatMessageType.SEND_MESSAGE, sessionId,
        "Response", null, null);

    when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

    chatService.handleEmployeeMessage("emp-1", wrongOrgId, "Employee", message);

    verify(chatMessageRepository, never()).save(any());
  }

  @Test
  void handleCloseChat_activeSession_closesAndBroadcasts() {
    UUID anonymousId = UUID.randomUUID();
    UUID orgId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    Organization org = new Organization();
    org.setId(orgId);

    ChatSession session = new ChatSession();
    session.setId(sessionId);
    session.setAnonymousId(anonymousId);
    session.setOrganization(org);
    session.setStatus(ChatSessionStatus.ACTIVE);

    when(chatSessionRepository.findByAnonymousIdAndStatus(anonymousId, ChatSessionStatus.ACTIVE))
        .thenReturn(Optional.of(session));
    when(chatSessionRepository.save(session)).thenReturn(session);
    when(sessionStorage.getEmployeeSessions(orgId)).thenReturn(Set.of());

    chatService.handleCloseChat(anonymousId);

    assertEquals(ChatSessionStatus.CLOSED, session.getStatus());
    assertNotNull(session.getClosed());
    verify(chatSessionRepository).save(session);
  }

  @Test
  void handleCloseChat_noSession_doesNothing() {
    UUID anonymousId = UUID.randomUUID();

    when(chatSessionRepository.findByAnonymousIdAndStatus(anonymousId, ChatSessionStatus.ACTIVE))
        .thenReturn(Optional.empty());

    chatService.handleCloseChat(anonymousId);

    verify(chatSessionRepository, never()).save(any());
  }

  @Test
  void handleEmployeeCloseChat_closesAndNotifiesBoth() throws IOException {
    UUID orgId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    UUID anonymousId = UUID.randomUUID();
    Organization org = new Organization();
    org.setId(orgId);

    ChatSession session = new ChatSession();
    session.setId(sessionId);
    session.setAnonymousId(anonymousId);
    session.setOrganization(org);
    session.setStatus(ChatSessionStatus.ACTIVE);

    ChatWsMessage message = new ChatWsMessage(ChatMessageType.CLOSE_CHAT, sessionId,
        null, null, null);

    when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
    when(chatSessionRepository.save(session)).thenReturn(session);

    ChatWebSocketSessionWrapper anonWrapper = mock(ChatWebSocketSessionWrapper.class);
    when(anonWrapper.isOpen()).thenReturn(true);
    when(sessionStorage.getAnonymousSession(anonymousId)).thenReturn(anonWrapper);
    when(serializer.serialize(any(ChatWsMessage.class))).thenReturn("{}");
    when(sessionStorage.getEmployeeSessions(orgId)).thenReturn(Set.of());

    chatService.handleEmployeeCloseChat("emp-1", orgId, message);

    assertEquals(ChatSessionStatus.CLOSED, session.getStatus());
    assertNotNull(session.getClosed());
    verify(anonWrapper).sendMessage(any(TextMessage.class));
  }
}
