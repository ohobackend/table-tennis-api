package com.tabletennis.app;
import com.fasterxml.jackson.databind.*;
import com.tabletennis.app.domain.user.*;
import com.tabletennis.app.common.util.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.*;
@SpringBootTest @AutoConfigureMockMvc @Transactional
public abstract class ApiIntegrationTest {
    private static PostgreSQLContainer<?> postgres;
    @DynamicPropertySource static void database(DynamicPropertyRegistry r) {
        String external=System.getenv("TEST_DATABASE_URL");
        if(external==null) {
            if(postgres==null) { postgres=new PostgreSQLContainer<>("postgres:15-alpine"); postgres.start(); }
            r.add("spring.datasource.url",postgres::getJdbcUrl); r.add("spring.datasource.username",postgres::getUsername); r.add("spring.datasource.password",postgres::getPassword);
        } else {
            r.add("spring.datasource.url",()->external); r.add("spring.datasource.username",()->System.getenv("TEST_DATABASE_USERNAME")); r.add("spring.datasource.password",()->System.getenv("TEST_DATABASE_PASSWORD"));
        }
        r.add("jwt.secret",()->"integration-test-only-secret-at-least-32-bytes");
    }
    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectMapper json;
    @Autowired protected UserRepository users;
    @Autowired protected PasswordEncoder passwords;
    protected RequestPostProcessor admin() { return jwt().jwt(j->j.subject("999").claim("role","ADMIN")).authorities(new SimpleGrantedAuthority("ROLE_ADMIN")); }
    protected RequestPostProcessor member(int id) { return jwt().jwt(j->j.subject(Integer.toString(id)).claim("role","USER")).authorities(new SimpleGrantedAuthority("ROLE_USER")); }
    protected User user(String name) {
        User u=new User(); u.setUserName(name); u.setRealName(name); u.setEmail(UUID.randomUUID().toString().substring(0,12)+"@test.com"); u.setPassword(passwords.encode("password123")); u.setRole(Role.USER); u.setGender("M"); u.setClubName("TestClub"); return users.saveAndFlush(u);
    }
    protected MockHttpServletRequestBuilder body(MockHttpServletRequestBuilder builder,Object value) throws Exception { return builder.contentType("application/json").content(json.writeValueAsBytes(value)); }
    protected JsonNode created(String path,Object value) throws Exception {
        var result=mvc.perform(body(post(path).with(admin()),value)).andExpect(status().isCreated()).andExpect(jsonPath("$.success").value(true)).andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("data");
    }
    protected JsonNode tournament() throws Exception {
        return created("/api/v1/tournaments",Map.of("tournamentYear",2026,"tournamentName","League","startDate","2026-04-10","endDate","2026-04-12","entryFee",30000));
    }
    protected String tournamentPath(JsonNode t) { return "/api/v1/tournaments/2026/"+t.get("tournamentId").asInt(); }
    protected JsonNode competition(JsonNode t,boolean groups) throws Exception {
        return created(tournamentPath(t)+"/competitions",Map.of("competitionName","Final","competitionType","ROUND_ROBIN","matchFormat","SINGLES","competitionOrder",1,"hasGroups",groups?"Y":"N","playersPerGroup",4,"status","SCHEDULED"));
    }
    protected void register(JsonNode t,User u) throws Exception { created(tournamentPath(t)+"/participants",Map.of("userId",u.getUserId())); }
    protected JsonNode match(User a,User b) throws Exception {
        var t=tournament(); register(t,a); register(t,b); var c=competition(t,false);
        return created("/api/v1/matches",Map.of("competitionId",c.get("competitionId").asInt(),"matchRound",0,
            "scheduledAt",java.time.OffsetDateTime.now(java.time.ZoneId.of("Asia/Seoul")).toString(),
            "participants",List.of(Map.of("userId",a.getUserId(),"side","SIDE_A","participantOrder",1),Map.of("userId",b.getUserId(),"side","SIDE_B","participantOrder",1))));
    }
    protected Map<String,Object> winningSets() { return Map.of("sets",List.of(
        Map.of("setNumber",1,"sideAPoint",11,"sideBPoint",8),Map.of("setNumber",2,"sideAPoint",12,"sideBPoint",10),Map.of("setNumber",3,"sideAPoint",11,"sideBPoint",5))); }
}
