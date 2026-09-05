package com.tabletennis.app.domain.competition;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.*;
import java.math.BigDecimal;
import com.tabletennis.app.common.util.*;

@Entity
@Table(name = "\"competition\"")
@Getter @Setter
public class Competition {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "competition_id")
    private Integer competitionId;
    @Column(name = "tournament_year")
    private Integer tournamentYear;
    @Column(name = "tournament_id")
    private Integer tournamentId;
    @Column(name = "competition_name", length = 50)
    private String competitionName;
    @Enumerated(EnumType.STRING)
    @Column(name = "competition_type", length = 50)
    private CompetitionType competitionType;
    @Enumerated(EnumType.STRING)
    @Column(name = "match_format", length = 50)
    private MatchFormat matchFormat;
    @Column(name = "competition_order")
    private Integer competitionOrder;
    @Column(name = "has_groups", length = 1)
    private String hasGroups;
    @Column(name = "players_per_group")
    private Integer playersPerGroup;
    @Column(name = "description", length = 200)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private Status status;
    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumns({@JoinColumn(name="tournament_year", referencedColumnName="tournament_year", insertable=false, updatable=false), @JoinColumn(name="tournament_id", referencedColumnName="tournament_id", insertable=false, updatable=false)})
    private com.tabletennis.app.domain.tournament.Tournament tournament;
}
