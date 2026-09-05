package com.tabletennis.app.domain.auth;
import com.tabletennis.app.domain.auth.dto.AuthRequests.*;
import com.tabletennis.app.domain.user.*;
import com.tabletennis.app.domain.user.dto.*;
import com.tabletennis.app.domain.user.mapper.UserMapper;
import com.tabletennis.app.common.exception.*;
import com.tabletennis.app.common.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import java.time.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
@Service @RequiredArgsConstructor
public class AuthService {
    private final UserRepository users; private final AuthSessionRepository sessions;
    private final PasswordEncoder passwords; private final JwtEncoder encoder; private final UserMapper mapper;
    @Value("${jwt.access-seconds}") private long accessSeconds;
    @Value("${jwt.refresh-seconds}") private long refreshSeconds;
    @Transactional
    public UserProfile signup(Signup r) {
        if(r.password().getBytes(StandardCharsets.UTF_8).length>72) throw new ApiException(ErrorCode.VALIDATION_ERROR,"비밀번호는 UTF-8 72바이트 이하입니다.");
        String email=r.email().toLowerCase(Locale.ROOT);
        if(users.count(Queries.eq("email",email))>0) throw new ApiException(ErrorCode.CONFLICT);
        User u=new User(); u.setEmail(email); u.setPassword(passwords.encode(r.password()));
        u.setUserName(r.userName()); u.setRealName(r.realName()); u.setRole(Role.USER);
        return mapper.profile(users.saveAndFlush(u));
    }
    @Transactional
    public Tokens login(Login r) {
        User u=users.findOne(Queries.eq("email",r.email().toLowerCase(Locale.ROOT))).orElseThrow(()->new ApiException(ErrorCode.UNAUTHORIZED));
        if(!passwords.matches(r.password(),u.getPassword())) throw new ApiException(ErrorCode.UNAUTHORIZED);
        AuthSession s=new AuthSession(); s.setSessionId(UUID.randomUUID()); s.setUserId(u.getUserId());
        s.setExpiresAt(Instant.now().plusSeconds(refreshSeconds));
        return issue(u,s);
    }
    @Transactional
    public Tokens refresh(String raw) {
        AuthSession s=sessions.findForRefresh(hash(raw)).orElseThrow(()->new ApiException(ErrorCode.UNAUTHORIZED));
        if(s.isRevoked() || !s.getExpiresAt().isAfter(Instant.now())) throw new ApiException(ErrorCode.UNAUTHORIZED);
        return issue(users.findById(s.getUserId()).orElseThrow(()->new ApiException(ErrorCode.UNAUTHORIZED)),s);
    }
    @Transactional
    public void logout(Jwt jwt) {
        AuthSession s=sessions.findById(UUID.fromString(jwt.getClaimAsString("sid"))).orElseThrow(()->new ApiException(ErrorCode.UNAUTHORIZED));
        s.setRevoked(true);
    }
    private Tokens issue(User u,AuthSession s) {
        String raw=UUID.randomUUID().toString()+UUID.randomUUID(); s.setRefreshHash(hash(raw)); sessions.saveAndFlush(s);
        Instant now=Instant.now();
        JwtClaimsSet claims=JwtClaimsSet.builder().issuer("table-tennis-api").subject(u.getUserId().toString())
            .issuedAt(now).expiresAt(now.plusSeconds(accessSeconds)).id(UUID.randomUUID().toString())
            .claim("sid",s.getSessionId().toString()).claim("role",u.getRole().name()).build();
        String access=encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
        return new Tokens(access,raw,accessSeconds,"Bearer");
    }
    private String hash(String raw) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8))); }
        catch(java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
}
