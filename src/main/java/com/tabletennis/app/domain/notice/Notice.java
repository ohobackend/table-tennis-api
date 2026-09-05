package com.tabletennis.app.domain.notice;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.*;
import java.math.BigDecimal;
import com.tabletennis.app.common.util.*;

@Entity
@Table(name = "\"notice\"")
@Getter @Setter
public class Notice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_num")
    private Integer noticeNum;
    @Column(name = "notice_title", length = 100, nullable = false)
    private String noticeTitle;
    @Column(name = "notice_contents", length = 500, nullable = false)
    private String noticeContents;
    @Column(name = "notice_writer", length = 20, nullable = false)
    private String noticeWriter;
    @Column(name = "noti_reg_date")
    private LocalDate notiRegDate;
    @Column(name = "hit_num")
    private Integer hitNum;
}
