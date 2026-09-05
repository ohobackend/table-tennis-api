package com.tabletennis.app.domain.comment;
import org.springframework.data.jpa.repository.*;
public interface CommentRepository extends JpaRepository<Comment, Integer>, JpaSpecificationExecutor<Comment> { }
