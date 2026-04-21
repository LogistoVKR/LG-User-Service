package kz.logisto.lguserservice.web.socket.config;

import java.time.Duration;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("application.chat.websocket")
public class ChatWebSocketProperty {

  private String anonymousPath = "/ws/chat/anonymous";
  private String employeePath = "/ws/chat/employee";
  private Integer pingRetries = 3;
  private Duration pingInterval = Duration.ofSeconds(30);
  private Integer messageBufferSize = 8192;
  private List<String> allowedOrigins = List.of("*");
  private Duration maxSessionIdleTimeout = Duration.ofMinutes(10);
  private Duration sendTimeLimit = Duration.ofSeconds(5);
}
