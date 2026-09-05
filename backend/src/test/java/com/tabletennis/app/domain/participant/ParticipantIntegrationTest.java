package com.tabletennis.app.domain.participant;
import com.tabletennis.app.ApiIntegrationTest;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;
import java.util.*;

class ParticipantIntegrationTest extends ApiIntegrationTest {
    @Test void registrationsSearchAndParentScoping() throws Exception {
        var t=tournament(); var other=tournament(); var u=user("Player");
        String path=tournamentPath(t)+"/participants";
        int id=created(path,Map.of("userId",u.getUserId())).get("participantId").asInt();
        mvc.perform(get(path+"?keyword=TestClub")).andExpect(jsonPath("$.meta.total").value(1));
        mvc.perform(body(put(path+"/"+id).with(admin()),Map.of("userId",u.getUserId(),"finalRank",1))).andExpect(status().isOk()).andExpect(jsonPath("$.data.finalRank").value(1));
        mvc.perform(delete(tournamentPath(other)+"/participants/"+id).with(admin())).andExpect(status().isNotFound());
        mvc.perform(delete(path+"/"+id).with(admin())).andExpect(status().isOk());
    }
    @Test void duplicateRegistration() throws Exception {
        var t=tournament(); var u=user("Player"); register(t,u);
        mvc.perform(body(post(tournamentPath(t)+"/participants").with(admin()),Map.of("userId",u.getUserId()))).andExpect(status().isConflict());
    }
}
