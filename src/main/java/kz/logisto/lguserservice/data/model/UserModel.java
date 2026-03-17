package kz.logisto.lguserservice.data.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserModel {

  private String id;
  private String email;
  private String username;
  private String firstName;
  private String lastName;
  private LocalDateTime created;
}
