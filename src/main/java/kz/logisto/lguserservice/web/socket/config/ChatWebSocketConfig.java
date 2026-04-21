package kz.logisto.lguserservice.web.socket.config;

import kz.logisto.lguserservice.web.socket.handler.AnonymousChatHandler;
import kz.logisto.lguserservice.web.socket.handler.EmployeeChatHandler;
import kz.logisto.lguserservice.web.socket.interceptor.AnonymousHandshakeInterceptor;
import kz.logisto.lguserservice.web.socket.interceptor.EmployeeJwtHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.web.socket.server.standard.TomcatRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class ChatWebSocketConfig implements WebSocketConfigurer {

  private final ChatWebSocketProperty property;
  private final AnonymousChatHandler anonymousChatHandler;
  private final EmployeeChatHandler employeeChatHandler;
  private final AnonymousHandshakeInterceptor anonymousInterceptor;
  private final EmployeeJwtHandshakeInterceptor employeeInterceptor;

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    String[] origins = property.getAllowedOrigins().toArray(new String[0]);

    registry.addHandler(anonymousChatHandler, property.getAnonymousPath())
        .setHandshakeHandler(handshakeHandler())
        .addInterceptors(anonymousInterceptor)
        .setAllowedOrigins(origins);

    registry.addHandler(employeeChatHandler, property.getEmployeePath())
        .setHandshakeHandler(handshakeHandler())
        .addInterceptors(employeeInterceptor)
        .setAllowedOrigins(origins);
  }

  @Bean
  public ServletServerContainerFactoryBean servletServerContainerFactoryBean() {
    ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
    container.setMaxSessionIdleTimeout(property.getMaxSessionIdleTimeout().toMillis());
    container.setMaxTextMessageBufferSize(property.getMessageBufferSize());
    container.setMaxBinaryMessageBufferSize(property.getMessageBufferSize());
    return container;
  }

  private DefaultHandshakeHandler handshakeHandler() {
    return new DefaultHandshakeHandler(new TomcatRequestUpgradeStrategy());
  }
}
