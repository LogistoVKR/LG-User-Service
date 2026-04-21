package kz.logisto.lguserservice.data.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kz.logisto.lguserservice.data.entity.ChatSession;
import kz.logisto.lguserservice.data.enums.ChatSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {

  List<ChatSession> findByOrganization_IdAndStatus(UUID organizationId, ChatSessionStatus status);

  Optional<ChatSession> findByAnonymousIdAndStatus(UUID anonymousId, ChatSessionStatus status);

  @Modifying
  @Query("""
      UPDATE ChatSession cs SET cs.status = 'CLOSED', cs.closed = :now
      WHERE cs.status = 'ACTIVE'
        AND cs.id NOT IN (
          SELECT DISTINCT cm.chatSession.id FROM ChatMessage cm
          WHERE cm.created > :threshold
        )
        AND cs.created < :threshold
      """)
  int closeStaleActiveSessions(LocalDateTime threshold, LocalDateTime now);
}
