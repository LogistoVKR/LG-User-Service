package kz.logisto.lguserservice.data.repository;

import java.util.List;
import java.util.UUID;
import kz.logisto.lguserservice.data.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

  List<ChatMessage> findByChatSession_IdOrderByCreatedAsc(UUID chatSessionId);
}
