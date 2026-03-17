package kz.logisto.lguserservice.service;

import kz.logisto.lguserservice.data.model.CountUserClientModel;
import java.security.Principal;
import java.util.UUID;

public interface CountService {

  CountUserClientModel countUserClient(UUID organizationId, Principal principal);
}
