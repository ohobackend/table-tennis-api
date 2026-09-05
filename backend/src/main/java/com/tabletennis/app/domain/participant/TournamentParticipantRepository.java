package com.tabletennis.app.domain.participant;
import org.springframework.data.jpa.repository.*;
public interface TournamentParticipantRepository extends JpaRepository<TournamentParticipant, Integer>, JpaSpecificationExecutor<TournamentParticipant> { }
