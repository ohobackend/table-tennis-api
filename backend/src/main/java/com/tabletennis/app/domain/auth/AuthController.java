package com.tabletennis.app.domain.auth;
import com.tabletennis.app.domain.auth.dto.AuthRequests.*;
import com.tabletennis.app.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
@RestController @RequestMapping("/api/v1/auth") @RequiredArgsConstructor
public class AuthController {
    private final AuthService service;
    @PostMapping("/signup") @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ApiResponse<?> signup(@Valid @RequestBody Signup r) { return ApiResponse.ok(service.signup(r)); }
    @PostMapping("/login") public ApiResponse<?> login(@Valid @RequestBody Login r) { return ApiResponse.ok(service.login(r)); }
    @PostMapping("/refresh") public ApiResponse<?> refresh(@Valid @RequestBody Refresh r) { return ApiResponse.ok(service.refresh(r.refreshToken())); }
    @PostMapping("/logout") public ApiResponse<?> logout(@AuthenticationPrincipal Jwt jwt) { service.logout(jwt); return ApiResponse.ok(null); }
}
