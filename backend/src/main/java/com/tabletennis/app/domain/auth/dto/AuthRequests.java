package com.tabletennis.app.domain.auth.dto;
import jakarta.validation.constraints.*;
public final class AuthRequests {
    public record Signup(@NotBlank @Email @Size(max=30) String email,
        @NotBlank @Size(min=8,max=72) String password,
        @NotBlank @Size(max=20) String userName,
        @NotBlank @Size(max=30) String realName) {}
    public record Login(@NotBlank @Email @Size(max=30) String email,@NotBlank @Size(max=72) String password) {}
    public record Refresh(@NotBlank @Size(max=200) String refreshToken) {}
    public record Tokens(String accessToken,String refreshToken,long expiresIn,String tokenType) {}
}
