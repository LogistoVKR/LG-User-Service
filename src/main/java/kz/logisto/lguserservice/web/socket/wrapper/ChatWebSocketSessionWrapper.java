package kz.logisto.lguserservice.web.socket.wrapper;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import kz.logisto.lguserservice.web.socket.config.ChatWebSocketProperty;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

public class ChatWebSocketSessionWrapper extends ConcurrentWebSocketSessionDecorator {

  private final int maxRetries;
  private final AtomicInteger retries;
  private final AtomicBoolean isPongReceived = new AtomicBoolean(false);

  public ChatWebSocketSessionWrapper(ChatWebSocketProperty property, WebSocketSession session) {
    super(session, (int) property.getSendTimeLimit().toMillis(),
        property.getMessageBufferSize(), OverflowStrategy.DROP);
    this.maxRetries = property.getPingRetries();
    this.retries = new AtomicInteger(maxRetries);
  }

  public boolean isPongReceived() {
    if (!isPongReceived.get()) {
      return retries.decrementAndGet() > 0;
    }
    return true;
  }

  public void pongReceived() {
    this.retries.set(maxRetries);
    this.isPongReceived.set(true);
  }

  public void waitForNextPong() {
    this.isPongReceived.set(false);
  }
}
