package com.tabletennis.app.domain.board;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.*;
import java.math.BigDecimal;
import com.tabletennis.app.common.util.*;

@Entity
@Table(name = "\"board\"")
@Getter @Setter
public class Board {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Integer boardId;
    @Column(name = "board_title", length = 100, nullable = false)
    private String boardTitle;
    @Column(name = "board_content", length = 500, nullable = false)
    private String boardContent;
    @Column(name = "board_writer", length = 20, nullable = false)
    private String boardWriter;
    @Column(name = "board_reg_date")
    private LocalDate boardRegDate;
}
