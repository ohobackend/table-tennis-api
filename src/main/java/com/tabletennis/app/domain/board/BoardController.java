package com.tabletennis.app.domain.board;
import com.tabletennis.app.domain.board.dto.*;
import com.tabletennis.app.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/v1/boards") @RequiredArgsConstructor
public class BoardController {
    private final BoardService service;
    @GetMapping public ApiResponse<?> list(@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size,
        @RequestParam(defaultValue="reg_date,desc") String sort,@RequestParam(required=false) String keyword) { return service.list(page,size,sort,keyword); }
    @GetMapping("/{id}") public ApiResponse<?> get(@PathVariable int id) { return ApiResponse.ok(service.get(id)); }
    @PostMapping @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ApiResponse<?> create(@Valid @RequestBody BoardRequest r) { return ApiResponse.ok(service.create(r)); }
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> update(@PathVariable int id,@Valid @RequestBody BoardRequest r) { return ApiResponse.ok(service.update(id,r)); }
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> delete(@PathVariable int id) { service.delete(id); return ApiResponse.ok(null); }
}
