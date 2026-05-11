package kz.logisto.lguserservice.controller;

import kz.logisto.lguserservice.data.dto.client.CreateClientDto;
import kz.logisto.lguserservice.data.dto.client.UpdateClientDto;
import kz.logisto.lguserservice.data.model.ClientModel;
import kz.logisto.lguserservice.service.ClientService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clients")
public class ClientController {

  private final ClientService clientService;

  @GetMapping("/{id}")
  public ResponseEntity<ClientModel> getById(@PathVariable UUID id, Principal principal) {
    return ResponseEntity.ok(clientService.findById(id, principal));
  }

  @PostMapping
  public ResponseEntity<ClientModel> create(@Valid @RequestBody CreateClientDto dto,
                                            Principal principal) {
    return ResponseEntity.ok(clientService.create(dto, principal));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ClientModel> update(@PathVariable UUID id,
                                            @Valid @RequestBody UpdateClientDto dto,
                                            Principal principal) {
    return ResponseEntity.ok(clientService.update(id, dto, principal));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id, Principal principal) {
    clientService.delete(id, principal);
    return ResponseEntity.noContent().build();
  }
}
