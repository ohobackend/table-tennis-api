package com.tabletennis.app.domain.tournament;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.*;
import java.math.BigDecimal;
import com.tabletennis.app.common.util.*;

@Entity
@Table(name = "\"tournament\"")
@Getter @Setter
public class Tournament {
    @EmbeddedId
    private TournamentId id;
    @Column(name = "tournament_name", length = 100, nullable = false)
    private String tournamentName;
    @Column(name = "location", length = 100)
    private String location;
    @Column(name = "organizer_id")
    private Integer organizerId;
    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;
    @Column(name = "entry_fee")
    private BigDecimal entryFee;
    @Column(name = "event_info", length = 500)
    private String eventInfo;
    @Column(name = "prize_info", length = 500)
    private String prizeInfo;
    @Column(name = "notes", length = 500)
    private String notes;
    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="organizer_id", referencedColumnName="user_id", insertable=false, updatable=false)
    private com.tabletennis.app.domain.user.User organizer;
}
