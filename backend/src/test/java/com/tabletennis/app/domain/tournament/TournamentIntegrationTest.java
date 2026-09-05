package com.tabletennis.app.domain.tournament;
import com.tabletennis.app.ApiIntegrationTest;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;
import java.util.*;

class TournamentIntegrationTest extends ApiIntegrationTest {
    @Test void compositeKeyCrud() throws Exception {
        var t=tournament(); String path=tournamentPath(t);
        assertThat(t.get("tournamentYear").asInt()).isEqualTo(2026);
        mvc.perform(get(path)).andExpect(status().isOk()).andExpect(jsonPath("$.data.tournamentName").value("League"));
        mvc.perform(get("/api/v1/tournaments?keyword=League")).andExpect(jsonPath("$.meta.total").value(1));
        mvc.perform(body(put(path).with(admin()),Map.of("tournamentYear",2026,"tournamentName","New","startDate","2026-05-01","endDate","2026-05-02"))).andExpect(status().isOk()).andExpect(jsonPath("$.data.tournamentName").value("New"));
        mvc.perform(delete(path).with(admin())).andExpect(status().isOk()); mvc.perform(get(path)).andExpect(status().isNotFound());
    }
    @Test void dateAndYearValidation() throws Exception {
        mvc.perform(body(post("/api/v1/tournaments").with(admin()),Map.of("tournamentYear",2026,"tournamentName","Bad","startDate","2026-05-02","endDate","2026-05-01"))).andExpect(status().isBadRequest());
        var t=tournament();
        mvc.perform(body(put(tournamentPath(t)).with(admin()),Map.of("tournamentYear",2027,"tournamentName","Bad","startDate","2026-05-01","endDate","2026-05-02"))).andExpect(status().isBadRequest());
    }
}
