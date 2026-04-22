package kz.logisto.lguserservice;

import kz.logisto.lguserservice.config.TestSecurityConfig;
import kz.logisto.lguserservice.data.entity.Client;
import kz.logisto.lguserservice.data.entity.Organization;
import kz.logisto.lguserservice.data.entity.User;
import kz.logisto.lguserservice.data.entity.UserOrganization;
import kz.logisto.lguserservice.data.entity.key.UserOrganizationId;
import kz.logisto.lguserservice.data.enums.OrganizationRole;
import kz.logisto.lguserservice.data.repository.ClientRepository;
import kz.logisto.lguserservice.data.repository.OrganizationRepository;
import kz.logisto.lguserservice.data.repository.UserOrganizationRepository;
import kz.logisto.lguserservice.data.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

@Testcontainers
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest {

  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("test")
          .withUsername("test")
          .withPassword("test");

  static {
    POSTGRES.start();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @Autowired
  protected UserRepository userRepository;
  @Autowired
  protected OrganizationRepository organizationRepository;
  @Autowired
  protected UserOrganizationRepository userOrganizationRepository;
  @Autowired
  protected ClientRepository clientRepository;

  protected User createUser(String id, String username, String email) {
    User user = new User();
    user.setId(id);
    user.setUsername(username);
    user.setEmail(email);
    user.setFirstName("First_" + id);
    user.setLastName("Last_" + id);
    return userRepository.save(user);
  }

  protected Organization createOrganization(String name) {
    Organization org = new Organization();
    org.setName(name);
    org.setDescription("Description of " + name);
    org.setDeleted(false);
    return organizationRepository.save(org);
  }

  protected UserOrganization addUserToOrganization(User user, Organization org,
      OrganizationRole role) {
    UserOrganization uo = new UserOrganization();
    uo.setId(new UserOrganizationId(user.getId(), org.getId()));
    uo.setUser(user);
    uo.setOrganization(org);
    uo.setRole(role);
    return userOrganizationRepository.save(uo);
  }

  protected Client createClient(Organization org, String firstName, String lastName) {
    Client client = new Client();
    client.setFirstName(firstName);
    client.setLastName(lastName);
    client.setEmail(firstName.toLowerCase() + "@test.com");
    client.setPhoneNumber("+77001234567");
    client.setDateOfBirth(LocalDate.of(1990, 1, 1));
    client.setOrganization(org);
    return clientRepository.save(client);
  }

  protected void cleanDatabase() {
    userOrganizationRepository.deleteAll();
    clientRepository.deleteAll();
    organizationRepository.deleteAll();
    userRepository.deleteAll();
  }
}
