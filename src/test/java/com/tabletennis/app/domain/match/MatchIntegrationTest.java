package com.tabletennis.app.domain.match;
import com.tabletennis.app.ApiIntegrationTest;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;
import java.util.*;

class MatchIntegrationTest extends ApiIntegrationTest {
    @Test void crudParticipantsAndSchedule() throws Exception {
        var a=user("A"); var b=user("B"); var m=match(a,b); int id=m.get("matchId").asInt(); int cid=m.get("competitionId").asInt();
        mvc.perform(get("/api/v1/matches/"+id)).andExpect(status().isOk()).andExpect(jsonPath("$.data.participants.length()").value(2));
        mvc.perform(get("/api/v1/competitions/"+cid+"/matches")).andExpect(jsonPath("$.meta.total").value(1));
        mvc.perform(body(put("/api/v1/matches/"+id).with(admin()),Map.of("competitionId",cid,"matchRound",2,"courtNumber",3,
            "participants",List.of(Map.of("userId",a.getUserId(),"side","SIDE_A","participantOrder",1),Map.of("userId",b.getUserId(),"side","SIDE_B","participantOrder",1)))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.courtNumber").value(3));
        mvc.perform(delete("/api/v1/matches/"+id).with(admin())).andExpect(status().isOk());
        mvc.perform(get("/api/v1/matches/"+id)).andExpect(status().isNotFound());
    }
    @Test void rejectsInvalidParticipantsAndNextMatchCycle() throws Exception {
        var a=user("A"); var b=user("B"); var m=match(a,b); int id=m.get("matchId").asInt(); int cid=m.get("competitionId").asInt();
        mvc.perform(body(post("/api/v1/matches").with(admin()),Map.of("competitionId",cid,"matchRound",0,
            "participants",List.of(Map.of("userId",a.getUserId(),"side","SIDE_A","participantOrder",1),Map.of("userId",a.getUserId(),"side","SIDE_B","participantOrder",1)))))
            .andExpect(status().isBadRequest());
        mvc.perform(body(put("/api/v1/matches/"+id).with(admin()),Map.of("competitionId",cid,"matchRound",0,"nextMatchId",id,
            "participants",List.of(Map.of("userId",a.getUserId(),"side","SIDE_A","participantOrder",1),Map.of("userId",b.getUserId(),"side","SIDE_B","participantOrder",1)))))
            .andExpect(status().isBadRequest());
    }
}
