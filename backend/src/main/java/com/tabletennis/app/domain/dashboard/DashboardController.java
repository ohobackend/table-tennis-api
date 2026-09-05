package com.tabletennis.app.domain.dashboard;
import com.tabletennis.app.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/dashboard") @RequiredArgsConstructor
public class DashboardController {
    private final DashboardService service;
    @GetMapping("/today-matches") public ApiResponse<?> today(@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size) { return service.today(page,size); }
    @GetMapping("/top-players") public ApiResponse<?> top() { return service.top(); }
    @GetMapping("/recent-results") public ApiResponse<?> recent(@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size) { return service.recent(page,size); }
}
