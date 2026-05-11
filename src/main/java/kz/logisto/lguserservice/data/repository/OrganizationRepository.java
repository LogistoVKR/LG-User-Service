package kz.logisto.lguserservice.data.repository;

import kz.logisto.lguserservice.data.entity.Organization;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID>,
    JpaSpecificationExecutor<Organization> { }
