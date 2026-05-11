package kz.logisto.lguserservice.service;

import kz.logisto.lguserservice.data.dto.organization.OrganizationAllFilterDto;
import kz.logisto.lguserservice.data.dto.organization.OrganizationDto;
import kz.logisto.lguserservice.data.entity.Organization;
import kz.logisto.lguserservice.data.model.OrganizationModel;
import kz.logisto.lguserservice.data.model.OzonApiKeyModel;
import java.security.Principal;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrganizationService {

  Organization getReferenceById(UUID id);

  Page<OrganizationModel> findAll(OrganizationAllFilterDto filter, Pageable pageable);

  OrganizationModel create(OrganizationDto organizationDto, Principal principal);

  OrganizationModel update(UUID id, OrganizationDto organizationDto, Principal principal);

  void delete(UUID id, Principal principal);

  OzonApiKeyModel getOzonApiKey(UUID organizationId);

  void deleteOzonApiKey(UUID organizationId, Principal principal);
}
