package com.tabletennis.app.domain.user.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record UserUpdateRequest(@NotBlank @Size(max=20) String userName,
    @NotBlank @Size(max=30) String realName,@Size(max=20) String phoneNumber,
    @Past LocalDate birthDate,@Pattern(regexp="M|F") String gender,
    @PositiveOrZero Integer openRanking,@PositiveOrZero Integer regionRanking,
    @Size(max=50) String clubName,@Size(max=20) String userType,@Size(max=2048) String profileImage) {}
