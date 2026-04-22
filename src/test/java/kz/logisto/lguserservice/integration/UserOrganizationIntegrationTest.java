package kz.logisto.lguserservice.integration;

import kz.logisto.lguserservice.BaseIntegrationTest;
import kz.logisto.lguserservice.data.entity.Organization;
import kz.logisto.lguserservice.data.entity.User;
import kz.logisto.lguserservice.data.entity.UserOrganization;
import kz.logisto.lguserservice.data.enums.OrganizationRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserOrganizationIntegrationTest extends BaseIntegrationTest {

  private User owner;
  private User member;
  private Organization org;

  @BeforeEach
  void setUp() {
    owner = createUser("owner-1", "owner", "owner@test.com");
    member = createUser("member-1", "member", "member@test.com");
    org = createOrganization("Test Org");
    addUserToOrganization(owner, org, OrganizationRole.OWNER);
    addUserToOrganization(member, org, OrganizationRole.MEMBER);
  }

  @AfterEach
  void tearDown() {
    cleanDatabase();
  }

  @Test
  void findById_compositeKey() {
    Optional<UserOrganization> found = userOrganizationRepository.findById(
        owner.getId(), org.getId());

    assertTrue(found.isPresent());
    assertEquals(OrganizationRole.OWNER, found.get().getRole());
  }

  @Test
  void findById_notFound() {
    Optional<UserOrganization> found = userOrganizationRepository.findById(
        "non-existing", org.getId());

    assertTrue(found.isEmpty());
  }

  @Test
  void existsById_exists() {
    boolean exists = userOrganizationRepository.existsById(owner.getId(), org.getId());
    assertTrue(exists);
  }

  @Test
  void existsById_notExists() {
    boolean exists = userOrganizationRepository.existsById("non-existing", org.getId());
    assertFalse(exists);
  }

  @Test
  void hasAnyRole_ownerHasOwnerRole() {
    boolean result = userOrganizationRepository.hasAnyRole(
        owner.getId(), org.getId(), OrganizationRole.ADMIN, OrganizationRole.OWNER);
    assertTrue(result);
  }

  @Test
  void hasAnyRole_memberDoesNotHaveAdminOrOwner() {
    boolean result = userOrganizationRepository.hasAnyRole(
        member.getId(), org.getId(), OrganizationRole.ADMIN, OrganizationRole.OWNER);
    assertFalse(result);
  }

  @Test
  void hasAnyRole_memberHasWarehouseManagerRoles_false() {
    boolean result = userOrganizationRepository.hasAnyRole(
        member.getId(), org.getId(),
        OrganizationRole.WAREHOUSE_MANAGER, OrganizationRole.ADMIN, OrganizationRole.OWNER);
    assertFalse(result);
  }

  @Test
  void countByOrganizationId() {
    int count = userOrganizationRepository.countByOrganization_Id(org.getId());
    assertEquals(2, count);
  }

  @Test
  void updateRole() {
    UserOrganization uo = userOrganizationRepository.findById(member.getId(), org.getId())
        .orElseThrow();
    uo.setRole(OrganizationRole.ADMIN);
    userOrganizationRepository.save(uo);

    UserOrganization updated = userOrganizationRepository.findById(member.getId(), org.getId())
        .orElseThrow();
    assertEquals(OrganizationRole.ADMIN, updated.getRole());
  }

  @Test
  void deleteUserFromOrganization() {
    UserOrganization uo = userOrganizationRepository.findById(member.getId(), org.getId())
        .orElseThrow();
    userOrganizationRepository.delete(uo);

    assertFalse(userOrganizationRepository.existsById(member.getId(), org.getId()));
    assertEquals(1, userOrganizationRepository.countByOrganization_Id(org.getId()));
  }
}
