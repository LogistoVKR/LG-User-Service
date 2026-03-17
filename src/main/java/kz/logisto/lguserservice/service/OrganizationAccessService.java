package kz.logisto.lguserservice.service;

import java.util.UUID;

public interface OrganizationAccessService {

  boolean canManageWarehouse(String userId, UUID organizationId);

  boolean canManageOrganization(String userId, UUID organizationId);

  boolean isMember(String userId, UUID organizationId);
}
