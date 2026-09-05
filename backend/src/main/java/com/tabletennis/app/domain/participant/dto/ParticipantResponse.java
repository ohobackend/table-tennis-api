package com.tabletennis.app.domain.participant.dto;
import com.tabletennis.app.domain.user.dto.UserProfile;
import java.time.LocalDate;
public record ParticipantResponse(Integer participantId,Integer tournamentYear,Integer tournamentId,UserProfile user,LocalDate regiDate,Integer finalRank,String notes,LocalDate upDate) {}
