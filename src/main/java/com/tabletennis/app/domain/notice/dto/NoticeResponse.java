package com.tabletennis.app.domain.notice.dto;
import java.time.LocalDate;
public record NoticeResponse(Integer noticeNum,String noticeTitle,String noticeContents,String noticeWriter,LocalDate notiRegDate,Integer hitNum) {}
