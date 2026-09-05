package com.tabletennis.app.domain.ranking;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.*;
import java.math.BigDecimal;
import com.tabletennis.app.common.util.*;

@Entity
@Table(name = "\"match_stats\"")
@Getter @Setter
public class MatchStats {
    @EmbeddedId
    private MatchStatsId id;
    @Column(name = "aces")
    private Integer aces;
    @Column(name = "faults")
    private Integer faults;
    @Column(name = "rallies")
    private Integer rallies;
    @Column(name = "win_rate")
    private Double winRate;
    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="match_id", referencedColumnName="match_id", insertable=false, updatable=false)
    private com.tabletennis.app.domain.match.Match match;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", referencedColumnName="user_id", insertable=false, updatable=false)
    private com.tabletennis.app.domain.user.User user;
}
