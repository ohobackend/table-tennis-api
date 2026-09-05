package com.tabletennis.app.domain.dashboard;
import com.tabletennis.app.ApiIntegrationTest;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;
import java.util.*;

class DashboardIntegrationTest extends ApiIntegrationTest {
    @Test void todayAndCompletedResults() throws Exception {
        int id=match(user("A"),user("B")).get("matchId").asInt();
        mvc.perform(get("/api/v1/dashboard/today-matches")).andExpect(status().isOk()).andExpect(jsonPath("$.meta.total").value(1));
        created("/api/v1/matches/"+id+"/sets",winningSets());
        mvc.perform(get("/api/v1/dashboard/today-matches")).andExpect(jsonPath("$.meta.total").value(0));
        mvc.perform(get("/api/v1/dashboard/recent-results")).andExpect(jsonPath("$.data[0].matchId").value(id));
        mvc.perform(get("/api/v1/dashboard/top-players")).andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(2));
    }
}
