package kz.logisto.lguserservice.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSocketSecurityConfig {

  @Bean
  @Order(0)
  public SecurityFilterChain webSocketFilterChain(HttpSecurity http) throws Exception {
    return http
        .securityMatcher("/ws/**")
        .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
        .cors(AbstractHttpConfigurer::disable)
        .csrf(AbstractHttpConfigurer::disable)
        .build();
  }
}
