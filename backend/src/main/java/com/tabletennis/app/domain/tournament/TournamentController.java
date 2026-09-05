package com.tabletennis.app.domain.tournament;
import com.tabletennis.app.domain.tournament.dto.*;
import com.tabletennis.app.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/v1/tournaments") @RequiredArgsConstructor
public class TournamentController {
    private final TournamentService service;
    @GetMapping public ApiResponse<?> list(@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size,
        @RequestParam(defaultValue="reg_date,desc") String sort,@RequestParam(required=false) String keyword) { return service.list(page,size,sort,keyword); }
    @GetMapping("/{year}/{id}") public ApiResponse<?> get(@PathVariable int year,@PathVariable int id) { return ApiResponse.ok(service.get(year,id)); }
    @PostMapping @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ApiResponse<?> create(@Valid @RequestBody TournamentRequest r) { return ApiResponse.ok(service.create(r)); }
    @PutMapping("/{year}/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> update(@PathVariable int year,@PathVariable int id,@Valid @RequestBody TournamentRequest r) { return ApiResponse.ok(service.update(year,id,r)); }
    @DeleteMapping("/{year}/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> delete(@PathVariable int year,@PathVariable int id) { service.delete(year,id); return ApiResponse.ok(null); }
}
