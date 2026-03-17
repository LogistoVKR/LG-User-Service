package kz.logisto.lguserservice.service.impl;

import kz.logisto.lguserservice.data.model.CountUserClientModel;
import kz.logisto.lguserservice.exception.ForbiddenException;
import kz.logisto.lguserservice.service.ClientService;
import kz.logisto.lguserservice.service.OrganizationAccessService;
import kz.logisto.lguserservice.service.CountService;
import kz.logisto.lguserservice.service.UserOrganizationService;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CountServiceImpl implements CountService {

  private final ClientService clientService;
  private final UserOrganizationService userOrganizationService;
  private final OrganizationAccessService organizationAccessService;

  @Override
  public CountUserClientModel countUserClient(UUID organizationId, Principal principal) {
    if (!organizationAccessService.isMember(principal.getName(), organizationId)) {
      throw new ForbiddenException();
    }
    int users = userOrganizationService.countByOrganizationId(organizationId);
    int clients = clientService.countByOrganizationId(organizationId);
    return new CountUserClientModel(users, clients);
  }
}
