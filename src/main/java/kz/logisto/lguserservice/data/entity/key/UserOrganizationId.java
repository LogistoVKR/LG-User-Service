package kz.logisto.lguserservice.data.entity.key;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class UserOrganizationId implements Serializable {

  private String userId;
  private UUID organizationId;

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserOrganizationId that = (UserOrganizationId) o;
    return Objects.equals(userId, that.userId) && Objects.equals(organizationId,
        that.organizationId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, organizationId);
  }
}
