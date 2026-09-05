package com.tabletennis.app.domain.competition;
import com.tabletennis.app.ApiIntegrationTest;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;
import java.util.*;

class CompetitionIntegrationTest extends ApiIntegrationTest {
    @Test void crudAndEnumValidation() throws Exception {
        var t=tournament(); var c=competition(t,false); int id=c.get("competitionId").asInt();
        mvc.perform(get(tournamentPath(t)+"/competitions")).andExpect(jsonPath("$.meta.total").value(1));
        mvc.perform(get("/api/v1/competitions/"+id)).andExpect(status().isOk());
        mvc.perform(body(put("/api/v1/competitions/"+id).with(admin()),Map.of("competitionName","Updated","competitionType","SINGLE_ELIMINATION","matchFormat","SINGLES","competitionOrder",2,"hasGroups","N","status","SCHEDULED"))).andExpect(status().isOk());
        mvc.perform(body(post(tournamentPath(t)+"/competitions").with(admin()),Map.of("competitionName","Bad","competitionType","INVALID","matchFormat","SINGLES","competitionOrder",1,"hasGroups","N","status","SCHEDULED"))).andExpect(status().isBadRequest());
        mvc.perform(delete("/api/v1/competitions/"+id).with(admin())).andExpect(status().isOk());
    }
}
