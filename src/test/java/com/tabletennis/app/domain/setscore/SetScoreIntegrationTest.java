package com.tabletennis.app.domain.setscore;
import com.tabletennis.app.ApiIntegrationTest;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;
import java.util.*;

class SetScoreIntegrationTest extends ApiIntegrationTest {
    @org.springframework.beans.factory.annotation.Autowired com.tabletennis.app.domain.competition.CompetitionRepository competitions;
    @Test void automaticWinnerCorrectionAndFinalize() throws Exception {
        var m=match(user("A"),user("B")); int id=m.get("matchId").asInt();
        var scored=created("/api/v1/matches/"+id+"/sets",winningSets());
        assertThat(scored.get("winnerSide").asText()).isEqualTo("SIDE_A");
        assertThat(scored.get("status").asText()).isEqualTo("COMPLETED");
        assertThat(scored.get("totalSets").asInt()).isEqualTo(3);
        mvc.perform(post("/api/v1/matches/"+id+"/finalize").with(admin())).andExpect(status().isOk());
        int setId=scored.get("sets").get(2).get("setId").asInt();
        mvc.perform(body(put("/api/v1/matches/"+id+"/sets/"+setId).with(admin()),Map.of("setNumber",3,"sideAPoint",8,"sideBPoint",11)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("IN_PROGRESS")).andExpect(jsonPath("$.data.winnerSide").doesNotExist())
            .andExpect(jsonPath("$.data.sideASets").value(2)).andExpect(jsonPath("$.data.sideBSets").value(1));
        mvc.perform(post("/api/v1/matches/"+id+"/finalize").with(admin())).andExpect(status().isConflict());
        created("/api/v1/matches/"+id+"/sets",Map.of("sets",List.of(Map.of("setNumber",4,"sideAPoint",11,"sideBPoint",5))));
        mvc.perform(get("/api/v1/matches/"+id)).andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }
    @Test void rejectsInvalidScoresGapsAndCrossMatchEdits() throws Exception {
        int id=match(user("A"),user("B")).get("matchId").asInt();
        mvc.perform(body(post("/api/v1/matches/"+id+"/sets").with(admin()),Map.of("sets",List.of(Map.of("setNumber",1,"sideAPoint",11,"sideBPoint",10))))).andExpect(status().isBadRequest());
        mvc.perform(body(post("/api/v1/matches/"+id+"/sets").with(admin()),Map.of("sets",List.of(Map.of("setNumber",1,"sideAPoint",13,"sideBPoint",8))))).andExpect(status().isBadRequest());
        // Gap failure must roll back the inserted row. This is verified in a separate, non-test transaction below.
        var other=match(user("C"),user("D")); var scores=created("/api/v1/matches/"+other.get("matchId").asInt()+"/sets",winningSets());
        int setId=scores.get("sets").get(0).get("setId").asInt();
        mvc.perform(body(put("/api/v1/matches/"+id+"/sets/"+setId).with(admin()),Map.of("setNumber",1,"sideAPoint",11,"sideBPoint",5))).andExpect(status().isNotFound());
    }
    @Test @org.springframework.transaction.annotation.Transactional(propagation=org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void gapRollback() throws Exception {
        var a=user("GapA"); var b=user("GapB"); var match=match(a,b); int id=match.get("matchId").asInt();
        var c=competitions.findById(match.get("competitionId").asInt()).orElseThrow();
        mvc.perform(body(post("/api/v1/matches/"+id+"/sets").with(admin()),Map.of("sets",List.of(Map.of("setNumber",2,"sideAPoint",11,"sideBPoint",5))))).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/matches/"+id+"/sets")).andExpect(jsonPath("$.data.length()").value(0));
        mvc.perform(delete("/api/v1/tournaments/"+c.getTournamentYear()+"/"+c.getTournamentId()).with(admin())).andExpect(status().isOk());
        users.deleteById(a.getUserId()); users.deleteById(b.getUserId());
    }
}
