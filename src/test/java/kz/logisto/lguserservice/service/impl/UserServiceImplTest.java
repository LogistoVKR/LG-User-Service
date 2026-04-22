package kz.logisto.lguserservice.service.impl;

import kz.logisto.lguserservice.data.constants.JwtConstants;
import kz.logisto.lguserservice.data.dto.user.UserFilterDto;
import kz.logisto.lguserservice.data.entity.User;
import kz.logisto.lguserservice.data.model.UserModel;
import kz.logisto.lguserservice.data.repository.UserRepository;
import kz.logisto.lguserservice.exception.NotFoundException;
import kz.logisto.lguserservice.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock
  private UserMapper userMapper;
  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserServiceImpl userService;

  @Test
  void login_newUser_createsAndReturns() {
    JwtAuthenticationToken principal = createJwtPrincipal("user-1", "john", "john@test.com",
        "John", "Doe");

    when(userRepository.findById("user-1")).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    UserModel expectedModel = new UserModel("user-1", "john@test.com", "john", "John", "Doe",
        LocalDateTime.now());
    when(userMapper.toModel(any(User.class))).thenReturn(expectedModel);

    UserModel result = userService.login(principal);

    assertEquals(expectedModel, result);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    User savedUser = captor.getValue();
    assertEquals("user-1", savedUser.getId());
    assertEquals("john", savedUser.getUsername());
    assertEquals("john@test.com", savedUser.getEmail());
    assertEquals("John", savedUser.getFirstName());
    assertEquals("Doe", savedUser.getLastName());
  }

  @Test
  void login_existingUser_updatesChangedFields() {
    JwtAuthenticationToken principal = createJwtPrincipal("user-1", "john_new", "john_new@test.com",
        "Johnny", "Doer");

    User existingUser = new User("user-1", "john@test.com", "john", "John", "Doe",
        LocalDateTime.now());
    when(userRepository.findById("user-1")).thenReturn(Optional.of(existingUser));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    UserModel expectedModel = new UserModel();
    when(userMapper.toModel(any(User.class))).thenReturn(expectedModel);

    userService.login(principal);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    User saved = captor.getValue();
    assertEquals("john_new", saved.getUsername());
    assertEquals("john_new@test.com", saved.getEmail());
    assertEquals("Johnny", saved.getFirstName());
    assertEquals("Doer", saved.getLastName());
  }

  @Test
  void login_existingUser_noChanges_stillSaves() {
    JwtAuthenticationToken principal = createJwtPrincipal("user-1", "john", "john@test.com",
        "John", "Doe");

    User existingUser = new User("user-1", "john@test.com", "john", "John", "Doe",
        LocalDateTime.now());
    when(userRepository.findById("user-1")).thenReturn(Optional.of(existingUser));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    when(userMapper.toModel(any(User.class))).thenReturn(new UserModel());

    userService.login(principal);

    verify(userRepository).save(existingUser);
  }

  @Test
  void login_notJwtPrincipal_throwsNotFoundException() {
    Principal principal = () -> "user-1";

    assertThrows(NotFoundException.class, () -> userService.login(principal));
  }

  @Test
  void getOrThrow_existingUser_returns() {
    User user = new User();
    user.setId("user-1");
    when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

    User result = userService.getOrThrow("user-1");

    assertEquals("user-1", result.getId());
  }

  @Test
  void getOrThrow_nonExisting_throwsNotFoundException() {
    when(userRepository.findById("unknown")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> userService.getOrThrow("unknown"));
  }

  @Test
  @SuppressWarnings("unchecked")
  void findAll_delegatesToRepository() {
    UserFilterDto filter = new UserFilterDto("john", null, null, null);
    Pageable pageable = PageRequest.of(0, 10);

    Page<User> userPage = new PageImpl<>(List.of(new User()));
    when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
    when(userMapper.toModel(any(User.class))).thenReturn(new UserModel());

    Page<UserModel> result = userService.findAll(filter, pageable);

    assertEquals(1, result.getTotalElements());
    verify(userRepository).findAll(any(Specification.class), eq(pageable));
  }

  private JwtAuthenticationToken createJwtPrincipal(String userId, String username, String email,
      String firstName, String lastName) {
    Jwt jwt = Jwt.withTokenValue("token")
        .header("alg", "RS256")
        .subject(userId)
        .claim(JwtConstants.USERNAME, username)
        .claim(JwtConstants.EMAIL, email)
        .claim(JwtConstants.FIRST_NAME, firstName)
        .claim(JwtConstants.LAST_NAME, lastName)
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(3600))
        .build();
    return new JwtAuthenticationToken(jwt);
  }
}
