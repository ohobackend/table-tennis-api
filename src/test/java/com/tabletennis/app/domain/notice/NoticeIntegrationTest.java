package com.tabletennis.app.domain.notice;
import com.tabletennis.app.ApiIntegrationTest;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;
import java.util.*;

class NoticeIntegrationTest extends ApiIntegrationTest {
    @Test void crudSearchAndHits() throws Exception {
        int id=created("/api/v1/notices",Map.of("noticeTitle","League","noticeContents","Details","noticeWriter","Admin")).get("noticeNum").asInt();
        mvc.perform(get("/api/v1/notices/"+id)).andExpect(status().isOk()).andExpect(jsonPath("$.data.hitNum").value(1)).andExpect(jsonPath("$.data.noticeTitle").value("League"));
        mvc.perform(get("/api/v1/notices/"+id)).andExpect(jsonPath("$.data.hitNum").value(2));
        mvc.perform(get("/api/v1/notices?keyword=League&page=1&size=1")).andExpect(jsonPath("$.meta.total").value(1));
        mvc.perform(body(put("/api/v1/notices/"+id).with(admin()),Map.of("noticeTitle","Updated","noticeContents","New","noticeWriter","Admin"))).andExpect(status().isOk()).andExpect(jsonPath("$.data.noticeTitle").value("Updated"));
        mvc.perform(delete("/api/v1/notices/"+id).with(admin())).andExpect(status().isOk());
        mvc.perform(get("/api/v1/notices/"+id)).andExpect(status().isNotFound()).andExpect(jsonPath("$.error.code").value("NOTICE_NOT_FOUND"));
    }
    @Test void validatesAndEnforcesRoles() throws Exception {
        mvc.perform(body(post("/api/v1/notices"),Map.of())).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(body(post("/api/v1/notices").with(member(1)),Map.of("noticeTitle","A","noticeContents","B","noticeWriter","C"))).andExpect(status().isForbidden()).andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mvc.perform(body(post("/api/v1/notices").with(admin()),Map.of())).andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        mvc.perform(get("/api/v1/notices?page=0")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/notices?sort=password,desc")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/notices/no-number")).andExpect(status().isBadRequest());
    }
}
