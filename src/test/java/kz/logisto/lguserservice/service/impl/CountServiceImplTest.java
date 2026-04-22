package kz.logisto.lguserservice.service.impl;

import kz.logisto.lguserservice.data.model.CountUserClientModel;
import kz.logisto.lguserservice.exception.ForbiddenException;
import kz.logisto.lguserservice.service.ClientService;
import kz.logisto.lguserservice.service.OrganizationAccessService;
import kz.logisto.lguserservice.service.UserOrganizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CountServiceImplTest {

  @Mock
  private ClientService clientService;
  @Mock
  private UserOrganizationService userOrganizationService;
  @Mock
  private OrganizationAccessService organizationAccessService;

  @InjectMocks
  private CountServiceImpl countService;

  private final Principal principal = () -> "user-1";

  @Test
  void countUserClient_member_returnsCounts() {
    UUID orgId = UUID.randomUUID();

    when(organizationAccessService.isMember("user-1", orgId)).thenReturn(true);
    when(userOrganizationService.countByOrganizationId(orgId)).thenReturn(5);
    when(clientService.countByOrganizationId(orgId)).thenReturn(10);

    CountUserClientModel result = countService.countUserClient(orgId, principal);

    assertEquals(5, result.getUsers());
    assertEquals(10, result.getClients());
  }

  @Test
  void countUserClient_notMember_throwsForbidden() {
    UUID orgId = UUID.randomUUID();

    when(organizationAccessService.isMember("user-1", orgId)).thenReturn(false);

    assertThrows(ForbiddenException.class,
        () -> countService.countUserClient(orgId, principal));
  }
}
