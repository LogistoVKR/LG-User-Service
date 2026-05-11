package kz.logisto.lguserservice.service;

import kz.logisto.lguserservice.data.dto.client.ClientFilterDto;
import kz.logisto.lguserservice.data.dto.client.CreateClientDto;
import kz.logisto.lguserservice.data.dto.client.UpdateClientDto;
import kz.logisto.lguserservice.data.model.ClientModel;
import java.security.Principal;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientService {

  ClientModel findById(UUID id, Principal principal);

  Page<ClientModel> findAll(UUID organizationId, ClientFilterDto filter, Pageable pageable,
      Principal principal);

  ClientModel create(CreateClientDto dto, Principal principal);

  ClientModel update(UUID id, UpdateClientDto dto, Principal principal);

  void delete(UUID id, Principal principal);

  int countByOrganizationId(UUID organizationId);

  ClientModel findClientInOrganization(UUID clientId, UUID organizationId);
}
