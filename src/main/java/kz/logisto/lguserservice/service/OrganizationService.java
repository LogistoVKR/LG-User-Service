package kz.logisto.lguserservice.service;

import kz.logisto.lguserservice.data.dto.organization.OrganizationDto;
import kz.logisto.lguserservice.data.entity.Organization;
import kz.logisto.lguserservice.data.model.OrganizationModel;
import java.security.Principal;
import java.util.UUID;

public interface OrganizationService {

  Organization getReferenceById(UUID id);

  OrganizationModel create(OrganizationDto organizationDto, Principal principal);

  OrganizationModel update(UUID id, OrganizationDto organizationDto, Principal principal);

  void delete(UUID id, Principal principal);
}
