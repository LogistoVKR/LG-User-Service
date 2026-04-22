package kz.logisto.lguserservice.service.impl;

import kz.logisto.lguserservice.data.dto.organization.UserOrganizationDto;
import kz.logisto.lguserservice.data.entity.Organization;
import kz.logisto.lguserservice.data.entity.User;
import kz.logisto.lguserservice.data.entity.UserOrganization;
import kz.logisto.lguserservice.data.entity.key.UserOrganizationId;
import kz.logisto.lguserservice.data.enums.OrganizationRole;
import kz.logisto.lguserservice.data.model.UserOrganizationModel;
import kz.logisto.lguserservice.data.repository.UserOrganizationRepository;
import kz.logisto.lguserservice.exception.ForbiddenException;
import kz.logisto.lguserservice.exception.NotFoundException;
import kz.logisto.lguserservice.mapper.UserOrganizationMapper;
import kz.logisto.lguserservice.service.OrganizationAccessService;
import kz.logisto.lguserservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserOrganizationServiceImplTest {

  @Mock
  private UserService userService;
  @Mock
  private UserOrganizationMapper userOrganizationMapper;
  @Mock
  private OrganizationAccessService organizationAccessService;
  @Mock
  private UserOrganizationRepository userOrganizationRepository;

  @InjectMocks
  private UserOrganizationServiceImpl userOrganizationService;

  private final Principal principal = () -> "admin-1";

  @Test
  void addUser_success() {
    UUID orgId = UUID.randomUUID();
    UserOrganizationDto dto = new UserOrganizationDto("user-2", orgId, OrganizationRole.MEMBER);

    Organization org = new Organization();
    org.setId(orgId);
    User user = new User();
    user.setId("user-2");
    UserOrganization uo = new UserOrganization();
    UserOrganizationModel model = new UserOrganizationModel();

    when(organizationAccessService.canManageOrganization("admin-1", orgId)).thenReturn(true);
    when(userOrganizationRepository.findById("admin-1", orgId))
        .thenReturn(Optional.of(createUo("admin-1", org)));
    when(userService.getOrThrow("user-2")).thenReturn(user);
    when(userOrganizationMapper.toEntity(user, org, OrganizationRole.MEMBER)).thenReturn(uo);
    when(userOrganizationRepository.save(uo)).thenReturn(uo);
    when(userOrganizationMapper.toUserModel(uo)).thenReturn(model);

    UserOrganizationModel result = userOrganizationService.addUserToOrganization(dto, principal);

    assertEquals(model, result);
  }

  @Test
  void addUser_selfAssignment_throwsForbidden() {
    UUID orgId = UUID.randomUUID();
    UserOrganizationDto dto = new UserOrganizationDto("admin-1", orgId, OrganizationRole.MEMBER);

    assertThrows(ForbiddenException.class,
        () -> userOrganizationService.addUserToOrganization(dto, principal));
  }

  @Test
  void addUser_noManageAccess_throwsForbidden() {
    UUID orgId = UUID.randomUUID();
    UserOrganizationDto dto = new UserOrganizationDto("user-2", orgId, OrganizationRole.MEMBER);

    when(organizationAccessService.canManageOrganization("admin-1", orgId)).thenReturn(false);

    assertThrows(ForbiddenException.class,
        () -> userOrganizationService.addUserToOrganization(dto, principal));
  }

  @Test
  void updateUser_success() {
    UUID orgId = UUID.randomUUID();
    UserOrganizationDto dto = new UserOrganizationDto("user-2", orgId, OrganizationRole.ADMIN);

    Organization org = new Organization();
    org.setId(orgId);
    User user = new User();
    user.setId("user-2");
    UserOrganization uo = createUo("user-2", org);
    UserOrganizationModel model = new UserOrganizationModel();

    when(organizationAccessService.canManageOrganization("admin-1", orgId)).thenReturn(true);
    when(userOrganizationRepository.findById("admin-1", orgId))
        .thenReturn(Optional.of(createUo("admin-1", org)));
    when(userService.getOrThrow("user-2")).thenReturn(user);
    when(userOrganizationRepository.findById("user-2", orgId)).thenReturn(Optional.of(uo));
    when(userOrganizationRepository.save(uo)).thenReturn(uo);
    when(userOrganizationMapper.toUserModel(uo)).thenReturn(model);

    UserOrganizationModel result = userOrganizationService.updateUserInOrganization(dto, principal);

    assertEquals(model, result);
    assertEquals(OrganizationRole.ADMIN, uo.getRole());
  }

  @Test
  void updateUser_selfUpdate_throwsForbidden() {
    UUID orgId = UUID.randomUUID();
    UserOrganizationDto dto = new UserOrganizationDto("admin-1", orgId, OrganizationRole.ADMIN);

    assertThrows(ForbiddenException.class,
        () -> userOrganizationService.updateUserInOrganization(dto, principal));
  }

  @Test
  void deleteUser_success() {
    UUID orgId = UUID.randomUUID();
    UserOrganizationDto dto = new UserOrganizationDto("user-2", orgId, OrganizationRole.MEMBER);

    Organization org = new Organization();
    org.setId(orgId);
    User user = new User();
    user.setId("user-2");
    UserOrganization uo = createUo("user-2", org);

    when(organizationAccessService.canManageOrganization("admin-1", orgId)).thenReturn(true);
    when(userOrganizationRepository.findById("admin-1", orgId))
        .thenReturn(Optional.of(createUo("admin-1", org)));
    when(userService.getOrThrow("user-2")).thenReturn(user);
    when(userOrganizationRepository.findById("user-2", orgId)).thenReturn(Optional.of(uo));

    userOrganizationService.deleteUserFromOrganization(dto, principal);

    verify(userOrganizationRepository).delete(uo);
  }

  @Test
  void deleteUser_selfDelete_throwsForbidden() {
    UUID orgId = UUID.randomUUID();
    UserOrganizationDto dto = new UserOrganizationDto("admin-1", orgId, OrganizationRole.MEMBER);

    assertThrows(ForbiddenException.class,
        () -> userOrganizationService.deleteUserFromOrganization(dto, principal));
  }

  @Test
  void findAllUsers_notMember_throwsForbidden() {
    UUID orgId = UUID.randomUUID();

    when(organizationAccessService.isMember("admin-1", orgId)).thenReturn(false);

    assertThrows(ForbiddenException.class,
        () -> userOrganizationService.findAllUsersByOrganizationId(
            orgId, new kz.logisto.lguserservice.data.dto.user.UserFilterDto(null, null, null, null),
            org.springframework.data.domain.PageRequest.of(0, 10), principal));
  }

  @Test
  void getOrganizationIfCanManage_noAccess_throwsForbidden() {
    UUID orgId = UUID.randomUUID();

    when(organizationAccessService.canManageOrganization("admin-1", orgId)).thenReturn(false);

    assertThrows(ForbiddenException.class,
        () -> userOrganizationService.getOrganizationIfCanManage(orgId, principal));
  }

  private UserOrganization createUo(String userId, Organization org) {
    UserOrganization uo = new UserOrganization();
    uo.setId(new UserOrganizationId(userId, org.getId()));
    uo.setOrganization(org);
    User user = new User();
    user.setId(userId);
    uo.setUser(user);
    uo.setRole(OrganizationRole.OWNER);
    return uo;
  }
}
