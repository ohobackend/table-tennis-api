package com.tabletennis.app.domain.match;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.*;
import java.math.BigDecimal;
import com.tabletennis.app.common.util.*;

@Entity
@Table(name = "\"match_participant\"")
@Getter @Setter
public class MatchParticipant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_participant_id")
    private Integer matchParticipantId;
    @Column(name = "match_id")
    private Integer matchId;
    @Column(name = "user_id")
    private Integer userId;
    @Enumerated(EnumType.STRING)
    @Column(name = "side", length = 10)
    private Side side;
    @Column(name = "participant_order")
    private Integer participantOrder;
    @Column(name = "creat_date")
    private LocalDate creatDate;
    @Column(name = "up_date")
    private LocalDate upDate;
    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="match_id", referencedColumnName="match_id", insertable=false, updatable=false)
    private com.tabletennis.app.domain.match.Match match;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", referencedColumnName="user_id", insertable=false, updatable=false)
    private com.tabletennis.app.domain.user.User user;
}
