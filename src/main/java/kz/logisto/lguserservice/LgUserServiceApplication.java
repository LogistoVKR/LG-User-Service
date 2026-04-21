package kz.logisto.lguserservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LgUserServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(LgUserServiceApplication.class, args);
  }
}
