package com.tabletennis.app.domain.participant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.*;
import java.math.BigDecimal;
import com.tabletennis.app.common.util.*;

@Entity
@Table(name = "\"tournament_participant\"")
@Getter @Setter
public class TournamentParticipant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Integer participantId;
    @Column(name = "tournament_year")
    private Integer tournamentYear;
    @Column(name = "tournament_id")
    private Integer tournamentId;
    @Column(name = "user_id")
    private Integer userId;
    @Column(name = "regi_date")
    private LocalDate regiDate;
    @Column(name = "final_rank")
    private Integer finalRank;
    @Column(name = "notes", length = 500)
    private String notes;
    @Column(name = "up_date")
    private LocalDate upDate;
    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", referencedColumnName="user_id", insertable=false, updatable=false)
    private com.tabletennis.app.domain.user.User user;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumns({@JoinColumn(name="tournament_year", referencedColumnName="tournament_year", insertable=false, updatable=false), @JoinColumn(name="tournament_id", referencedColumnName="tournament_id", insertable=false, updatable=false)})
    private com.tabletennis.app.domain.tournament.Tournament tournament;
}
