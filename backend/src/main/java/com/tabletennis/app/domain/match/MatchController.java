package com.tabletennis.app.domain.match;
import com.tabletennis.app.domain.match.dto.*;
import com.tabletennis.app.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/v1") @RequiredArgsConstructor
public class MatchController {
    private final MatchService service;
    @GetMapping("/competitions/{competitionId}/matches") public ApiResponse<?> list(@PathVariable int competitionId,@RequestParam(defaultValue="1") int page,
        @RequestParam(defaultValue="20") int size,@RequestParam(defaultValue="reg_date,desc") String sort) { return service.list(competitionId,page,size,sort); }
    @GetMapping("/matches/{id}") public ApiResponse<?> get(@PathVariable int id) { return ApiResponse.ok(service.get(id)); }
    @PostMapping("/matches") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ApiResponse<?> create(@Valid @RequestBody MatchRequest r) { return ApiResponse.ok(service.create(r)); }
    @PutMapping("/matches/{id}") @PreAuthorize("hasRole('ADMIN')") public ApiResponse<?> update(@PathVariable int id,@Valid @RequestBody MatchRequest r) { return ApiResponse.ok(service.update(id,r)); }
    @DeleteMapping("/matches/{id}") @PreAuthorize("hasRole('ADMIN')") public ApiResponse<?> delete(@PathVariable int id) { service.delete(id); return ApiResponse.ok(null); }
}
