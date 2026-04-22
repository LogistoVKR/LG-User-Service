package kz.logisto.lguserservice.integration;

import kz.logisto.lguserservice.BaseIntegrationTest;
import kz.logisto.lguserservice.data.entity.ChatMessage;
import kz.logisto.lguserservice.data.entity.ChatSession;
import kz.logisto.lguserservice.data.entity.Organization;
import kz.logisto.lguserservice.data.enums.ChatSenderType;
import kz.logisto.lguserservice.data.enums.ChatSessionStatus;
import kz.logisto.lguserservice.data.repository.ChatMessageRepository;
import kz.logisto.lguserservice.data.repository.ChatSessionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ChatIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private ChatSessionRepository chatSessionRepository;
  @Autowired
  private ChatMessageRepository chatMessageRepository;
  @Autowired
  private EntityManager entityManager;

  private Organization org;

  @BeforeEach
  void setUp() {
    org = createOrganization("Chat Org");
  }

  @AfterEach
  void tearDown() {
    chatMessageRepository.deleteAll();
    chatSessionRepository.deleteAll();
    cleanDatabase();
  }

  @Test
  void createChatSession_persistsCorrectly() {
    ChatSession session = createChatSession(org, UUID.randomUUID(), "Guest");

    assertNotNull(session.getId());
    assertEquals(ChatSessionStatus.ACTIVE, session.getStatus());
    assertEquals("Guest", session.getAnonymousName());
    assertNotNull(session.getCreated());
  }

  @Test
  void findByAnonymousIdAndStatus_active() {
    UUID anonymousId = UUID.randomUUID();
    createChatSession(org, anonymousId, "Guest");

    Optional<ChatSession> found = chatSessionRepository.findByAnonymousIdAndStatus(
        anonymousId, ChatSessionStatus.ACTIVE);

    assertTrue(found.isPresent());
    assertEquals(anonymousId, found.get().getAnonymousId());
  }

  @Test
  void findByAnonymousIdAndStatus_closed_notFoundAsActive() {
    UUID anonymousId = UUID.randomUUID();
    ChatSession session = createChatSession(org, anonymousId, "Guest");
    session.setStatus(ChatSessionStatus.CLOSED);
    session.setClosed(LocalDateTime.now());
    chatSessionRepository.save(session);

    Optional<ChatSession> found = chatSessionRepository.findByAnonymousIdAndStatus(
        anonymousId, ChatSessionStatus.ACTIVE);

    assertTrue(found.isEmpty());
  }

  @Test
  void findByOrganizationIdAndStatus() {
    createChatSession(org, UUID.randomUUID(), "Guest1");
    createChatSession(org, UUID.randomUUID(), "Guest2");

    ChatSession closedSession = createChatSession(org, UUID.randomUUID(), "Guest3");
    closedSession.setStatus(ChatSessionStatus.CLOSED);
    closedSession.setClosed(LocalDateTime.now());
    chatSessionRepository.save(closedSession);

    List<ChatSession> activeSessions = chatSessionRepository.findByOrganization_IdAndStatus(
        org.getId(), ChatSessionStatus.ACTIVE);

    assertEquals(2, activeSessions.size());
  }

  @Test
  void saveChatMessage_persistsWithCorrectSenderType() {
    ChatSession session = createChatSession(org, UUID.randomUUID(), "Guest");

    ChatMessage anonymousMsg = createChatMessage(session, ChatSenderType.ANONYMOUS,
        "anon-1", "Hello from visitor");
    ChatMessage employeeMsg = createChatMessage(session, ChatSenderType.EMPLOYEE,
        "emp-1", "Hello from support");

    assertNotNull(anonymousMsg.getId());
    assertEquals(ChatSenderType.ANONYMOUS, anonymousMsg.getSenderType());
    assertEquals(ChatSenderType.EMPLOYEE, employeeMsg.getSenderType());
  }

  @Test
  void findMessagesByChatSessionId_orderedByCreated() throws InterruptedException {
    ChatSession session = createChatSession(org, UUID.randomUUID(), "Guest");

    createChatMessage(session, ChatSenderType.ANONYMOUS, "anon-1", "First message");
    Thread.sleep(10);
    createChatMessage(session, ChatSenderType.EMPLOYEE, "emp-1", "Second message");
    Thread.sleep(10);
    createChatMessage(session, ChatSenderType.ANONYMOUS, "anon-1", "Third message");

    List<ChatMessage> messages = chatMessageRepository.findByChatSession_IdOrderByCreatedAsc(
        session.getId());

    assertEquals(3, messages.size());
    assertEquals("First message", messages.get(0).getContent());
    assertEquals("Second message", messages.get(1).getContent());
    assertEquals("Third message", messages.get(2).getContent());
  }

  @Test
  void closeChatSession_setsStatusAndTimestamp() {
    UUID anonymousId = UUID.randomUUID();
    ChatSession session = createChatSession(org, anonymousId, "Guest");

    session.setStatus(ChatSessionStatus.CLOSED);
    session.setClosed(LocalDateTime.now());
    chatSessionRepository.save(session);

    ChatSession closed = chatSessionRepository.findById(session.getId()).orElseThrow();
    assertEquals(ChatSessionStatus.CLOSED, closed.getStatus());
    assertNotNull(closed.getClosed());
  }

  @Test
  @Transactional
  void closeStaleActiveSessions_closesOldSessions() {
    ChatSession staleSession = new ChatSession();
    staleSession.setOrganization(org);
    staleSession.setAnonymousId(UUID.randomUUID());
    staleSession.setAnonymousName("Old Guest");
    staleSession.setStatus(ChatSessionStatus.ACTIVE);
    staleSession = chatSessionRepository.save(staleSession);
    entityManager.flush();
    entityManager.clear();

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime threshold = now.plusHours(25);

    int closed = chatSessionRepository.closeStaleActiveSessions(threshold, now);
    entityManager.flush();
    entityManager.clear();

    assertEquals(1, closed);

    ChatSession updated = chatSessionRepository.findById(staleSession.getId()).orElseThrow();
    assertEquals(ChatSessionStatus.CLOSED, updated.getStatus());
  }

  private ChatSession createChatSession(Organization org, UUID anonymousId, String name) {
    ChatSession session = new ChatSession();
    session.setOrganization(org);
    session.setAnonymousId(anonymousId);
    session.setAnonymousName(name);
    session.setStatus(ChatSessionStatus.ACTIVE);
    return chatSessionRepository.save(session);
  }

  private ChatMessage createChatMessage(ChatSession session, ChatSenderType senderType,
      String senderId, String content) {
    ChatMessage message = new ChatMessage();
    message.setChatSession(session);
    message.setSenderType(senderType);
    message.setSenderId(senderId);
    message.setContent(content);
    return chatMessageRepository.save(message);
  }
}
