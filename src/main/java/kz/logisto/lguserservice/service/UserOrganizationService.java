package kz.logisto.lguserservice.service;

import kz.logisto.lguserservice.data.dto.organization.OrganizationFilterDto;
import kz.logisto.lguserservice.data.dto.organization.UserOrganizationDto;
import kz.logisto.lguserservice.data.dto.user.UserFilterDto;
import kz.logisto.lguserservice.data.entity.Organization;
import kz.logisto.lguserservice.data.entity.User;
import kz.logisto.lguserservice.data.enums.OrganizationRole;
import kz.logisto.lguserservice.data.model.OrganizationUserModel;
import kz.logisto.lguserservice.data.model.UserOrganizationModel;
import kz.logisto.lguserservice.exception.ForbiddenException;
import java.security.Principal;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserOrganizationService {

  Page<UserOrganizationModel> findAllUsersByOrganizationId(UUID organizationId,
      UserFilterDto filter, Pageable pageable, Principal principal);

  Page<OrganizationUserModel> findAllOrganizationsByUserId(OrganizationFilterDto filter,
      Pageable pageable, Principal principal);

  void addUserToOrganization(User user, Organization organization, OrganizationRole role);

  UserOrganizationModel addUserToOrganization(UserOrganizationDto userOrganizationDto,
      Principal principal);

  UserOrganizationModel updateUserInOrganization(UserOrganizationDto userOrganizationDto,
      Principal principal);

  void deleteUserFromOrganization(UserOrganizationDto userOrganizationDto, Principal principal);

  Organization getOrganizationIfCanManage(UUID id, Principal principal) throws ForbiddenException;

  int countByOrganizationId(UUID organizationId);
}
