package com.tabletennis.app.domain.group;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.*;
import java.math.BigDecimal;
import com.tabletennis.app.common.util.*;

@Entity
@Table(name = "\"group\"")
@Getter @Setter
public class Group {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Integer groupId;
    @Column(name = "competition_id")
    private Integer competitionId;
    @Column(name = "group_name", length = 50)
    private String groupName;
    @Column(name = "creat_date")
    private LocalDate creatDate;
    @Column(name = "up_date")
    private LocalDate upDate;
    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="competition_id", referencedColumnName="competition_id", insertable=false, updatable=false)
    private com.tabletennis.app.domain.competition.Competition competition;
}
