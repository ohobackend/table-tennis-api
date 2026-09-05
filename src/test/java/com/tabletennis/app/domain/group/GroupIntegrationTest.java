package com.tabletennis.app.domain.group;
import com.tabletennis.app.ApiIntegrationTest;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;
import java.util.*;

class GroupIntegrationTest extends ApiIntegrationTest {
    @Test void groupsMembershipAndRanks() throws Exception {
        var t=tournament(); var u=user("Player"); var outsider=user("Outsider"); register(t,u);
        var c=competition(t,true); String path="/api/v1/competitions/"+c.get("competitionId").asInt()+"/groups";
        int id=created(path,Map.of("groupName","A")).get("groupId").asInt();
        mvc.perform(get(path)).andExpect(jsonPath("$.meta.total").value(1));
        mvc.perform(body(put("/api/v1/groups/"+id).with(admin()),Map.of("groupName","B"))).andExpect(status().isOk());
        created("/api/v1/groups/"+id+"/participants",Map.of("userId",u.getUserId()));
        mvc.perform(get("/api/v1/groups/"+id+"/participants")).andExpect(jsonPath("$.data[0].userId").value(u.getUserId()));
        mvc.perform(body(put("/api/v1/groups/"+id+"/participants/"+u.getUserId()).with(admin()),Map.of("groupRank",1))).andExpect(status().isOk());
        mvc.perform(body(post("/api/v1/groups/"+id+"/participants").with(admin()),Map.of("userId",u.getUserId()))).andExpect(status().isConflict());
        mvc.perform(body(post("/api/v1/groups/"+id+"/participants").with(admin()),Map.of("userId",outsider.getUserId()))).andExpect(status().isConflict());
    }
}
