package com.tabletennis.app.domain.ranking;
import org.springframework.data.jpa.repository.*;
public interface MatchStatsRepository extends JpaRepository<MatchStats, MatchStatsId>, JpaSpecificationExecutor<MatchStats> { }
