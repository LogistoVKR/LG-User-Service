package kz.logisto.lguserservice.data.repository;

import kz.logisto.lguserservice.data.entity.Client;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID>,
    JpaSpecificationExecutor<Client> {

  int countByOrganization_Id(UUID organizationId);
}
