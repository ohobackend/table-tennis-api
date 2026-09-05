package com.tabletennis.app.domain.tournament.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.math.BigDecimal;
public record TournamentRequest(@NotNull @Min(1900) @Max(9999) Integer tournamentYear,
    @NotBlank @Size(max=100) String tournamentName,@Size(max=100) String location,@Positive Integer organizerId,
    @NotNull LocalDate startDate,@NotNull LocalDate endDate,@DecimalMin("0") BigDecimal entryFee,
    @Size(max=500) String eventInfo,@Size(max=500) String prizeInfo,@Size(max=500) String notes) {}
