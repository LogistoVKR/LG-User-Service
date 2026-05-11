package kz.logisto.lguserservice.service.impl;

import kz.logisto.lguserservice.data.dto.organization.OrganizationDto;
import kz.logisto.lguserservice.data.entity.Organization;
import kz.logisto.lguserservice.data.entity.User;
import kz.logisto.lguserservice.data.model.OrganizationModel;
import kz.logisto.lguserservice.data.model.OzonApiKeyModel;
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
    OrganizationDto dto = new OrganizationDto("Test Org", "Description", "ozon-key", "client-1");
    User user = new User();
    user.setId("user-1");
    Organization org = new Organization();
    org.setId(UUID.randomUUID());
    OrganizationModel model = new OrganizationModel(org.getId(), "Test Org", "Description", true,
        "client-1");

    when(userService.getOrThrow(principal)).thenReturn(user);
    when(organizationMapper.toEntity(dto)).thenReturn(org);
    when(organizationRepository.save(org)).thenReturn(org);
    when(organizationMapper.toModel(org)).thenReturn(model);

    OrganizationModel result = organizationService.create(dto, principal);

    assertEquals("Test Org", result.getName());
    assertTrue(result.isHasOzonIntegration());
    assertEquals("client-1", result.getOzonClientId());
    verify(userOrganizationService).addUserToOrganization(user, org, OWNER);
  }

  @Test
  void update_withManageAccess_updatesOrganization() {
    UUID orgId = UUID.randomUUID();
    OrganizationDto dto = new OrganizationDto("Updated", "New desc", null, null);
    Organization org = new Organization();
    org.setId(orgId);
    OrganizationModel model = new OrganizationModel(orgId, "Updated", "New desc", false, null);

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
    OrganizationDto dto = new OrganizationDto("Updated", "Desc", null, null);

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

  @Test
  void getOzonApiKey_existing_returnsModel() {
    UUID orgId = UUID.randomUUID();
    Organization org = new Organization();
    org.setId(orgId);
    org.setOzonApiKey("ozon-key");
    org.setOzonClientId("client-1");
    OzonApiKeyModel model = new OzonApiKeyModel("ozon-key", "client-1", true);

    when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
    when(organizationMapper.toOzonApiKeyModel(org)).thenReturn(model);

    OzonApiKeyModel result = organizationService.getOzonApiKey(orgId);

    assertEquals("ozon-key", result.getOzonApiKey());
    assertEquals("client-1", result.getOzonClientId());
    assertTrue(result.isHasIntegration());
  }

  @Test
  void getOzonApiKey_missingOrg_throwsNotFound() {
    UUID orgId = UUID.randomUUID();

    when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> organizationService.getOzonApiKey(orgId));
  }

  @Test
  void getOzonApiKey_orgDeleted_throwsNotFound() {
    UUID orgId = UUID.randomUUID();
    Organization org = new Organization();
    org.setId(orgId);
    org.setOzonApiKey("ozon-key");
    org.setDeleted(true);

    when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));

    assertThrows(NotFoundException.class, () -> organizationService.getOzonApiKey(orgId));
  }

  @Test
  void getOzonApiKey_keyIsNull_returnsModelWithoutIntegration() {
    UUID orgId = UUID.randomUUID();
    Organization org = new Organization();
    org.setId(orgId);
    org.setOzonApiKey(null);
    OzonApiKeyModel model = new OzonApiKeyModel(null, null, false);

    when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
    when(organizationMapper.toOzonApiKeyModel(org)).thenReturn(model);

    OzonApiKeyModel result = organizationService.getOzonApiKey(orgId);

    assertFalse(result.isHasIntegration());
    assertNull(result.getOzonApiKey());
  }

  @Test
  void getOzonApiKey_keyIsBlank_returnsModelWithoutIntegration() {
    UUID orgId = UUID.randomUUID();
    Organization org = new Organization();
    org.setId(orgId);
    org.setOzonApiKey("   ");
    OzonApiKeyModel model = new OzonApiKeyModel("   ", null, false);

    when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
    when(organizationMapper.toOzonApiKeyModel(org)).thenReturn(model);

    OzonApiKeyModel result = organizationService.getOzonApiKey(orgId);

    assertFalse(result.isHasIntegration());
  }

  @Test
  void deleteOzonApiKey_withManageAccess_clearsKey() {
    UUID orgId = UUID.randomUUID();
    Organization org = new Organization();
    org.setId(orgId);
    org.setOzonApiKey("ozon-key");
    org.setOzonClientId("client-1");

    when(userOrganizationService.getOrganizationIfCanManage(orgId, principal)).thenReturn(org);
    when(organizationRepository.save(org)).thenReturn(org);

    organizationService.deleteOzonApiKey(orgId, principal);

    assertNull(org.getOzonApiKey());
    assertNull(org.getOzonClientId());
    verify(organizationRepository).save(org);
  }

  @Test
  void deleteOzonApiKey_withoutAccess_throwsForbidden() {
    UUID orgId = UUID.randomUUID();

    when(userOrganizationService.getOrganizationIfCanManage(orgId, principal))
        .thenThrow(new ForbiddenException());

    assertThrows(ForbiddenException.class,
        () -> organizationService.deleteOzonApiKey(orgId, principal));
    verify(organizationRepository, never()).save(any());
  }
}
