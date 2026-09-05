package com.tabletennis.app.domain.competition;
import com.tabletennis.app.domain.competition.dto.*;
import com.tabletennis.app.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/v1") @RequiredArgsConstructor
public class CompetitionController {
    private final CompetitionService service;
    @GetMapping("/tournaments/{year}/{tournamentId}/competitions") public ApiResponse<?> list(@PathVariable int year,@PathVariable int tournamentId,
        @RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size,@RequestParam(defaultValue="reg_date,asc") String sort) { return service.list(year,tournamentId,page,size,sort); }
    @PostMapping("/tournaments/{year}/{tournamentId}/competitions") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ApiResponse<?> create(@PathVariable int year,@PathVariable int tournamentId,@Valid @RequestBody CompetitionRequest r) { return ApiResponse.ok(service.create(year,tournamentId,r)); }
    @GetMapping("/competitions/{id}") public ApiResponse<?> get(@PathVariable int id) { return ApiResponse.ok(service.require(id)); }
    @PutMapping("/competitions/{id}") @PreAuthorize("hasRole('ADMIN')") public ApiResponse<?> update(@PathVariable int id,@Valid @RequestBody CompetitionRequest r) { return ApiResponse.ok(service.update(id,r)); }
    @DeleteMapping("/competitions/{id}") @PreAuthorize("hasRole('ADMIN')") public ApiResponse<?> delete(@PathVariable int id) { service.delete(id); return ApiResponse.ok(null); }
}
