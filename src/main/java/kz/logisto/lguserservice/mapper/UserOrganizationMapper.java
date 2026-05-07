package kz.logisto.lguserservice.mapper;

import kz.logisto.lguserservice.data.entity.Organization;
import kz.logisto.lguserservice.data.entity.User;
import kz.logisto.lguserservice.data.entity.UserOrganization;
import kz.logisto.lguserservice.data.enums.OrganizationRole;
import kz.logisto.lguserservice.data.model.OrganizationUserModel;
import kz.logisto.lguserservice.data.model.UserOrganizationModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface UserOrganizationMapper {

  @Mapping(target = "id", source = "user.id")
  @Mapping(target = "email", source = "user.email")
  @Mapping(target = "username", source = "user.username")
  @Mapping(target = "firstName", source = "user.firstName")
  @Mapping(target = "lastName", source = "user.lastName")
  UserOrganizationModel toUserModel(UserOrganization userOrganization);

  @Mapping(target = "id", source = "organization.id")
  @Mapping(target = "name", source = "organization.name")
  @Mapping(target = "description", source = "organization.description")
  @Mapping(target = "hasOzonIntegration",
      expression = "java(userOrganization.getOrganization().getOzonApiKey() != null && !userOrganization.getOrganization().getOzonApiKey().isBlank())")
  OrganizationUserModel toOrganizationModel(UserOrganization userOrganization);

  @Mapping(target = "created", ignore = true)
  @Mapping(target = "id.userId", source = "user.id")
  @Mapping(target = "id.organizationId", source = "organization.id")
  @Mapping(target = "user", source = "user")
  @Mapping(target = "organization", source = "organization")
  @Mapping(target = "role", source = "role")
  UserOrganization toEntity(User user, Organization organization, OrganizationRole role);
}
