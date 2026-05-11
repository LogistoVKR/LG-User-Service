package kz.logisto.lguserservice.data.model;

import kz.logisto.lguserservice.data.enums.OrganizationRole;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationUserModel {

  private UUID id;
  private String name;
  private String description;
  private boolean hasOzonIntegration;
  private String ozonClientId;
  private OrganizationRole role;
}
