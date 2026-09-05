package com.tabletennis.app.domain.group;
import org.springframework.data.jpa.repository.*;
public interface GroupRepository extends JpaRepository<Group, Integer>, JpaSpecificationExecutor<Group> { }
