package kz.logisto.lguserservice.data.entity;

import kz.logisto.lguserservice.data.entity.key.UserOrganizationId;
import kz.logisto.lguserservice.data.enums.OrganizationRole;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_organization")
public class UserOrganization {

  @EmbeddedId
  private UserOrganizationId id;

  @Enumerated(EnumType.STRING)
  @JdbcType(PostgreSQLEnumJdbcType.class)
  private OrganizationRole role;

  @CreationTimestamp
  private LocalDateTime created;

  @MapsId("userId")
  @JoinColumn(name = "user_id")
  @Fetch(FetchMode.JOIN)
  @ManyToOne(fetch = FetchType.EAGER)
  private User user;

  @MapsId("organizationId")
  @JoinColumn(name = "organization_id")
  @Fetch(FetchMode.JOIN)
  @ManyToOne(fetch = FetchType.EAGER)
  private Organization organization;

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserOrganization that = (UserOrganization) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
