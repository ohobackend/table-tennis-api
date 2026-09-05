package com.tabletennis.app.domain.board.dto;
import java.time.LocalDate;
public record BoardResponse(Integer boardId,String boardTitle,String boardContent,String boardWriter,LocalDate boardRegDate) {}
