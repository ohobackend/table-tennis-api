package com.tabletennis.app.domain.group;
import org.springframework.data.jpa.repository.*;
public interface GroupParticipantRepository extends JpaRepository<GroupParticipant, GroupParticipantId>, JpaSpecificationExecutor<GroupParticipant> { }
