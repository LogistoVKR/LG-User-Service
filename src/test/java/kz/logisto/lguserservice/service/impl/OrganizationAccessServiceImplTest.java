package kz.logisto.lguserservice.service.impl;

import kz.logisto.lguserservice.data.enums.OrganizationRole;
import kz.logisto.lguserservice.data.repository.UserOrganizationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationAccessServiceImplTest {

  @Mock
  private UserOrganizationRepository repository;

  @InjectMocks
  private OrganizationAccessServiceImpl accessService;

  private static final String USER_ID = "user-1";
  private static final UUID ORG_ID = UUID.randomUUID();

  @Test
  void canManageWarehouse_withAllowedRoles_returnsTrue() {
    OrganizationRole[] warehouseRoles = {OrganizationRole.WAREHOUSE_MANAGER,
        OrganizationRole.ADMIN, OrganizationRole.OWNER};
    when(repository.hasAnyRole(USER_ID, ORG_ID, warehouseRoles)).thenReturn(true);

    assertTrue(accessService.canManageWarehouse(USER_ID, ORG_ID));
  }

  @Test
  void canManageWarehouse_withMemberOnly_returnsFalse() {
    OrganizationRole[] warehouseRoles = {OrganizationRole.WAREHOUSE_MANAGER,
        OrganizationRole.ADMIN, OrganizationRole.OWNER};
    when(repository.hasAnyRole(USER_ID, ORG_ID, warehouseRoles)).thenReturn(false);

    assertFalse(accessService.canManageWarehouse(USER_ID, ORG_ID));
  }

  @Test
  void canManageOrganization_withOwnerOrAdmin_returnsTrue() {
    OrganizationRole[] manageRoles = {OrganizationRole.ADMIN, OrganizationRole.OWNER};
    when(repository.hasAnyRole(USER_ID, ORG_ID, manageRoles)).thenReturn(true);

    assertTrue(accessService.canManageOrganization(USER_ID, ORG_ID));
  }

  @Test
  void canManageOrganization_withWarehouseManager_returnsFalse() {
    OrganizationRole[] manageRoles = {OrganizationRole.ADMIN, OrganizationRole.OWNER};
    when(repository.hasAnyRole(USER_ID, ORG_ID, manageRoles)).thenReturn(false);

    assertFalse(accessService.canManageOrganization(USER_ID, ORG_ID));
  }

  @Test
  void isMember_exists_returnsTrue() {
    when(repository.existsById(USER_ID, ORG_ID)).thenReturn(true);

    assertTrue(accessService.isMember(USER_ID, ORG_ID));
  }

  @Test
  void isMember_notExists_returnsFalse() {
    when(repository.existsById(USER_ID, ORG_ID)).thenReturn(false);

    assertFalse(accessService.isMember(USER_ID, ORG_ID));
  }
}
