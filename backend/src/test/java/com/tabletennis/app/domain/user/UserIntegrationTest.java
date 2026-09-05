package com.tabletennis.app.domain.user;
import com.tabletennis.app.ApiIntegrationTest;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;
import java.util.*;

class UserIntegrationTest extends ApiIntegrationTest {
    @Test void privacySearchAndOwnership() throws Exception {
        var u=user("Player"); var other=user("Other");
        mvc.perform(get("/api/v1/users/"+u.getUserId())).andExpect(status().isOk())
            .andExpect(jsonPath("$.data.password").doesNotExist()).andExpect(jsonPath("$.data.email").doesNotExist()).andExpect(jsonPath("$.data.phoneNumber").doesNotExist());
        mvc.perform(get("/api/v1/users?keyword=Player&club=TestClub&gender=m")).andExpect(jsonPath("$.meta.total").value(1));
        var request=Map.of("userName","Changed","realName","Changed","clubName","NewClub","gender","F","role","ADMIN");
        mvc.perform(body(put("/api/v1/users/"+u.getUserId()).with(member(other.getUserId())),request)).andExpect(status().isForbidden());
        mvc.perform(body(put("/api/v1/users/"+u.getUserId()).with(member(u.getUserId())),request)).andExpect(status().isOk()).andExpect(jsonPath("$.data.userName").value("Changed"));
        assertThat(users.findById(u.getUserId()).orElseThrow().getRole()).isEqualTo(com.tabletennis.app.common.util.Role.USER);
        mvc.perform(get("/api/v1/users?gender=X")).andExpect(status().isBadRequest());
    }
}
