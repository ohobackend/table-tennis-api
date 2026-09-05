package com.tabletennis.app.domain.comment.dto;
import jakarta.validation.constraints.*;
public record CommentRequest(@NotBlank @Size(max=500) String commentContent,@Min(0) @Max(10) Integer commentDepth) {}
