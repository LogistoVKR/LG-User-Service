package kz.logisto.lguserservice.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class OrganizationSecurityConfig extends AbstractSecurityConfig {

  @Bean
  public SecurityFilterChain organizationFilterChain(HttpSecurity http,
      JwtAuthenticationConverter jwtConverter)
      throws Exception {
    super.init(http, jwtConverter);
    return http.securityMatcher("/organizations/**")
        .authorizeHttpRequests(authorize ->
            authorize
                .requestMatchers(HttpMethod.GET,
                    "/organizations/{id}/membership",
                    "/organizations/{id}/warehouse-access",
                    "/organizations/{id}/ozon-api-key")
                .hasRole("lg-backend-service")
                .anyRequest().authenticated())
        .build();
  }
}
