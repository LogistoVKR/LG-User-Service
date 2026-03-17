package kz.logisto.lguserservice.config.security;

import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class BaseSecurityConfig extends AbstractSecurityConfig {

  private static final String[] PERMIT_ALL_PATHS = new String[]{
      "/actuator/**",
      "/swagger-ui/**",
      "/v3/api-docs/**",
  };

  @Bean
  public SecurityFilterChain baseFilterChain(HttpSecurity http,
                                             JwtAuthenticationConverter jwtConverter)
      throws Exception {
    super.init(http, jwtConverter);
    return http
        .securityMatcher(PERMIT_ALL_PATHS)
        .authorizeHttpRequests(authorize ->
            authorize
                .anyRequest().permitAll())
        .build();
  }

  @Bean
  public JwtAuthenticationConverter grantedAuthoritiesExtractor() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(jwt -> {
      Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
      if (resourceAccess == null) {
        return List.of();
      }

      Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get("lg-backend");
      if (clientAccess == null) {
        return List.of();
      }

      List<String> roles = (List<String>) clientAccess.get("roles");
      return roles.stream()
          .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
          .toList();
    });
    return converter;
  }
}
