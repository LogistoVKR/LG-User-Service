package kz.logisto.lguserservice.web.socket.storage;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import kz.logisto.lguserservice.web.socket.config.ChatWebSocketProperty;
import kz.logisto.lguserservice.web.socket.wrapper.ChatWebSocketSessionWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatSessionStorage {

  private final ChatWebSocketProperty property;

  private final Map<UUID, ChatWebSocketSessionWrapper> anonymousSessions = new ConcurrentHashMap<>();
  private final Map<UUID, Set<ChatWebSocketSessionWrapper>> employeeSessions = new ConcurrentHashMap<>();
  private final Map<String, ChatWebSocketSessionWrapper> allSessions = new ConcurrentHashMap<>();

  public ChatWebSocketSessionWrapper addAnonymousSession(UUID anonymousId,
      WebSocketSession session) {
    var wrapper = new ChatWebSocketSessionWrapper(property, session);
    anonymousSessions.put(anonymousId, wrapper);
    allSessions.put(session.getId(), wrapper);
    return wrapper;
  }

  public ChatWebSocketSessionWrapper addEmployeeSession(UUID organizationId,
      WebSocketSession session) {
    var wrapper = new ChatWebSocketSessionWrapper(property, session);
    employeeSessions
        .computeIfAbsent(organizationId, k -> ConcurrentHashMap.newKeySet())
        .add(wrapper);
    allSessions.put(session.getId(), wrapper);
    return wrapper;
  }

  public ChatWebSocketSessionWrapper getAnonymousSession(UUID anonymousId) {
    return anonymousSessions.get(anonymousId);
  }

  public Set<ChatWebSocketSessionWrapper> getEmployeeSessions(UUID organizationId) {
    return employeeSessions.getOrDefault(organizationId, Set.of());
  }

  public ChatWebSocketSessionWrapper getBySessionId(String sessionId) {
    return allSessions.get(sessionId);
  }

  public void removeAnonymousSession(UUID anonymousId, WebSocketSession session) {
    anonymousSessions.remove(anonymousId);
    allSessions.remove(session.getId());
  }

  public void removeEmployeeSession(UUID organizationId, WebSocketSession session) {
    Set<ChatWebSocketSessionWrapper> set = employeeSessions.get(organizationId);
    if (set != null) {
      set.removeIf(w -> w.getId().equals(session.getId()));
      if (set.isEmpty()) {
        employeeSessions.remove(organizationId);
      }
    }
    allSessions.remove(session.getId());
  }
}
