package com.tabletennis.app.domain.board;
import com.tabletennis.app.ApiIntegrationTest;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;
import java.util.*;

class BoardIntegrationTest extends ApiIntegrationTest {
    @Test void crudAndCommentCascade() throws Exception {
        var u=user("Writer");
        int id=created("/api/v1/boards",Map.of("boardTitle","Board","boardContent","Content","boardWriter","Admin")).get("boardId").asInt();
        mvc.perform(get("/api/v1/boards/"+id)).andExpect(status().isOk()).andExpect(jsonPath("$.data.boardTitle").value("Board"));
        mvc.perform(get("/api/v1/boards?keyword=Board")).andExpect(jsonPath("$.meta.total").value(1));
        mvc.perform(body(put("/api/v1/boards/"+id).with(admin()),Map.of("boardTitle","Updated","boardContent","Content","boardWriter","Admin"))).andExpect(status().isOk());
        mvc.perform(body(post("/api/v1/boards/"+id+"/comments").with(member(u.getUserId())),Map.of("commentContent","Reply"))).andExpect(status().isCreated());
        mvc.perform(delete("/api/v1/boards/"+id).with(admin())).andExpect(status().isOk());
        mvc.perform(get("/api/v1/boards/"+id)).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/boards/"+id+"/comments")).andExpect(status().isNotFound());
    }
}
