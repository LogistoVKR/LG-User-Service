package kz.logisto.lguserservice.service.impl;

import kz.logisto.lguserservice.data.enums.OrganizationRole;
import kz.logisto.lguserservice.data.repository.UserOrganizationRepository;
import kz.logisto.lguserservice.service.OrganizationAccessService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizationAccessServiceImpl implements OrganizationAccessService {

  private static final OrganizationRole[] MANAGE_ORGANIZATION = {OrganizationRole.ADMIN,
      OrganizationRole.OWNER};
  private static final OrganizationRole[] MANAGE_WAREHOUSE = {OrganizationRole.WAREHOUSE_MANAGER,
      OrganizationRole.ADMIN, OrganizationRole.OWNER};

  private final UserOrganizationRepository repository;

  @Override
  public boolean canManageWarehouse(String userId, UUID organizationId) {
    return repository.hasAnyRole(userId, organizationId, MANAGE_WAREHOUSE);
  }

  @Override
  public boolean canManageOrganization(String userId, UUID organizationId) {
    return repository.hasAnyRole(userId, organizationId, MANAGE_ORGANIZATION);
  }

  @Override
  public boolean isMember(String userId, UUID organizationId) {
    return repository.existsById(userId, organizationId);
  }
}
