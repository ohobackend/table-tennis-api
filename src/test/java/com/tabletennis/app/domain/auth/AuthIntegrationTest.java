package com.tabletennis.app.domain.auth;
import com.tabletennis.app.ApiIntegrationTest;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import java.time.Instant;
class AuthIntegrationTest extends ApiIntegrationTest {
    @Autowired AuthSessionRepository sessions;
    @Test void signupLoginRotationAndLogout() throws Exception {
        mvc.perform(body(post("/api/v1/auth/signup"),Map.of("email","new@test.com","password","password123","userName","New","realName","New","role","ADMIN")))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.data.password").doesNotExist()).andExpect(jsonPath("$.data.email").doesNotExist());
        var u=users.findOne(com.tabletennis.app.common.util.Queries.eq("email","new@test.com")).orElseThrow();
        assertThat(u.getRole()).isEqualTo(com.tabletennis.app.common.util.Role.USER);
        assertThat(u.getPassword()).isNotEqualTo("password123"); assertThat(passwords.matches("password123",u.getPassword())).isTrue();
        mvc.perform(body(post("/api/v1/auth/signup"),Map.of("email","NEW@test.com","password","password123","userName","New","realName","New"))).andExpect(status().isConflict());
        var login=mvc.perform(body(post("/api/v1/auth/login"),Map.of("email","new@test.com","password","password123"))).andExpect(status().isOk()).andReturn();
        var tokens=json.readTree(login.getResponse().getContentAsString()).get("data");
        String access=tokens.get("accessToken").asText(),refresh=tokens.get("refreshToken").asText();
        mvc.perform(body(post("/api/v1/notices").header("Authorization","Bearer "+access),Map.of("noticeTitle","A","noticeContents","B","noticeWriter","C"))).andExpect(status().isForbidden());
        var rotation=mvc.perform(body(post("/api/v1/auth/refresh"),Map.of("refreshToken",refresh))).andExpect(status().isOk()).andReturn();
        String rotated=json.readTree(rotation.getResponse().getContentAsString()).at("/data/refreshToken").asText();
        assertThat(rotated).isNotEqualTo(refresh);
        mvc.perform(body(post("/api/v1/auth/refresh"),Map.of("refreshToken",refresh))).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/logout").header("Authorization","Bearer "+access)).andExpect(status().isOk());
        mvc.perform(post("/api/v1/auth/logout").header("Authorization","Bearer "+access)).andExpect(status().isUnauthorized());
        mvc.perform(body(post("/api/v1/auth/refresh"),Map.of("refreshToken",rotated))).andExpect(status().isUnauthorized());
    }
    @Test void badCredentialsTokensAndExpiry() throws Exception {
        var u=user("Player");
        mvc.perform(body(post("/api/v1/auth/login"),Map.of("email",u.getEmail(),"password","wrong"))).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/notices").header("Authorization","Bearer malformed")).andExpect(status().isUnauthorized());
        mvc.perform(body(post("/api/v1/auth/refresh"),Map.of("refreshToken","bogus"))).andExpect(status().isUnauthorized());
        mvc.perform(body(post("/api/v1/auth/signup"),Map.of("email","bad","password","short","userName","","realName",""))).andExpect(status().isBadRequest());
        var result=mvc.perform(body(post("/api/v1/auth/login"),Map.of("email",u.getEmail(),"password","password123"))).andReturn();
        var t=json.readTree(result.getResponse().getContentAsString()).get("data");
        sessions.findAll().forEach(s->s.setExpiresAt(Instant.now().minusSeconds(1))); sessions.flush();
        mvc.perform(post("/api/v1/auth/logout").header("Authorization","Bearer "+t.get("accessToken").asText())).andExpect(status().isUnauthorized());
        mvc.perform(body(post("/api/v1/auth/refresh"),Map.of("refreshToken",t.get("refreshToken").asText()))).andExpect(status().isUnauthorized());
    }
}
