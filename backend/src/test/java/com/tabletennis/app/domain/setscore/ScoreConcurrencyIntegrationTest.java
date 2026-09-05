package com.tabletennis.app.domain.setscore;
import com.tabletennis.app.ApiIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.*;
import com.tabletennis.app.domain.competition.CompetitionRepository;
import java.util.*;
import java.util.concurrent.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;
@Transactional(propagation=Propagation.NOT_SUPPORTED)
class ScoreConcurrencyIntegrationTest extends ApiIntegrationTest {
    @Autowired CompetitionRepository competitions;
    @Test void concurrentSameSetHasOneWinnerAndOneConflict() throws Exception {
        var a=user("ConcurrentA"); var b=user("ConcurrentB"); var m=match(a,b);
        int id=m.get("matchId").asInt(); var c=competitions.findById(m.get("competitionId").asInt()).orElseThrow();
        var pool=Executors.newFixedThreadPool(2); var start=new CountDownLatch(1);
        try {
            Callable<Integer> attempt=()->{
                start.await();
                return mvc.perform(body(post("/api/v1/matches/"+id+"/sets").with(admin()),
                    Map.of("sets",List.of(Map.of("setNumber",1,"sideAPoint",11,"sideBPoint",8)))))
                    .andReturn().getResponse().getStatus();
            };
            Future<Integer> first=pool.submit(attempt),second=pool.submit(attempt); start.countDown();
            assertThat(List.of(first.get(20,TimeUnit.SECONDS),second.get(20,TimeUnit.SECONDS))).containsExactlyInAnyOrder(201,409);
            mvc.perform(get("/api/v1/matches/"+id+"/sets")).andExpect(jsonPath("$.data.length()").value(1));
            mvc.perform(get("/api/v1/matches/"+id)).andExpect(jsonPath("$.data.totalSets").value(1)).andExpect(jsonPath("$.data.sideASets").value(1));
        } finally {
            pool.shutdownNow();
            mvc.perform(delete("/api/v1/tournaments/"+c.getTournamentYear()+"/"+c.getTournamentId()).with(admin())).andExpect(status().isOk());
            users.deleteById(a.getUserId()); users.deleteById(b.getUserId());
        }
    }
}
