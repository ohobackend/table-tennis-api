package com.tabletennis.app.domain.tournament;
import org.springframework.data.jpa.repository.*;
public interface TournamentRepository extends JpaRepository<Tournament, TournamentId>, JpaSpecificationExecutor<Tournament> { }
