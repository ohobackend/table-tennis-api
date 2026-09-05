package com.tabletennis.app.domain.group;
import com.tabletennis.app.domain.group.dto.GroupRequests.*;
import com.tabletennis.app.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/v1") @RequiredArgsConstructor
public class GroupController {
    private final GroupService service;
    @GetMapping("/competitions/{competitionId}/groups") public ApiResponse<?> list(@PathVariable int competitionId,@RequestParam(defaultValue="1") int page,
        @RequestParam(defaultValue="20") int size,@RequestParam(defaultValue="reg_date,desc") String sort) { return service.list(competitionId,page,size,sort); }
    @PostMapping("/competitions/{competitionId}/groups") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ApiResponse<?> create(@PathVariable int competitionId,@Valid @RequestBody Create r) { return ApiResponse.ok(service.create(competitionId,r)); }
    @PutMapping("/groups/{id}") @PreAuthorize("hasRole('ADMIN')") public ApiResponse<?> update(@PathVariable int id,@Valid @RequestBody Create r) { return ApiResponse.ok(service.update(id,r)); }
    @GetMapping("/groups/{id}/participants") public ApiResponse<?> participants(@PathVariable int id,@RequestParam(defaultValue="1") int page,
        @RequestParam(defaultValue="20") int size,@RequestParam(defaultValue="reg_date,desc") String sort) { return service.participants(id,page,size,sort); }
    @PostMapping("/groups/{id}/participants") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ApiResponse<?> add(@PathVariable int id,@Valid @RequestBody Participant r) { return ApiResponse.ok(service.add(id,r)); }
    @PutMapping("/groups/{id}/participants/{userId}") @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> rank(@PathVariable int id,@PathVariable int userId,@Valid @RequestBody Rank r) { return ApiResponse.ok(service.rank(id,userId,r)); }
}
