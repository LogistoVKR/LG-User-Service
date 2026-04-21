package kz.logisto.lguserservice.web.socket.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import kz.logisto.lguserservice.web.socket.config.ChatWebSocketProperty;
import kz.logisto.lguserservice.web.socket.wrapper.ChatWebSocketSessionWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatPingPongService {

  private final ChatWebSocketProperty property;

  private final Map<String, ScheduledFuture<?>> pingPongs = new ConcurrentHashMap<>();
  private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(0,
      Thread.ofVirtual().factory());

  private static final PingMessage PING = new PingMessage();

  public void startPingPong(ChatWebSocketSessionWrapper wrapper) {
    ScheduledFuture<?> future = executor.scheduleAtFixedRate(() -> {
      if (wrapper.isPongReceived()) {
        wrapper.waitForNextPong();
        sendPing(wrapper);
      } else {
        closeSession(wrapper, CloseStatus.GOING_AWAY);
      }
    }, property.getPingInterval().toSeconds(), property.getPingInterval().toSeconds(),
        TimeUnit.SECONDS);
    pingPongs.put(wrapper.getId(), future);
  }

  public void stopPingPong(String sessionId) {
    ScheduledFuture<?> future = pingPongs.remove(sessionId);
    if (future != null && !future.isCancelled() && !future.isDone()) {
      future.cancel(true);
    }
  }

  private void sendPing(ChatWebSocketSessionWrapper wrapper) {
    try {
      wrapper.sendMessage(PING);
    } catch (IOException e) {
      log.error("Cannot send ping to session {}: {}", wrapper.getId(), e.getMessage());
    }
  }

  private void closeSession(ChatWebSocketSessionWrapper wrapper, CloseStatus status) {
    try {
      if (wrapper.isOpen()) {
        wrapper.close(status);
      }
    } catch (IOException e) {
      log.error("Cannot close session {}: {}", wrapper.getId(), e.getMessage());
    }
  }
}
