package kz.logisto.lguserservice.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class UserSecurityConfig extends AbstractSecurityConfig {

  @Bean
  public SecurityFilterChain userFilterChain(HttpSecurity http,
                                             JwtAuthenticationConverter jwtConverter)
      throws Exception {
    super.init(http, jwtConverter);
    return http.securityMatcher("/users/**")
        .authorizeHttpRequests(authorize ->
            authorize
                .anyRequest().authenticated())
        .build();
  }
}
