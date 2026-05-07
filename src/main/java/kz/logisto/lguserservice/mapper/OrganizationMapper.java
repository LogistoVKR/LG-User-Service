package kz.logisto.lguserservice.mapper;

import kz.logisto.lguserservice.data.dto.organization.OrganizationDto;
import kz.logisto.lguserservice.data.entity.Organization;
import kz.logisto.lguserservice.data.model.OrganizationModel;
import kz.logisto.lguserservice.data.model.OzonApiKeyModel;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = SPRING)
public interface OrganizationMapper {

  Organization toEntity(OrganizationDto organizationDto);

  @Mapping(target = "hasOzonIntegration", expression = "java(hasOzonIntegration(organization))")
  OrganizationModel toModel(Organization organization);

  @Mapping(target = "hasIntegration", expression = "java(hasOzonIntegration(organization))")
  OzonApiKeyModel toOzonApiKeyModel(Organization organization);

  @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
  void updateEntity(@MappingTarget Organization organization, OrganizationDto organizationDto);

  default boolean hasOzonIntegration(Organization organization) {
    return organization.getOzonApiKey() != null && !organization.getOzonApiKey().isBlank()
        && organization.getOzonClientId() != null && !organization.getOzonClientId().isBlank();
  }
}
