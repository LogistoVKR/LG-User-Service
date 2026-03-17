package kz.logisto.lguserservice.config.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

public abstract class AbstractSecurityConfig {

  protected final void init(HttpSecurity http, JwtAuthenticationConverter jwtConverter)
      throws Exception {
    http
        .oauth2ResourceServer(
            oauth -> oauth.jwt(
                jwt -> jwt.jwtAuthenticationConverter(jwtConverter)))
        .cors(AbstractHttpConfigurer::disable)
        .csrf(AbstractHttpConfigurer::disable);
  }
}
