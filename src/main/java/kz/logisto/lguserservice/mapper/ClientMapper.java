package kz.logisto.lguserservice.mapper;

import kz.logisto.lguserservice.data.dto.client.CreateClientDto;
import kz.logisto.lguserservice.data.dto.client.UpdateClientDto;
import kz.logisto.lguserservice.data.entity.Client;
import kz.logisto.lguserservice.data.model.ClientModel;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = SPRING)
public interface ClientMapper {

  Client toEntity(CreateClientDto dto);

  ClientModel toModel(Client client);

  @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
  void updateEntity(@MappingTarget Client client, UpdateClientDto dto);
}
