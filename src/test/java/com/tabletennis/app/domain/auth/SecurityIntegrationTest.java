package com.tabletennis.app.domain.auth;
import com.tabletennis.app.ApiIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import java.time.Instant;
import java.util.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class SecurityIntegrationTest extends ApiIntegrationTest {
    @Autowired JwtEncoder encoder;
    @Autowired AuthSessionRepository sessions;
    @Test void expiredAccessIsRejected() throws Exception {
        var u=user("Expired"); AuthSession s=new AuthSession(); s.setSessionId(UUID.randomUUID()); s.setUserId(u.getUserId());
        s.setRefreshHash("a".repeat(64)); s.setExpiresAt(Instant.now().plusSeconds(1000)); sessions.saveAndFlush(s);
        var claims=JwtClaimsSet.builder().issuer("table-tennis-api").subject(u.getUserId().toString()).issuedAt(Instant.now().minusSeconds(8000))
            .expiresAt(Instant.now().minusSeconds(4000)).claim("sid",s.getSessionId().toString()).claim("role","USER").build();
        var token=encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
        mvc.perform(get("/api/v1/notices").header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }
    @Test void corsAndSwagger() throws Exception {
        mvc.perform(options("/api/v1/notices").header("Origin","http://localhost:3000").header("Access-Control-Request-Method","POST").header("Access-Control-Request-Headers","authorization"))
            .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin","http://localhost:3000"));
        mvc.perform(options("/api/v1/notices").header("Origin","https://untrusted.example").header("Access-Control-Request-Method","POST")).andExpect(status().isForbidden());
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(jsonPath("$.openapi").exists());
    }
    @Test void adminWritesRequireAuthenticationAcrossDomains() throws Exception {
        for(String path:List.of("/api/v1/notices","/api/v1/boards","/api/v1/tournaments","/api/v1/tournaments/2026/1/participants",
            "/api/v1/tournaments/2026/1/competitions","/api/v1/competitions/1/groups","/api/v1/groups/1/participants","/api/v1/matches",
            "/api/v1/matches/1/sets","/api/v1/matches/1/finalize")) {
            mvc.perform(body(post(path),Map.of())).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        }
    }
}
