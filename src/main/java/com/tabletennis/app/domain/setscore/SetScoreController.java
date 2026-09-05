package com.tabletennis.app.domain.setscore;
import com.tabletennis.app.domain.setscore.dto.SetRequests.*;
import com.tabletennis.app.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/v1/matches/{matchId}") @RequiredArgsConstructor
public class SetScoreController {
    private final SetScoreService service;
    @GetMapping("/sets") public ApiResponse<?> list(@PathVariable int matchId) { return ApiResponse.ok(service.list(matchId)); }
    @PostMapping("/sets") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ApiResponse<?> add(@PathVariable int matchId,@Valid @RequestBody Batch r) { return ApiResponse.ok(service.add(matchId,r)); }
    @PutMapping("/sets/{setId}") @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> update(@PathVariable int matchId,@PathVariable int setId,@Valid @RequestBody Score r) { return ApiResponse.ok(service.update(matchId,setId,r)); }
    @PostMapping("/finalize") @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> finalizeMatch(@PathVariable int matchId) { return ApiResponse.ok(service.finalizeMatch(matchId)); }
}
