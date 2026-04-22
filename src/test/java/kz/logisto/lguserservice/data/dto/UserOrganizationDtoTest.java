package kz.logisto.lguserservice.data.dto;

import kz.logisto.lguserservice.data.dto.organization.UserOrganizationDto;
import kz.logisto.lguserservice.data.enums.OrganizationRole;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserOrganizationDtoTest {

  private static final UUID ORG_ID = UUID.randomUUID();

  @Test
  void constructor_ownerRole_throwsIllegalArgument() {
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> new UserOrganizationDto("user-1", ORG_ID, OrganizationRole.OWNER));
    assertEquals("Role cannot be OWNER", ex.getMessage());
  }

  @Test
  void constructor_adminRole_createsSuccessfully() {
    UserOrganizationDto dto = new UserOrganizationDto("user-1", ORG_ID, OrganizationRole.ADMIN);

    assertEquals("user-1", dto.userId());
    assertEquals(ORG_ID, dto.organizationId());
    assertEquals(OrganizationRole.ADMIN, dto.role());
  }

  @Test
  void constructor_memberRole_createsSuccessfully() {
    UserOrganizationDto dto = new UserOrganizationDto("user-1", ORG_ID, OrganizationRole.MEMBER);

    assertEquals(OrganizationRole.MEMBER, dto.role());
  }

  @Test
  void constructor_warehouseManagerRole_createsSuccessfully() {
    UserOrganizationDto dto = new UserOrganizationDto("user-1", ORG_ID,
        OrganizationRole.WAREHOUSE_MANAGER);

    assertEquals(OrganizationRole.WAREHOUSE_MANAGER, dto.role());
  }
}
