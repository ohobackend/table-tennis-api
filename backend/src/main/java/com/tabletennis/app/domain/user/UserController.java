package com.tabletennis.app.domain.user;
import com.tabletennis.app.domain.user.dto.*;
import com.tabletennis.app.common.response.*;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
@RestController @RequestMapping("/api/v1/users") @RequiredArgsConstructor
public class UserController {
    private final UserService service;
    @GetMapping public ApiResponse<?> list(@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size,
        @RequestParam(defaultValue="reg_date,desc") String sort,@RequestParam(required=false) String keyword,
        @RequestParam(required=false) String club,@RequestParam(required=false) String gender) { return service.list(page,size,sort,keyword,club,gender); }
    @GetMapping("/{id}") public ApiResponse<?> get(@PathVariable int id) { return ApiResponse.ok(service.get(id)); }
    @PutMapping("/{id}") public ApiResponse<?> update(@PathVariable int id,@Valid @RequestBody UserUpdateRequest r,@AuthenticationPrincipal Jwt jwt) { return ApiResponse.ok(service.update(id,r,jwt)); }
}
