package kz.logisto.lguserservice.service;

import kz.logisto.lguserservice.data.dto.user.UserFilterDto;
import kz.logisto.lguserservice.data.entity.User;
import kz.logisto.lguserservice.data.model.UserModel;
import kz.logisto.lguserservice.exception.NotFoundException;
import java.security.Principal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

  Page<UserModel> findAll(UserFilterDto filter, Pageable pageable);

  User getOrThrow(String userId) throws NotFoundException;

  User getOrThrow(Principal principal) throws NotFoundException;

  UserModel login(Principal principal);
}
