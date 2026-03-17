package kz.logisto.lguserservice.service.impl;

import kz.logisto.lguserservice.data.constants.JwtConstants;
import kz.logisto.lguserservice.data.dto.user.UserFilterDto;
import kz.logisto.lguserservice.data.entity.User;
import kz.logisto.lguserservice.data.model.UserModel;
import kz.logisto.lguserservice.data.repository.UserRepository;
import kz.logisto.lguserservice.exception.NotFoundException;
import kz.logisto.lguserservice.mapper.UserMapper;
import kz.logisto.lguserservice.service.UserService;
import jakarta.persistence.criteria.Predicate;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserMapper userMapper;
  private final UserRepository userRepository;

  @Override
  public Page<UserModel> findAll(UserFilterDto filter, Pageable pageable) {
    Specification<User> specification = buildSpecification(filter);
    return userRepository.findAll(specification, pageable)
        .map(userMapper::toModel);
  }

  @Override
  public User getOrThrow(String userId) throws NotFoundException {
    return userRepository.findById(userId)
        .orElseThrow(NotFoundException::new);
  }

  @Override
  public User getOrThrow(Principal principal) throws NotFoundException {
    return this.getOrThrow(principal.getName());
  }

  @Override
  public UserModel login(Principal principal) {
    return userMapper.toModel(getUserFromPrincipal(principal));
  }

  private User getUserFromPrincipal(Principal principal) {
    Map<String, Object> claims = getPrincipalClaims(principal);
    if (claims == null) {
      throw new NotFoundException("User claims not found");
    }

    return userRepository.findById(principal.getName())
        .map(user -> updateUser(user, claims))
        .orElseGet(() -> createUser(principal, claims));
  }

  private Map<String, Object> getPrincipalClaims(Principal principal) {
    if (principal instanceof JwtAuthenticationToken jwt) {
      return jwt.getTokenAttributes();
    }
    return null;
  }

  private User createUser(Principal principal, Map<String, Object> claims) {
    User user = new User();
    user.setId(principal.getName());
    user.setUsername(claims.get(JwtConstants.USERNAME).toString());
    user.setEmail(claims.get(JwtConstants.EMAIL).toString());
    user.setFirstName(claims.get(JwtConstants.FIRST_NAME).toString());
    user.setLastName(claims.get(JwtConstants.LAST_NAME).toString());
    return userRepository.save(user);
  }

  private User updateUser(User user, Map<String, Object> claims) {
    String username = claims.get(JwtConstants.USERNAME).toString();
    if (!user.getUsername().equals(username)) {
      user.setUsername(username);
    }
    String email = claims.get(JwtConstants.EMAIL).toString();
    if (!user.getEmail().equals(email)) {
      user.setEmail(email);
    }
    String firstName = claims.get(JwtConstants.FIRST_NAME).toString();
    if (!user.getFirstName().equals(firstName)) {
      user.setFirstName(firstName);
    }
    String lastName = claims.get(JwtConstants.LAST_NAME).toString();
    if (!user.getLastName().equals(lastName)) {
      user.setLastName(lastName);
    }
    return userRepository.save(user);
  }

  private Specification<User> buildSpecification(UserFilterDto filter) {
    return (root, query, cb) -> {
      List<Predicate> andPredicates = new ArrayList<>();
      List<Predicate> orPredicates = new ArrayList<>();

      if (filter.username() != null) {
        if (Boolean.TRUE.equals(filter.or())) {
          orPredicates.add(
              cb.like(cb.lower(root.get("username")), filter.username().toLowerCase() + "%"));
        } else {
          andPredicates.add(
              cb.like(cb.lower(root.get("username")), filter.username().toLowerCase() + "%"));
        }
      }

      if (filter.firstName() != null) {
        if (Boolean.TRUE.equals(filter.or())) {
          orPredicates.add(
              cb.like(cb.lower(root.get("firstName")), filter.firstName().toLowerCase() + "%"));
        } else {
          andPredicates.add(
              cb.like(cb.lower(root.get("firstName")), filter.firstName().toLowerCase() + "%"));
        }
      }

      if (filter.lastName() != null) {
        if (Boolean.TRUE.equals(filter.or())) {
          orPredicates.add(
              cb.like(cb.lower(root.get("lastName")), filter.lastName().toLowerCase() + "%"));
        } else {
          andPredicates.add(
              cb.like(cb.lower(root.get("lastName")), filter.lastName().toLowerCase() + "%"));
        }
      }

      Predicate andPredicate = cb.and(andPredicates.toArray(new Predicate[0]));
      Predicate orPredicate =
          orPredicates.isEmpty() ? cb.conjunction() : cb.or(orPredicates.toArray(new Predicate[0]));

      return cb.and(andPredicate, orPredicate);
    };
  }
}
