package com.tabletennis.app.domain.ranking;
import com.tabletennis.app.ApiIntegrationTest;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;
import java.util.*;

class RankingIntegrationTest extends ApiIntegrationTest {
    @Test void statisticsFiltersResultsAndNoSetJoinInflation() throws Exception {
        var a=user("Winner"); var b=user("Loser"); int id=match(a,b).get("matchId").asInt();
        created("/api/v1/matches/"+id+"/sets",winningSets());
        mvc.perform(get("/api/v1/players/"+a.getUserId()+"/stats")).andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalMatches").value(1)).andExpect(jsonPath("$.data.wins").value(1))
            .andExpect(jsonPath("$.data.winRate").value(100.0)).andExpect(jsonPath("$.data.averagePoints").value(34.0));
        mvc.perform(get("/api/v1/players/"+b.getUserId()+"/stats")).andExpect(jsonPath("$.data.winRate").value(0.0));
        mvc.perform(get("/api/v1/rankings?period=month&club=TestClub&gender=m")).andExpect(status().isOk()).andExpect(jsonPath("$.meta.total").value(2));
        mvc.perform(get("/api/v1/results?type=individual&name=Winner")).andExpect(jsonPath("$.meta.total").value(1));
        mvc.perform(get("/api/v1/results?type=team&club=TestClub")).andExpect(jsonPath("$.meta.total").value(0));
        mvc.perform(get("/api/v1/players/"+a.getUserId()+"/matches")).andExpect(jsonPath("$.meta.total").value(1));
        mvc.perform(get("/api/v1/rankings?period=invalid")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/results?type=invalid")).andExpect(status().isBadRequest());
    }
    @Test void zeroStatsAndPeriodBoundary() throws Exception {
        var a=user("Zero"); var b=user("Old"); int id=match(a,b).get("matchId").asInt();
        mvc.perform(get("/api/v1/players/"+a.getUserId()+"/stats")).andExpect(jsonPath("$.data.totalMatches").value(0));
        created("/api/v1/matches/"+id+"/sets",winningSets());
        var m=repository.findById(id).orElseThrow(); m.setCompletedAt(java.time.OffsetDateTime.now().minusYears(2)); repository.flush();
        mvc.perform(get("/api/v1/rankings?period=month")).andExpect(jsonPath("$.meta.total").value(0));
        mvc.perform(get("/api/v1/rankings?period=all")).andExpect(jsonPath("$.meta.total").value(2));
    }
    @org.springframework.beans.factory.annotation.Autowired com.tabletennis.app.domain.match.MatchRepository repository;
}
