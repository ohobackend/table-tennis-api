package com.tabletennis.app.domain.match;
import org.springframework.data.jpa.repository.*;
public interface MatchParticipantRepository extends JpaRepository<MatchParticipant, Integer>, JpaSpecificationExecutor<MatchParticipant> { }
