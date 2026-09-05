package com.tabletennis.app.domain.ranking;
import com.tabletennis.app.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1") @RequiredArgsConstructor
public class RankingController {
    private final RankingService service;
    @GetMapping("/rankings") public ApiResponse<?> rankings(@RequestParam(defaultValue="month") String period,@RequestParam(required=false) String club,
        @RequestParam(required=false) String gender,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size) { return service.rankings(period,club,gender,page,size); }
    @GetMapping("/players/{userId}/stats") public ApiResponse<?> stats(@PathVariable int userId) { return ApiResponse.ok(service.stats(userId)); }
    @GetMapping("/players/{userId}/matches") public ApiResponse<?> matches(@PathVariable int userId,@RequestParam(defaultValue="1") int page,
        @RequestParam(defaultValue="20") int size,@RequestParam(defaultValue="reg_date,desc") String sort) { return service.playerMatches(userId,page,size,sort); }
    @GetMapping("/results") public ApiResponse<?> results(@RequestParam(defaultValue="individual") String type,@RequestParam(required=false) String name,
        @RequestParam(required=false) String club,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size,
        @RequestParam(defaultValue="reg_date,desc") String sort) { return service.results(type,name,club,page,size,sort); }
}
