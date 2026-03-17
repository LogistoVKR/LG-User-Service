package kz.logisto.lguserservice.data.dto.organization;

import kz.logisto.lguserservice.data.enums.OrganizationRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UserOrganizationDto(
    @Size(max = 36) @NotBlank String userId,
    @NotNull UUID organizationId,
    @NotNull OrganizationRole role) {

  public UserOrganizationDto(String userId, UUID organizationId, OrganizationRole role) {
    if (role != null && role == OrganizationRole.OWNER) {
      throw new IllegalArgumentException("Role cannot be OWNER");
    }

    this.userId = userId;
    this.organizationId = organizationId;
    this.role = role;
  }
}
