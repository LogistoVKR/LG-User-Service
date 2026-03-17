package kz.logisto.lguserservice.mapper;

import kz.logisto.lguserservice.data.dto.organization.OrganizationDto;
import kz.logisto.lguserservice.data.entity.Organization;
import kz.logisto.lguserservice.data.model.OrganizationModel;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = SPRING)
public interface OrganizationMapper {

  Organization toEntity(OrganizationDto organizationDto);

  OrganizationModel toModel(Organization organization);

  @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
  void updateEntity(@MappingTarget Organization organization, OrganizationDto organizationDto);
}
