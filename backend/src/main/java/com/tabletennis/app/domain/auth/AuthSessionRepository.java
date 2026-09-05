package com.tabletennis.app.domain.auth;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface AuthSessionRepository extends JpaRepository<AuthSession,UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from AuthSession s where s.refreshHash=:hash")
    Optional<AuthSession> findForRefresh(@Param("hash") String hash);
}
