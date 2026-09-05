package com.tabletennis.app.domain.comment;
import com.tabletennis.app.ApiIntegrationTest;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;
import java.util.*;

class CommentIntegrationTest extends ApiIntegrationTest {
    @Test void authorAndAdminOnly() throws Exception {
        var owner=user("Owner"); var other=user("Other");
        int board=created("/api/v1/boards",Map.of("boardTitle","Board","boardContent","Content","boardWriter","Admin")).get("boardId").asInt();
        var result=mvc.perform(body(post("/api/v1/boards/"+board+"/comments").with(member(owner.getUserId())),Map.of("commentContent","Hello","commentWriter","Spoofed")))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.data.commentWriter").value("Owner")).andReturn();
        int id=json.readTree(result.getResponse().getContentAsString()).at("/data/commentId").asInt();
        mvc.perform(body(put("/api/v1/comments/"+id).with(member(other.getUserId())),Map.of("commentContent","Bad"))).andExpect(status().isForbidden());
        mvc.perform(body(put("/api/v1/comments/"+id).with(member(owner.getUserId())),Map.of("commentContent","Updated"))).andExpect(status().isOk());
        mvc.perform(delete("/api/v1/comments/"+id).with(member(other.getUserId()))).andExpect(status().isForbidden());
        mvc.perform(delete("/api/v1/comments/"+id).with(admin())).andExpect(status().isOk());
    }
}
