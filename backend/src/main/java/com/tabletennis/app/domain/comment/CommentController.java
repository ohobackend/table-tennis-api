package com.tabletennis.app.domain.comment;
import com.tabletennis.app.domain.comment.dto.CommentRequest;
import com.tabletennis.app.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
@RestController @RequestMapping("/api/v1") @RequiredArgsConstructor
public class CommentController {
    private final CommentService service;
    @GetMapping("/boards/{boardId}/comments") public ApiResponse<?> list(@PathVariable int boardId,
        @RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size,@RequestParam(defaultValue="reg_date,desc") String sort) { return service.list(boardId,page,size,sort); }
    @PostMapping("/boards/{boardId}/comments") @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ApiResponse<?> create(@PathVariable int boardId,@Valid @RequestBody CommentRequest r,@AuthenticationPrincipal Jwt jwt) { return ApiResponse.ok(service.create(boardId,r,jwt)); }
    @PutMapping("/comments/{id}") public ApiResponse<?> update(@PathVariable int id,@Valid @RequestBody CommentRequest r,@AuthenticationPrincipal Jwt jwt) { return ApiResponse.ok(service.update(id,r,jwt)); }
    @DeleteMapping("/comments/{id}") public ApiResponse<?> delete(@PathVariable int id,@AuthenticationPrincipal Jwt jwt) { service.delete(id,jwt); return ApiResponse.ok(null); }
}
