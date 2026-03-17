package kz.logisto.lguserservice.controller;

import kz.logisto.lguserservice.data.dto.client.ClientFilterDto;
import kz.logisto.lguserservice.data.dto.organization.OrganizationDto;
import kz.logisto.lguserservice.data.dto.organization.OrganizationFilterDto;
import kz.logisto.lguserservice.data.dto.organization.UserOrganizationDto;
import kz.logisto.lguserservice.data.dto.user.UserFilterDto;
import kz.logisto.lguserservice.data.model.ClientModel;
import kz.logisto.lguserservice.data.model.CountUserClientModel;
import kz.logisto.lguserservice.data.model.OrganizationModel;
import kz.logisto.lguserservice.data.model.OrganizationUserModel;
import kz.logisto.lguserservice.data.model.UserOrganizationModel;
import kz.logisto.lguserservice.service.ClientService;
import kz.logisto.lguserservice.service.OrganizationAccessService;
import kz.logisto.lguserservice.service.OrganizationService;
import kz.logisto.lguserservice.service.CountService;
import kz.logisto.lguserservice.service.UserOrganizationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/organizations")
public class OrganizationController {

  private final CountService countService;
  private final ClientService clientService;
  private final OrganizationService organizationService;
  private final UserOrganizationService userOrganizationService;
  private final OrganizationAccessService organizationAccessService;

  @GetMapping
  public ResponseEntity<Page<OrganizationUserModel>> getOrganizations(
      @ModelAttribute OrganizationFilterDto filter, @PageableDefault Pageable pageable,
      Principal principal) {
    return ResponseEntity.ok(
        userOrganizationService.findAllOrganizationsByUserId(filter, pageable, principal));
  }

  @GetMapping("/{id}/users")
  public ResponseEntity<Page<UserOrganizationModel>> getUsersByOrganizationId(@PathVariable UUID id,
                                                                              @ModelAttribute UserFilterDto filter,
                                                                              @PageableDefault Pageable pageable,
                                                                              Principal principal) {
    return ResponseEntity.ok(
        userOrganizationService.findAllUsersByOrganizationId(id, filter, pageable, principal));
  }

  @GetMapping("/{id}/clients")
  public ResponseEntity<Page<ClientModel>> getClientsByOrganizationId(@PathVariable UUID id,
                                                                      @ModelAttribute ClientFilterDto filter,
                                                                      @PageableDefault Pageable pageable,
                                                                      Principal principal) {
    return ResponseEntity.ok(clientService.findAll(id, filter, pageable, principal));
  }

  @GetMapping("/{id}/warehouse-access")
  public ResponseEntity<Boolean> hasWarehouseAccess(@PathVariable UUID id,
                                                    @RequestParam @Size(max = 36) String userId) {
    return ResponseEntity.ok(organizationAccessService.canManageWarehouse(userId, id));
  }

  @GetMapping("/{id}/membership")
  public ResponseEntity<Boolean> isMember(@PathVariable UUID id,
                                          @RequestParam @Size(max = 36) String userId) {
    return ResponseEntity.ok(organizationAccessService.isMember(userId, id));
  }

  @GetMapping("/{id}/counts")
  public ResponseEntity<CountUserClientModel> countUserClient(@PathVariable UUID id,
                                                              Principal principal) {
    return ResponseEntity.ok(countService.countUserClient(id, principal));
  }

  @PostMapping
  public ResponseEntity<OrganizationModel> createOrganization(
      @Valid @RequestBody OrganizationDto organizationDto, Principal principal) {
    return ResponseEntity.ok(organizationService.create(organizationDto, principal));
  }

  @PutMapping("/{id}")
  public ResponseEntity<OrganizationModel> updateOrganization(@PathVariable UUID id,
                                                              @RequestBody OrganizationDto organizationDto,
                                                              Principal principal) {
    return ResponseEntity.ok(organizationService.update(id, organizationDto, principal));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteOrganization(@PathVariable UUID id, Principal principal) {
    organizationService.delete(id, principal);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/users")
  public ResponseEntity<UserOrganizationModel> addUserToOrganization(
      @Valid @RequestBody UserOrganizationDto userOrganizationDto, Principal principal) {
    return ResponseEntity.ok(
        userOrganizationService.addUserToOrganization(userOrganizationDto, principal));
  }

  @PutMapping("/users")
  public ResponseEntity<UserOrganizationModel> updateUserInOrganization(
      @Valid @RequestBody UserOrganizationDto userOrganizationDto, Principal principal) {
    return ResponseEntity.ok(
        userOrganizationService.updateUserInOrganization(userOrganizationDto, principal));
  }

  @DeleteMapping("/users")
  public ResponseEntity<Void> removeUserFromOrganization(
      @RequestBody UserOrganizationDto userOrganizationDto, Principal principal) {
    userOrganizationService.deleteUserFromOrganization(userOrganizationDto, principal);
    return ResponseEntity.noContent().build();
  }
}
