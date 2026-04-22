package kz.logisto.lguserservice.service.impl;

import kz.logisto.lguserservice.data.dto.client.ClientFilterDto;
import kz.logisto.lguserservice.data.dto.client.CreateClientDto;
import kz.logisto.lguserservice.data.dto.client.UpdateClientDto;
import kz.logisto.lguserservice.data.entity.Client;
import kz.logisto.lguserservice.data.entity.Organization;
import kz.logisto.lguserservice.data.model.ClientModel;
import kz.logisto.lguserservice.data.repository.ClientRepository;
import kz.logisto.lguserservice.exception.ForbiddenException;
import kz.logisto.lguserservice.exception.NotFoundException;
import kz.logisto.lguserservice.mapper.ClientMapper;
import kz.logisto.lguserservice.service.OrganizationAccessService;
import kz.logisto.lguserservice.service.OrganizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

  @Mock
  private ClientMapper clientMapper;
  @Mock
  private ClientRepository clientRepository;
  @Mock
  private OrganizationService organizationService;
  @Mock
  private OrganizationAccessService organizationAccessService;

  @InjectMocks
  private ClientServiceImpl clientService;

  private final Principal principal = () -> "user-1";

  @Test
  void create_withWarehouseAccess_createsClient() {
    UUID orgId = UUID.randomUUID();
    CreateClientDto dto = new CreateClientDto("John", "Doe", null, LocalDate.of(1990, 1, 1),
        "john@test.com", "+77001234567", orgId);
    Organization org = new Organization();
    org.setId(orgId);
    Client client = new Client();
    ClientModel model = new ClientModel();

    when(organizationAccessService.canManageWarehouse("user-1", orgId)).thenReturn(true);
    when(organizationService.getReferenceById(orgId)).thenReturn(org);
    when(clientMapper.toEntity(dto)).thenReturn(client);
    when(clientRepository.save(client)).thenReturn(client);
    when(clientMapper.toModel(client)).thenReturn(model);

    ClientModel result = clientService.create(dto, principal);

    assertEquals(model, result);
    verify(clientRepository).save(client);
  }

  @Test
  void create_withoutAccess_throwsForbidden() {
    UUID orgId = UUID.randomUUID();
    CreateClientDto dto = new CreateClientDto("John", "Doe", null, null, null, null, orgId);

    when(organizationAccessService.canManageWarehouse("user-1", orgId)).thenReturn(false);

    assertThrows(ForbiddenException.class, () -> clientService.create(dto, principal));
  }

  @Test
  void update_existingClient_updatesFields() {
    UUID clientId = UUID.randomUUID();
    UUID orgId = UUID.randomUUID();
    UpdateClientDto dto = new UpdateClientDto("Jane", "Doe", null, null, null, null);
    Organization org = new Organization();
    org.setId(orgId);
    Client client = new Client();
    client.setId(clientId);
    client.setOrganization(org);
    ClientModel model = new ClientModel();

    when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
    when(organizationAccessService.canManageWarehouse("user-1", orgId)).thenReturn(true);
    when(clientRepository.save(client)).thenReturn(client);
    when(clientMapper.toModel(client)).thenReturn(model);

    ClientModel result = clientService.update(clientId, dto, principal);

    assertEquals(model, result);
    verify(clientMapper).updateEntity(client, dto);
  }

  @Test
  void update_notFound_throwsNotFoundException() {
    UUID clientId = UUID.randomUUID();
    UpdateClientDto dto = new UpdateClientDto("Jane", null, null, null, null, null);

    when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> clientService.update(clientId, dto, principal));
  }

  @Test
  void update_withoutAccess_throwsForbidden() {
    UUID clientId = UUID.randomUUID();
    UUID orgId = UUID.randomUUID();
    UpdateClientDto dto = new UpdateClientDto("Jane", null, null, null, null, null);
    Organization org = new Organization();
    org.setId(orgId);
    Client client = new Client();
    client.setId(clientId);
    client.setOrganization(org);

    when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
    when(organizationAccessService.canManageWarehouse("user-1", orgId)).thenReturn(false);

    assertThrows(ForbiddenException.class, () -> clientService.update(clientId, dto, principal));
  }

  @Test
  void delete_existingClient_deletes() {
    UUID clientId = UUID.randomUUID();
    UUID orgId = UUID.randomUUID();
    Organization org = new Organization();
    org.setId(orgId);
    Client client = new Client();
    client.setId(clientId);
    client.setOrganization(org);

    when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
    when(organizationAccessService.canManageWarehouse("user-1", orgId)).thenReturn(true);

    clientService.delete(clientId, principal);

    verify(clientRepository).delete(client);
  }

  @Test
  void delete_withoutAccess_throwsForbidden() {
    UUID clientId = UUID.randomUUID();
    UUID orgId = UUID.randomUUID();
    Organization org = new Organization();
    org.setId(orgId);
    Client client = new Client();
    client.setId(clientId);
    client.setOrganization(org);

    when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
    when(organizationAccessService.canManageWarehouse("user-1", orgId)).thenReturn(false);

    assertThrows(ForbiddenException.class, () -> clientService.delete(clientId, principal));
  }

  @Test
  @SuppressWarnings("unchecked")
  void findAll_notMember_returnsEmptyPage() {
    UUID orgId = UUID.randomUUID();
    ClientFilterDto filter = new ClientFilterDto(null, null, null);
    Pageable pageable = PageRequest.of(0, 10);

    when(organizationAccessService.isMember("user-1", orgId)).thenReturn(false);

    Page<ClientModel> result = clientService.findAll(orgId, filter, pageable, principal);

    assertTrue(result.isEmpty());
    verify(clientRepository, never()).findAll(any(Specification.class), any(Pageable.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  void findAll_asMember_returnsClients() {
    UUID orgId = UUID.randomUUID();
    ClientFilterDto filter = new ClientFilterDto(null, null, null);
    Pageable pageable = PageRequest.of(0, 10);

    when(organizationAccessService.isMember("user-1", orgId)).thenReturn(true);
    Page<Client> clientPage = new PageImpl<>(List.of(new Client()));
    when(clientRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(clientPage);
    when(clientMapper.toModel(any(Client.class))).thenReturn(new ClientModel());

    Page<ClientModel> result = clientService.findAll(orgId, filter, pageable, principal);

    assertEquals(1, result.getTotalElements());
  }
}
