package com.tabletennis.app.domain.board.dto;
import jakarta.validation.constraints.*;
public record BoardRequest(@NotBlank @Size(max=100) String boardTitle,@NotBlank @Size(max=500) String boardContent,@NotBlank @Size(max=20) String boardWriter) {}
