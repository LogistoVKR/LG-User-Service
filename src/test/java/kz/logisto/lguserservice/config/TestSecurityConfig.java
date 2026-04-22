package kz.logisto.lguserservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;

@TestConfiguration
public class TestSecurityConfig {

  @Bean
  public JwtDecoder jwtDecoder() {
    return token -> Jwt.withTokenValue(token)
        .header("alg", "RS256")
        .subject("test-user")
        .claim("preferred_username", "testuser")
        .claim("email", "test@test.com")
        .claim("given_name", "Test")
        .claim("family_name", "User")
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(3600))
        .build();
  }
}
