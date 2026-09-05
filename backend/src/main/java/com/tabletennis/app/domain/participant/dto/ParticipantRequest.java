package com.tabletennis.app.domain.participant.dto;
import jakarta.validation.constraints.*;
public record ParticipantRequest(@NotNull @Positive Integer userId,@Positive Integer finalRank,@Size(max=500) String notes) {}
