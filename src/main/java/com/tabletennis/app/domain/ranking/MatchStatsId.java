package com.tabletennis.app.domain.ranking;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
@Embeddable @Data @NoArgsConstructor @AllArgsConstructor
public class MatchStatsId implements Serializable {
    @Column(name = "match_id") private Integer matchId;
    @Column(name = "user_id") private Integer userId;
}
