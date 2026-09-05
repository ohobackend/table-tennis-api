package com.tabletennis.app.domain.setscore;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.*;
import java.math.BigDecimal;
import com.tabletennis.app.common.util.*;

@Entity
@Table(name = "\"set_score\"")
@Getter @Setter
public class SetScore {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "set_id")
    private Integer setId;
    @Column(name = "match_id")
    private Integer matchId;
    @Column(name = "set_number")
    private Integer setNumber;
    @Column(name = "side_a_point")
    private Integer sideAPoint;
    @Column(name = "side_b_point")
    private Integer sideBPoint;
    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="match_id", referencedColumnName="match_id", insertable=false, updatable=false)
    private com.tabletennis.app.domain.match.Match match;
}
