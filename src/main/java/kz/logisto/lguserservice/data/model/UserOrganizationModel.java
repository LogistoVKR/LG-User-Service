package kz.logisto.lguserservice.data.model;

import kz.logisto.lguserservice.data.enums.OrganizationRole;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserOrganizationModel {

  private String id;
  private String email;
  private String username;
  private String firstName;
  private String lastName;
  private OrganizationRole role;
  private LocalDateTime created;
}
