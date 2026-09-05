package com.tabletennis.app.domain.participant;
import com.tabletennis.app.domain.participant.dto.ParticipantRequest;
import com.tabletennis.app.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/v1/tournaments/{year}/{tournamentId}/participants") @RequiredArgsConstructor
public class ParticipantController {
    private final ParticipantService service;
    @GetMapping public ApiResponse<?> list(@PathVariable int year,@PathVariable int tournamentId,
        @RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size,
        @RequestParam(defaultValue="reg_date,desc") String sort,@RequestParam(required=false) String keyword) { return service.list(year,tournamentId,page,size,sort,keyword); }
    @PostMapping @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ApiResponse<?> create(@PathVariable int year,@PathVariable int tournamentId,@Valid @RequestBody ParticipantRequest r) { return ApiResponse.ok(service.create(year,tournamentId,r)); }
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> update(@PathVariable int year,@PathVariable int tournamentId,@PathVariable int id,@Valid @RequestBody ParticipantRequest r) { return ApiResponse.ok(service.update(year,tournamentId,id,r)); }
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> delete(@PathVariable int year,@PathVariable int tournamentId,@PathVariable int id) { service.delete(year,tournamentId,id); return ApiResponse.ok(null); }
}
