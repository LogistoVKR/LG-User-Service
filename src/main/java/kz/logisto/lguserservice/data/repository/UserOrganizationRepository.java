package kz.logisto.lguserservice.data.repository;

import kz.logisto.lguserservice.data.entity.UserOrganization;
import kz.logisto.lguserservice.data.entity.key.UserOrganizationId;
import kz.logisto.lguserservice.data.enums.OrganizationRole;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserOrganizationRepository extends
    JpaRepository<UserOrganization, UserOrganizationId>,
    JpaSpecificationExecutor<UserOrganization> {

  @Query("select uo from UserOrganization uo "
      + "where uo.id.userId = :userId and uo.id.organizationId = :organizationId")
  Optional<UserOrganization> findById(String userId, UUID organizationId);

  @Query("select count(uo) > 0 from UserOrganization uo "
      + "where uo.id.userId = :userId and uo.id.organizationId = :organizationId")
  boolean existsById(String userId, UUID organizationId);

  @Query("select count(uo) > 0 from UserOrganization uo "
      + "where uo.id.userId = :userId and uo.id.organizationId = :organizationId "
      + "and uo.role in :roles")
  boolean hasAnyRole(String userId, UUID organizationId, OrganizationRole... roles);

  int countByOrganization_Id(UUID organizationId);
}
