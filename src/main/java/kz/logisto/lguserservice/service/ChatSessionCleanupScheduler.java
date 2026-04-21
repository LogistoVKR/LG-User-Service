package kz.logisto.lguserservice.service;

import java.time.LocalDateTime;
import kz.logisto.lguserservice.data.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatSessionCleanupScheduler {

  private final ChatSessionRepository chatSessionRepository;

  @Scheduled(fixedRate = 3600000)
  @Transactional
  public void cleanupStaleSessions() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime threshold = now.minusHours(24);
    int closed = chatSessionRepository.closeStaleActiveSessions(threshold, now);
    if (closed > 0) {
      log.info("Closed {} stale chat sessions inactive since {}", closed, threshold);
    }
  }
}
