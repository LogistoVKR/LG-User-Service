package kz.logisto.lguserservice.controller;

import kz.logisto.lguserservice.data.dto.user.UserFilterDto;
import kz.logisto.lguserservice.data.model.UserModel;
import kz.logisto.lguserservice.service.UserService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

  private final UserService userService;

  @GetMapping
  public ResponseEntity<Page<UserModel>> getUsers(@ModelAttribute UserFilterDto filter,
                                                  @PageableDefault Pageable pageable) {
    return ResponseEntity.ok(userService.findAll(filter, pageable));
  }

  @PostMapping("/login")
  public ResponseEntity<UserModel> login(Principal principal) {
    return ResponseEntity.ok(userService.login(principal));
  }
}
