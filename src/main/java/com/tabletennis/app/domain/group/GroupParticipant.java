package com.tabletennis.app.domain.group;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.*;
import java.math.BigDecimal;
import com.tabletennis.app.common.util.*;

@Entity
@Table(name = "\"group_participant\"")
@Getter @Setter
public class GroupParticipant {
    @EmbeddedId
    private GroupParticipantId id;
    @Column(name = "group_rank")
    private Integer groupRank;
    @Column(name = "creat_date")
    private LocalDate creatDate;
    @Column(name = "up_date")
    private LocalDate upDate;
    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="group_id", referencedColumnName="group_id", insertable=false, updatable=false)
    private com.tabletennis.app.domain.group.Group group;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", referencedColumnName="user_id", insertable=false, updatable=false)
    private com.tabletennis.app.domain.user.User user;
}
