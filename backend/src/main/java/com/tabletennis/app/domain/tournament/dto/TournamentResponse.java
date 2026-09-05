package com.tabletennis.app.domain.tournament.dto;
import java.time.LocalDate;
import java.math.BigDecimal;
public record TournamentResponse(Integer tournamentYear,Integer tournamentId,String tournamentName,String location,Integer organizerId,
    LocalDate startDate,LocalDate endDate,BigDecimal entryFee,String eventInfo,String prizeInfo,String notes) {}
