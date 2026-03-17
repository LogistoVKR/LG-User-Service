package kz.logisto.lguserservice.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Transient;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "clients")
public class Client {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  private String firstName;
  private String middleName;
  private String lastName;
  private LocalDate dateOfBirth;
  private String email;
  private String phoneNumber;

  @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.DETACH})
  @JoinColumn(name = "organization_id")
  private Organization organization;

  @Transient
  @JsonIgnore
  public UUID getOrganizationId() {
    if (organization == null) {
      return null;
    }
    return organization.getId();
  }
}
