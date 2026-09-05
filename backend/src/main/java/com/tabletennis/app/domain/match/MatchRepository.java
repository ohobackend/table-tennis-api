package com.tabletennis.app.domain.match;
import org.springframework.data.jpa.repository.*;
public interface MatchRepository extends JpaRepository<Match, Integer>, JpaSpecificationExecutor<Match> { }
