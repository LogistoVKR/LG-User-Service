package kz.logisto.lguserservice.data.repository;

import kz.logisto.lguserservice.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String>,
    JpaSpecificationExecutor<User> { }
