package com.tabletennis.app.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tabletennis.app.common.response.ApiResponse;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.http.HttpMethod;
@Configuration @EnableMethodSecurity
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean SecurityFilterChain security(HttpSecurity http,ObjectMapper json) throws Exception {
        JwtGrantedAuthoritiesConverter roles=new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("role"); roles.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter=new JwtAuthenticationConverter(); converter.setJwtGrantedAuthoritiesConverter(roles);
        org.springframework.security.web.AuthenticationEntryPoint unauthorized=(req,res,e)->{
            res.setStatus(401); res.setContentType("application/json;charset=UTF-8");
            json.writeValue(res.getOutputStream(),ApiResponse.fail("UNAUTHORIZED","인증이 필요합니다."));
        };
        org.springframework.security.web.access.AccessDeniedHandler forbidden=(req,res,e)->{
            res.setStatus(403); res.setContentType("application/json;charset=UTF-8");
            json.writeValue(res.getOutputStream(),ApiResponse.fail("FORBIDDEN","권한이 없습니다."));
        };
        return http.csrf(c->c.disable()).cors(c->{})
            .sessionManagement(c->c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(c->c
                .requestMatchers("/api/v1/auth/signup","/api/v1/auth/login","/api/v1/auth/refresh","/actuator/health","/swagger-ui.html","/swagger-ui/**","/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/v1/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
                .requestMatchers("/api/v1/**").authenticated().anyRequest().denyAll())
            .exceptionHandling(c->c.authenticationEntryPoint(unauthorized).accessDeniedHandler(forbidden))
            .oauth2ResourceServer(c->c.jwt(j->j.jwtAuthenticationConverter(converter)).authenticationEntryPoint(unauthorized).accessDeniedHandler(forbidden))
            .build();
    }
}
