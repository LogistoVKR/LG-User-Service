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
import kz.logisto.lguserservice.service.OrganizationService;
import kz.logisto.lguserservice.service.UserOrganizationService;
import kz.logisto.lguserservice.service.UserService;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static kz.logisto.lguserservice.data.enums.OrganizationRole.OWNER;

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
}
