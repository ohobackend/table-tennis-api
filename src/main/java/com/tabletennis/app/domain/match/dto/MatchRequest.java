package com.tabletennis.app.domain.match.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.tabletennis.app.common.util.*;
import java.time.OffsetDateTime;
import java.util.List;
public record MatchRequest(@NotNull @Positive Integer competitionId,@Positive Integer groupId,
    @NotNull @Min(0) Integer matchRound,@Positive Integer matchNumber,@Positive Integer courtNumber,
    @Positive Integer nextMatchId,@Size(max=500) String notes,OffsetDateTime scheduledAt,@Size(max=100) String location,
    @NotEmpty @Size(max=100) List<@NotNull @Valid Participant> participants) {
    public record Participant(@NotNull @Positive Integer userId,@NotNull Side side,@NotNull @Positive Integer participantOrder) {}
}
