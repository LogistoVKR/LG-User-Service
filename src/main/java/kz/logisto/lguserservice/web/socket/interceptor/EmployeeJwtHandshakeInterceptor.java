package kz.logisto.lguserservice.web.socket.interceptor;

import jakarta.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import kz.logisto.lguserservice.service.OrganizationAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeJwtHandshakeInterceptor implements HandshakeInterceptor {

  private final JwtDecoder jwtDecoder;
  private final OrganizationAccessService organizationAccessService;

  @Override
  public boolean beforeHandshake(ServerHttpRequest request, @Nonnull ServerHttpResponse response,
      @Nonnull WebSocketHandler wsHandler, @Nonnull Map<String, Object> attributes) {
    var params = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();

    String token = params.getFirst("token");
    String orgIdParam = params.getFirst("organizationId");

    if (token == null || orgIdParam == null) {
      log.warn("Employee handshake rejected: missing token or organizationId");
      return false;
    }

    Jwt jwt;
    try {
      jwt = jwtDecoder.decode(token);
    } catch (JwtException e) {
      log.warn("Employee handshake rejected: invalid JWT - {}", e.getMessage());
      return false;
    }

    String userId = jwt.getSubject();
    UUID organizationId;
    try {
      organizationId = UUID.fromString(orgIdParam);
    } catch (IllegalArgumentException e) {
      log.warn("Employee handshake rejected: invalid organizationId format");
      return false;
    }

    if (!organizationAccessService.isMember(userId, organizationId)) {
      log.warn("Employee handshake rejected: user {} is not member of org {}",
          userId, organizationId);
      return false;
    }

    attributes.put("userId", userId);
    attributes.put("organizationId", organizationId);

    String employeeName = jwt.getClaimAsString("given_name");
    if (employeeName != null) {
      String lastName = jwt.getClaimAsString("family_name");
      if (lastName != null) {
        employeeName = employeeName + " " + lastName;
      }
      attributes.put("employeeName", employeeName);
    }

    log.debug("Employee handshake accepted: userId={}, organizationId={}", userId, organizationId);
    return true;
  }

  @Override
  public void afterHandshake(@Nonnull ServerHttpRequest request,
      @Nonnull ServerHttpResponse response,
      @Nonnull WebSocketHandler wsHandler, Exception exception) {
  }
}
