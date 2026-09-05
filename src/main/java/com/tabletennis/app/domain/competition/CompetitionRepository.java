package com.tabletennis.app.domain.competition;
import org.springframework.data.jpa.repository.*;
public interface CompetitionRepository extends JpaRepository<Competition, Integer>, JpaSpecificationExecutor<Competition> { }
