package com.tabletennis.app.domain.setscore.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
public final class SetRequests {
    public record Score(@NotNull @Min(1) @Max(5) Integer setNumber,@NotNull @Min(0) @Max(1000) Integer sideAPoint,@NotNull @Min(0) @Max(1000) Integer sideBPoint) {}
    public record Batch(@NotEmpty @Size(max=5) List<@NotNull @Valid Score> sets) {}
}
