package kz.logisto.lguserservice.service.impl;

import kz.logisto.lguserservice.data.dto.organization.OrganizationFilterDto;
import kz.logisto.lguserservice.data.dto.organization.UserOrganizationDto;
import kz.logisto.lguserservice.data.dto.user.UserFilterDto;
import kz.logisto.lguserservice.data.entity.Organization;
import kz.logisto.lguserservice.data.entity.User;
import kz.logisto.lguserservice.data.entity.UserOrganization;
import kz.logisto.lguserservice.data.enums.OrganizationRole;
import kz.logisto.lguserservice.data.model.OrganizationUserModel;
import kz.logisto.lguserservice.data.model.UserOrganizationModel;
import kz.logisto.lguserservice.data.repository.UserOrganizationRepository;
import kz.logisto.lguserservice.exception.ForbiddenException;
import kz.logisto.lguserservice.exception.NotFoundException;
import kz.logisto.lguserservice.mapper.UserOrganizationMapper;
import kz.logisto.lguserservice.service.OrganizationAccessService;
import kz.logisto.lguserservice.service.UserOrganizationService;
import kz.logisto.lguserservice.service.UserService;
import jakarta.persistence.criteria.Predicate;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserOrganizationServiceImpl implements UserOrganizationService {

  private final UserService userService;
  private final UserOrganizationMapper userOrganizationMapper;
  private final OrganizationAccessService organizationAccessService;
  private final UserOrganizationRepository userOrganizationRepository;

  @Override
  public Page<UserOrganizationModel> findAllUsersByOrganizationId(UUID organizationId,
                                                                  UserFilterDto filter,
                                                                  Pageable pageable,
                                                                  Principal principal) {
    if (!organizationAccessService.isMember(principal.getName(), organizationId)) {
      throw new ForbiddenException();
    }
    Specification<UserOrganization> specification = buildSpecification(organizationId, filter);
    return userOrganizationRepository.findAll(specification, pageable)
        .map(userOrganizationMapper::toUserModel);
  }

  @Override
  public Page<OrganizationUserModel> findAllOrganizationsByUserId(OrganizationFilterDto filter,
                                                                  Pageable pageable,
                                                                  Principal principal) {
    Specification<UserOrganization> specification = buildSpecification(principal.getName(), filter);
    return userOrganizationRepository.findAll(specification, pageable)
        .map(userOrganizationMapper::toOrganizationModel);
  }

  @Override
  public void addUserToOrganization(User user, Organization organization, OrganizationRole role) {
    UserOrganization userOrganization = userOrganizationMapper.toEntity(user, organization, role);
    userOrganizationRepository.save(userOrganization);
  }

  @Override
  public UserOrganizationModel addUserToOrganization(UserOrganizationDto userOrganizationDto,
                                                     Principal principal) {
    if (userOrganizationDto.userId().equals(principal.getName())) {
      throw new ForbiddenException();
    }
    Organization organization = getOrganizationIfCanManage(
        userOrganizationDto.organizationId(), principal);
    User user = userService.getOrThrow(userOrganizationDto.userId());
    UserOrganization userOrganization = userOrganizationMapper.toEntity(user, organization,
        userOrganizationDto.role());
    return userOrganizationMapper.toUserModel(userOrganizationRepository.save(userOrganization));
  }

  @Override
  public UserOrganizationModel updateUserInOrganization(UserOrganizationDto userOrganizationDto,
                                                        Principal principal) {
    if (userOrganizationDto.userId().equals(principal.getName())) {
      throw new ForbiddenException();
    }
    Organization organization = getOrganizationIfCanManage(
        userOrganizationDto.organizationId(), principal);
    User user = userService.getOrThrow(userOrganizationDto.userId());
    UserOrganization userOrganization = getOrThrow(user.getId(), organization.getId());
    userOrganization.setRole(userOrganizationDto.role());
    return userOrganizationMapper.toUserModel(userOrganizationRepository.save(userOrganization));
  }

  @Override
  public void deleteUserFromOrganization(UserOrganizationDto userOrganizationDto,
                                         Principal principal) {
    if (userOrganizationDto.userId().equals(principal.getName())) {
      throw new ForbiddenException();
    }
    Organization organization = getOrganizationIfCanManage(
        userOrganizationDto.organizationId(), principal);
    User user = userService.getOrThrow(userOrganizationDto.userId());
    UserOrganization userOrganization = getOrThrow(user.getId(), organization.getId());
    userOrganizationRepository.delete(userOrganization);
  }

  @Override
  public Organization getOrganizationIfCanManage(UUID id, Principal principal)
      throws ForbiddenException {
    if (!organizationAccessService.canManageOrganization(principal.getName(), id)) {
      throw new ForbiddenException();
    }

    return getOrThrow(principal.getName(), id)
        .getOrganization();
  }

  @Override
  public int countByOrganizationId(UUID organizationId) {
    return userOrganizationRepository.countByOrganization_Id(organizationId);
  }

  private UserOrganization getOrThrow(String userId, UUID organizationId) throws NotFoundException {
    return userOrganizationRepository.findById(userId, organizationId)
        .orElseThrow(NotFoundException::new);
  }

  private Specification<UserOrganization> buildSpecification(String userId,
                                                             OrganizationFilterDto filter) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      predicates.add(cb.equal(root.get("user").get("id"), userId));
      predicates.add(cb.equal(root.get("organization").get("deleted"), false));

      if (filter.name() != null) {
        predicates.add(cb.like(cb.lower(root.get("organization").get("name")),
            filter.name().toLowerCase() + "%"));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  private Specification<UserOrganization> buildSpecification(UUID organizationId,
                                                             UserFilterDto filter) {
    return (root, query, cb) -> {
      List<Predicate> andPredicates = new ArrayList<>();

      andPredicates.add(cb.equal(root.get("organization").get("id"), organizationId));
      andPredicates.add(cb.equal(root.get("organization").get("deleted"), false));

      if (filter.username() != null) {
        andPredicates.add(cb.like(cb.lower(root.get("user").get("username")),
            filter.username().toLowerCase() + "%"));
      }

      if (filter.firstName() != null) {
        andPredicates.add(cb.like(cb.lower(root.get("user").get("firstName")),
            filter.firstName().toLowerCase() + "%"));
      }

      if (filter.lastName() != null) {
        andPredicates.add(cb.like(cb.lower(root.get("user").get("lastName")),
            filter.lastName().toLowerCase() + "%"));
      }

      return cb.and(andPredicates.toArray(new Predicate[0]));
    };
  }
}
