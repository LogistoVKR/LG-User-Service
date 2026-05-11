package kz.logisto.lguserservice.service.impl;

import jakarta.persistence.criteria.Predicate;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
import kz.logisto.lguserservice.service.ClientService;
import kz.logisto.lguserservice.service.OrganizationAccessService;
import kz.logisto.lguserservice.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

  private final ClientMapper clientMapper;
  private final ClientRepository clientRepository;
  private final OrganizationService organizationService;
  private final OrganizationAccessService organizationAccessService;

  @Override
  public ClientModel findById(UUID id, Principal principal) {
    Client client = clientRepository.findById(id)
        .orElseThrow(NotFoundException::new);
    if (!organizationAccessService.isMember(principal.getName(), client.getOrganizationId())) {
      throw new NotFoundException();
    }
    return clientMapper.toModel(client);
  }

  @Override
  public Page<ClientModel> findAll(UUID organizationId, ClientFilterDto filter, Pageable pageable,
      Principal principal) {
    if (!organizationAccessService.isMember(principal.getName(), organizationId)) {
      return Page.empty();
    }
    Specification<Client> specification = buildSpecification(organizationId, filter);
    return clientRepository.findAll(specification, pageable)
        .map(clientMapper::toModel);
  }

  @Override
  @Transactional
  public ClientModel create(CreateClientDto dto, Principal principal) {
    if (!organizationAccessService.canManageWarehouse(principal.getName(), dto.organizationId())) {
      throw new ForbiddenException();
    }
    Organization organization = organizationService.getReferenceById(dto.organizationId());
    Client client = clientMapper.toEntity(dto);
    client.setOrganization(organization);
    return clientMapper.toModel(clientRepository.save(client));
  }

  @Override
  public ClientModel update(UUID id, UpdateClientDto dto, Principal principal) {
    Client client = clientRepository.findById(id)
        .orElseThrow(NotFoundException::new);
    if (!organizationAccessService.canManageWarehouse(principal.getName(),
        client.getOrganizationId())) {
      throw new ForbiddenException();
    }

    clientMapper.updateEntity(client, dto);
    return clientMapper.toModel(clientRepository.save(client));
  }

  @Override
  public void delete(UUID id, Principal principal) {
    Client client = clientRepository.findById(id)
        .orElseThrow(NotFoundException::new);
    if (!organizationAccessService.canManageWarehouse(principal.getName(),
        client.getOrganizationId())) {
      throw new ForbiddenException();
    }
    clientRepository.delete(client);
  }

  @Override
  public int countByOrganizationId(UUID organizationId) {
    return clientRepository.countByOrganization_Id(organizationId);
  }

  @Override
  public ClientModel findClientInOrganization(UUID clientId, UUID organizationId) {
    Client client = clientRepository.findById(clientId)
        .filter(c -> organizationId.equals(c.getOrganizationId()))
        .orElseThrow(NotFoundException::new);
    return clientMapper.toModel(client);
  }

  private Specification<Client> buildSpecification(UUID organizationId,
      ClientFilterDto filter) {
    return (root, query, cb) -> {
      List<Predicate> andPredicates = new ArrayList<>();

      andPredicates.add(cb.equal(root.get("organization").get("id"), organizationId));

      if (filter.firstName() != null) {
        andPredicates.add(
            cb.like(cb.lower(root.get("firstName")), filter.firstName().toLowerCase() + "%"));
      }

      if (filter.lastName() != null) {
        andPredicates.add(
            cb.like(cb.lower(root.get("lastName")), filter.lastName().toLowerCase() + "%"));
      }

      if (filter.dateOfBirth() != null) {
        andPredicates.add(cb.equal(root.get("dateOfBirth"), filter.dateOfBirth()));
      }

      return cb.and(andPredicates.toArray(new Predicate[0]));
    };
  }
}
