package com.tabletennis.app.domain.user;
import org.springframework.data.jpa.repository.*;
public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> { }
