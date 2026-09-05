package com.tabletennis.app.domain.tournament;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
@Embeddable @Data @NoArgsConstructor @AllArgsConstructor
public class TournamentId implements Serializable {
    @Column(name = "tournament_year") private Integer tournamentYear;
    @Column(name = "tournament_id") private Integer tournamentId;
}
