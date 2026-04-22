package kz.logisto.lguserservice.integration;

import kz.logisto.lguserservice.BaseIntegrationTest;
import kz.logisto.lguserservice.data.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryIntegrationTest extends BaseIntegrationTest {

  @AfterEach
  void tearDown() {
    cleanDatabase();
  }

  @Test
  void save_and_findById() {
    User user = createUser("user-1", "john", "john@test.com");

    Optional<User> found = userRepository.findById("user-1");

    assertTrue(found.isPresent());
    assertEquals("john", found.get().getUsername());
    assertEquals("john@test.com", found.get().getEmail());
    assertEquals("First_user-1", found.get().getFirstName());
    assertNotNull(found.get().getCreated());
  }

  @Test
  void findById_nonExisting_returnsEmpty() {
    Optional<User> found = userRepository.findById("non-existing");

    assertTrue(found.isEmpty());
  }

  @Test
  void save_multipleUsers_findAll() {
    createUser("user-1", "john", "john@test.com");
    createUser("user-2", "jane", "jane@test.com");
    createUser("user-3", "bob", "bob@test.com");

    assertEquals(3, userRepository.findAll().size());
  }

  @Test
  void update_existingUser() {
    User user = createUser("user-1", "john", "john@test.com");
    user.setUsername("john_updated");
    user.setEmail("john_updated@test.com");
    userRepository.save(user);

    User updated = userRepository.findById("user-1").orElseThrow();
    assertEquals("john_updated", updated.getUsername());
    assertEquals("john_updated@test.com", updated.getEmail());
  }
}
