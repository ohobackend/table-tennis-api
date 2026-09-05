package com.tabletennis.app.domain.match;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.*;
import java.math.BigDecimal;
import com.tabletennis.app.common.util.*;

@Entity
@Table(name = "\"match\"")
@Getter @Setter
public class Match {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    private Integer matchId;
    @Column(name = "competition_id")
    private Integer competitionId;
    @Column(name = "group_id")
    private Integer groupId;
    @Column(name = "match_round")
    private Integer matchRound;
    @Column(name = "match_number")
    private Integer matchNumber;
    @Column(name = "court_number")
    private Integer courtNumber;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private Status status;
    @Enumerated(EnumType.STRING)
    @Column(name = "winner_side", length = 10)
    private WinnerSide winnerSide;
    @Column(name = "side_a_sets")
    private Integer sideASets;
    @Column(name = "side_b_sets")
    private Integer sideBSets;
    @Column(name = "total_sets")
    private Integer totalSets;
    @Column(name = "next_match_id")
    private Integer nextMatchId;
    @Column(name = "notes", length = 500)
    private String notes;
    @Column(name = "scheduled_at")
    private OffsetDateTime scheduledAt;
    @Column(name = "location", length = 100)
    private String location;
    @Column(name = "completed_at")
    private OffsetDateTime completedAt;
    @Version
    private Long version;
    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="competition_id", referencedColumnName="competition_id", insertable=false, updatable=false)
    private com.tabletennis.app.domain.competition.Competition competition;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="group_id", referencedColumnName="group_id", insertable=false, updatable=false)
    private com.tabletennis.app.domain.group.Group group;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="next_match_id", referencedColumnName="match_id", insertable=false, updatable=false)
    private com.tabletennis.app.domain.match.Match nextMatch;
}
