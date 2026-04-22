package kz.logisto.lguserservice.integration;

import kz.logisto.lguserservice.BaseIntegrationTest;
import kz.logisto.lguserservice.data.entity.Organization;
import kz.logisto.lguserservice.data.entity.User;
import kz.logisto.lguserservice.data.entity.UserOrganization;
import kz.logisto.lguserservice.data.enums.OrganizationRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class OrganizationIntegrationTest extends BaseIntegrationTest {

  @AfterEach
  void tearDown() {
    cleanDatabase();
  }

  @Test
  void createOrganization_persistsCorrectly() {
    Organization org = createOrganization("Test Org");

    assertNotNull(org.getId());
    assertEquals("Test Org", org.getName());
    assertFalse(org.isDeleted());
    assertNotNull(org.getCreated());
  }

  @Test
  void createOrganization_withOwnerRole() {
    User user = createUser("user-1", "john", "john@test.com");
    Organization org = createOrganization("Test Org");
    UserOrganization uo = addUserToOrganization(user, org, OrganizationRole.OWNER);

    assertEquals(OrganizationRole.OWNER, uo.getRole());
    assertEquals("user-1", uo.getUser().getId());
    assertEquals(org.getId(), uo.getOrganization().getId());
  }

  @Test
  void updateOrganization() {
    Organization org = createOrganization("Old Name");
    org.setName("New Name");
    org.setDescription("Updated description");
    organizationRepository.save(org);

    Organization updated = organizationRepository.findById(org.getId()).orElseThrow();
    assertEquals("New Name", updated.getName());
    assertEquals("Updated description", updated.getDescription());
  }

  @Test
  void softDeleteOrganization() {
    Organization org = createOrganization("To Delete");
    assertFalse(org.isDeleted());

    org.setDeleted(true);
    organizationRepository.save(org);

    Organization deleted = organizationRepository.findById(org.getId()).orElseThrow();
    assertTrue(deleted.isDeleted());
  }

  @Test
  void findById_nonExisting_returnsEmpty() {
    Optional<Organization> found = organizationRepository.findById(
        java.util.UUID.randomUUID());

    assertTrue(found.isEmpty());
  }
}
