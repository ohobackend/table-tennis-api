package com.tabletennis.app.config;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.tabletennis.app.domain.auth.*;
import com.tabletennis.app.domain.user.*;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
@Configuration
public class JwtConfig {
    @Bean SecretKey jwtKey(@Value("${jwt.secret}") String secret) {
        if(secret.getBytes(StandardCharsets.UTF_8).length<32) throw new IllegalArgumentException("JWT_SECRET must contain at least 32 UTF-8 bytes");
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256");
    }
    @Bean JwtEncoder jwtEncoder(SecretKey key) { return new NimbusJwtEncoder(new ImmutableSecret<>(key)); }
    @Bean JwtDecoder jwtDecoder(SecretKey key,AuthSessionRepository sessions,UserRepository users) {
        NimbusJwtDecoder decoder=NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        OAuth2TokenValidator<Jwt> sessionValidator=jwt->{
            try {
                AuthSession s=sessions.findById(UUID.fromString(jwt.getClaimAsString("sid"))).orElseThrow();
                User u=users.findById(s.getUserId()).orElseThrow();
                if(!s.isRevoked() && s.getExpiresAt().isAfter(Instant.now()) && jwt.getSubject().equals(u.getUserId().toString()) && u.getRole().name().equals(jwt.getClaimAsString("role")))
                    return OAuth2TokenValidatorResult.success();
            } catch(RuntimeException ignored) {}
            return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"));
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer("table-tennis-api"),sessionValidator));
        return decoder;
    }
}
