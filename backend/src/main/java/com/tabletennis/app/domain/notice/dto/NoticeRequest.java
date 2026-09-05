package com.tabletennis.app.domain.notice.dto;
import jakarta.validation.constraints.*;
public record NoticeRequest(@NotBlank @Size(max=100) String noticeTitle,@NotBlank @Size(max=500) String noticeContents,@NotBlank @Size(max=20) String noticeWriter) {}
