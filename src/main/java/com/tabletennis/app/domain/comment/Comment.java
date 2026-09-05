package com.tabletennis.app.domain.comment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.*;
import java.math.BigDecimal;
import com.tabletennis.app.common.util.*;

@Entity
@Table(name = "\"comment\"")
@Getter @Setter
public class Comment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Integer commentId;
    @Column(name = "board_id")
    private Integer boardId;
    @Column(name = "user_id")
    private Integer userId;
    @Column(name = "comment_depth")
    private Integer commentDepth;
    @Column(name = "comment_content", length = 500, nullable = false)
    private String commentContent;
    @Column(name = "comment_writer", length = 20, nullable = false)
    private String commentWriter;
    @Column(name = "comment_reg_date")
    private LocalDate commentRegDate;
    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="board_id", referencedColumnName="board_id", insertable=false, updatable=false)
    private com.tabletennis.app.domain.board.Board board;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", referencedColumnName="user_id", insertable=false, updatable=false)
    private com.tabletennis.app.domain.user.User author;
}
