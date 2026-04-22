package kz.logisto.lguserservice.integration;

import kz.logisto.lguserservice.BaseIntegrationTest;
import kz.logisto.lguserservice.data.entity.Client;
import kz.logisto.lguserservice.data.entity.Organization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClientIntegrationTest extends BaseIntegrationTest {

  private Organization org;

  @BeforeEach
  void setUp() {
    org = createOrganization("Client Org");
  }

  @AfterEach
  void tearDown() {
    cleanDatabase();
  }

  @Test
  void createClient_persistsCorrectly() {
    Client client = createClient(org, "John", "Doe");

    assertNotNull(client.getId());
    assertEquals("John", client.getFirstName());
    assertEquals("Doe", client.getLastName());
    assertEquals(org.getId(), client.getOrganizationId());
  }

  @Test
  void findAll_byOrganization() {
    createClient(org, "John", "Doe");
    createClient(org, "Jane", "Smith");

    Organization otherOrg = createOrganization("Other Org");
    createClient(otherOrg, "Bob", "Jones");

    List<Client> orgClients = clientRepository.findAll().stream()
        .filter(c -> c.getOrganizationId().equals(org.getId()))
        .toList();

    assertEquals(2, orgClients.size());
  }

  @Test
  void updateClient() {
    Client client = createClient(org, "John", "Doe");
    client.setFirstName("Johnny");
    client.setEmail("johnny@test.com");
    clientRepository.save(client);

    Client updated = clientRepository.findById(client.getId()).orElseThrow();
    assertEquals("Johnny", updated.getFirstName());
    assertEquals("johnny@test.com", updated.getEmail());
  }

  @Test
  void deleteClient() {
    Client client = createClient(org, "John", "Doe");
    clientRepository.delete(client);

    assertTrue(clientRepository.findById(client.getId()).isEmpty());
  }

  @Test
  void countByOrganizationId() {
    createClient(org, "John", "Doe");
    createClient(org, "Jane", "Smith");

    int count = clientRepository.countByOrganization_Id(org.getId());
    assertEquals(2, count);
  }

  @Test
  void countByOrganizationId_noClients_returnsZero() {
    int count = clientRepository.countByOrganization_Id(org.getId());
    assertEquals(0, count);
  }
}
