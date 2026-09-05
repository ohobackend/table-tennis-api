package com.tabletennis.app.domain.competition.dto;
import jakarta.validation.constraints.*;
import com.tabletennis.app.common.util.*;
public record CompetitionRequest(@NotBlank @Size(max=50) String competitionName,
    @NotNull CompetitionType competitionType,@NotNull MatchFormat matchFormat,@NotNull @Positive Integer competitionOrder,
    @NotNull @Pattern(regexp="Y|N") String hasGroups,@Positive Integer playersPerGroup,
    @Size(max=200) String description,@NotNull Status status) {}
