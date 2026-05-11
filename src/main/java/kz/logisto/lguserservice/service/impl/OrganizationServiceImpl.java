package kz.logisto.lguserservice.service.impl;

import static kz.logisto.lguserservice.data.enums.OrganizationRole.OWNER;

import jakarta.persistence.criteria.Predicate;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import kz.logisto.lguserservice.data.dto.organization.OrganizationAllFilterDto;
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
import kz.logisto.lguserservice.service.OrganizationService;
import kz.logisto.lguserservice.service.UserOrganizationService;
import kz.logisto.lguserservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

  private final UserService userService;
  private final OrganizationMapper organizationMapper;
  private final OrganizationRepository organizationRepository;
  private final UserOrganizationService userOrganizationService;
  private final OrganizationAccessService organizationAccessService;

  @Override
  public Organization getReferenceById(UUID id) {
    return organizationRepository.getReferenceById(id);
  }

  @Override
  public Page<OrganizationModel> findAll(OrganizationAllFilterDto filter, Pageable pageable) {
    return organizationRepository.findAll(buildSpecification(filter), pageable)
        .map(organizationMapper::toModel);
  }

  @Override
  @Transactional
  public OrganizationModel create(OrganizationDto organizationDto, Principal principal) {
    User user = userService.getOrThrow(principal);
    Organization organization = organizationRepository.save(
        organizationMapper.toEntity(organizationDto));

    userOrganizationService.addUserToOrganization(user, organization, OWNER);

    return organizationMapper.toModel(organization);
  }

  @Override
  public OrganizationModel update(UUID id, OrganizationDto organizationDto, Principal principal) {
    Organization organization = userOrganizationService.getOrganizationIfCanManage(id, principal);
    organizationMapper.updateEntity(organization, organizationDto);
    return organizationMapper.toModel(organizationRepository.save(organization));
  }

  @Override
  public void delete(UUID id, Principal principal) {
    if (!organizationAccessService.canManageOrganization(principal.getName(), id)) {
      throw new ForbiddenException();
    }
    Organization organization = organizationRepository.findById(id)
        .orElseThrow(NotFoundException::new);
    organization.setDeleted(true);
    organizationRepository.save(organization);
  }

  @Override
  public OzonApiKeyModel getOzonApiKey(UUID organizationId) {
    Organization organization = organizationRepository.findById(organizationId)
        .filter(org -> !org.isDeleted())
        .orElseThrow(NotFoundException::new);
    return organizationMapper.toOzonApiKeyModel(organization);
  }

  @Override
  public void deleteOzonApiKey(UUID organizationId, Principal principal) {
    Organization organization = userOrganizationService.getOrganizationIfCanManage(
        organizationId, principal);
    organization.setOzonApiKey(null);
    organization.setOzonClientId(null);
    organizationRepository.save(organization);
  }

  private Specification<Organization> buildSpecification(OrganizationAllFilterDto filter) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(cb.equal(root.get("deleted"), false));
      if (filter.hasOzonIntegration() != null) {
        Predicate hasIntegration = cb.and(
            cb.isNotNull(root.get("ozonApiKey")),
            cb.isNotNull(root.get("ozonClientId"))
        );
        predicates.add(filter.hasOzonIntegration() ? hasIntegration : cb.not(hasIntegration));
      }
      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
