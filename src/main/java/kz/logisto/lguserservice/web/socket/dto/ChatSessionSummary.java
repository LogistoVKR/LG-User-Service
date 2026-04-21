package kz.logisto.lguserservice.web.socket.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatSessionSummary(
    UUID id,
    UUID anonymousId,
    String anonymousName,
    String status,
    LocalDateTime created,
    String lastMessage,
    LocalDateTime lastMessageTime
) {

}
