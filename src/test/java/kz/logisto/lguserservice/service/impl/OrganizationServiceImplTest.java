package kz.logisto.lguserservice.service.impl;

import kz.logisto.lguserservice.data.dto.organization.OrganizationDto;
import kz.logisto.lguserservice.data.entity.Organization;
import kz.logisto.lguserservice.data.entity.User;
import kz.logisto.lguserservice.data.model.OrganizationModel;
import kz.logisto.lguserservice.data.repository.OrganizationRepository;
import kz.logisto.lguserservice.exception.ForbiddenException;
import kz.logisto.lguserservice.exception.NotFoundException;
import kz.logisto.lguserservice.mapper.OrganizationMapper;
import kz.logisto.lguserservice.service.OrganizationAccessService;
import kz.logisto.lguserservice.service.UserOrganizationService;
import kz.logisto.lguserservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

import static kz.logisto.lguserservice.data.enums.OrganizationRole.OWNER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceImplTest {

  @Mock
  private UserService userService;
  @Mock
  private OrganizationMapper organizationMapper;
  @Mock
  private OrganizationRepository organizationRepository;
  @Mock
  private UserOrganizationService userOrganizationService;
  @Mock
  private OrganizationAccessService organizationAccessService;

  @InjectMocks
  private OrganizationServiceImpl organizationService;

  private final Principal principal = () -> "user-1";

  @Test
  void create_setsOwnerRole() {
    OrganizationDto dto = new OrganizationDto("Test Org", "Description");
    User user = new User();
    user.setId("user-1");
    Organization org = new Organization();
    org.setId(UUID.randomUUID());
    OrganizationModel model = new OrganizationModel(org.getId(), "Test Org", "Description");

    when(userService.getOrThrow(principal)).thenReturn(user);
    when(organizationMapper.toEntity(dto)).thenReturn(org);
    when(organizationRepository.save(org)).thenReturn(org);
    when(organizationMapper.toModel(org)).thenReturn(model);

    OrganizationModel result = organizationService.create(dto, principal);

    assertEquals("Test Org", result.getName());
    verify(userOrganizationService).addUserToOrganization(user, org, OWNER);
  }

  @Test
  void update_withManageAccess_updatesOrganization() {
    UUID orgId = UUID.randomUUID();
    OrganizationDto dto = new OrganizationDto("Updated", "New desc");
    Organization org = new Organization();
    org.setId(orgId);
    OrganizationModel model = new OrganizationModel(orgId, "Updated", "New desc");

    when(userOrganizationService.getOrganizationIfCanManage(orgId, principal)).thenReturn(org);
    when(organizationRepository.save(org)).thenReturn(org);
    when(organizationMapper.toModel(org)).thenReturn(model);

    OrganizationModel result = organizationService.update(orgId, dto, principal);

    assertEquals("Updated", result.getName());
    verify(organizationMapper).updateEntity(org, dto);
  }

  @Test
  void update_withoutAccess_throwsForbidden() {
    UUID orgId = UUID.randomUUID();
    OrganizationDto dto = new OrganizationDto("Updated", "Desc");

    when(userOrganizationService.getOrganizationIfCanManage(orgId, principal))
        .thenThrow(new ForbiddenException());

    assertThrows(ForbiddenException.class,
        () -> organizationService.update(orgId, dto, principal));
  }

  @Test
  void delete_withAccess_softDeletes() {
    UUID orgId = UUID.randomUUID();
    Organization org = new Organization();
    org.setId(orgId);
    org.setDeleted(false);

    when(organizationAccessService.canManageOrganization("user-1", orgId)).thenReturn(true);
    when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
    when(organizationRepository.save(org)).thenReturn(org);

    organizationService.delete(orgId, principal);

    assertTrue(org.isDeleted());
    verify(organizationRepository).save(org);
  }

  @Test
  void delete_withoutAccess_throwsForbidden() {
    UUID orgId = UUID.randomUUID();

    when(organizationAccessService.canManageOrganization("user-1", orgId)).thenReturn(false);

    assertThrows(ForbiddenException.class,
        () -> organizationService.delete(orgId, principal));
  }

  @Test
  void delete_notFound_throwsNotFoundException() {
    UUID orgId = UUID.randomUUID();

    when(organizationAccessService.canManageOrganization("user-1", orgId)).thenReturn(true);
    when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class,
        () -> organizationService.delete(orgId, principal));
  }
}
