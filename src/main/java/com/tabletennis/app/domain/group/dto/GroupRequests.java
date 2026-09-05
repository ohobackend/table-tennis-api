package com.tabletennis.app.domain.group.dto;
import jakarta.validation.constraints.*;
public final class GroupRequests {
    public record Create(@NotBlank @Size(max=50) String groupName) {}
    public record Participant(@NotNull @Positive Integer userId,@Positive Integer groupRank) {}
    public record Rank(@Positive Integer groupRank) {}
}
