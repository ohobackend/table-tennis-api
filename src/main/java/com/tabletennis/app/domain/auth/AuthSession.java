package com.tabletennis.app.domain.auth;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="auth_session") @Getter @Setter
public class AuthSession {
    @Id private UUID sessionId;
    private Integer userId;
    private String refreshHash;
    private Instant expiresAt;
    private boolean revoked;
}
