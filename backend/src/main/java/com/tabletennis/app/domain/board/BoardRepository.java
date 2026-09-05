package com.tabletennis.app.domain.board;
import org.springframework.data.jpa.repository.*;
public interface BoardRepository extends JpaRepository<Board, Integer>, JpaSpecificationExecutor<Board> { }
