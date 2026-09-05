package com.tabletennis.app.domain.setscore;
import org.springframework.data.jpa.repository.*;
public interface SetScoreRepository extends JpaRepository<SetScore, Integer>, JpaSpecificationExecutor<SetScore> { }
