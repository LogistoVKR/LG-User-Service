package kz.logisto.lguserservice.web.socket.interceptor;

import jakarta.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class AnonymousHandshakeInterceptor implements HandshakeInterceptor {

  @Override
  public boolean beforeHandshake(ServerHttpRequest request, @Nonnull ServerHttpResponse response,
      @Nonnull WebSocketHandler wsHandler, @Nonnull Map<String, Object> attributes) {
    var params = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();

    String orgIdParam = params.getFirst("organizationId");
    if (orgIdParam == null) {
      log.warn("Anonymous handshake rejected: missing organizationId parameter");
      return false;
    }

    try {
      UUID organizationId = UUID.fromString(orgIdParam);
      attributes.put("organizationId", organizationId);
    } catch (IllegalArgumentException e) {
      log.warn("Anonymous handshake rejected: invalid organizationId format");
      return false;
    }

    String anonymousIdParam = params.getFirst("anonymousId");
    if (anonymousIdParam != null) {
      try {
        attributes.put("anonymousId", UUID.fromString(anonymousIdParam));
      } catch (IllegalArgumentException e) {
        log.warn("Anonymous handshake rejected: invalid anonymousId format");
        return false;
      }
    } else {
      attributes.put("anonymousId", UUID.randomUUID());
    }

    String name = params.getFirst("name");
    if (name != null && !name.isBlank()) {
      attributes.put("anonymousName", name.trim());
    }

    log.debug("Anonymous handshake accepted: anonymousId={}, organizationId={}",
        attributes.get("anonymousId"), orgIdParam);
    return true;
  }

  @Override
  public void afterHandshake(@Nonnull ServerHttpRequest request,
      @Nonnull ServerHttpResponse response,
      @Nonnull WebSocketHandler wsHandler, Exception exception) {
  }
}
