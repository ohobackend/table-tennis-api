package com.tabletennis.app.domain.notice;
import org.springframework.data.jpa.repository.*;
public interface NoticeRepository extends JpaRepository<Notice, Integer>, JpaSpecificationExecutor<Notice> { }
