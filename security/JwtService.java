package com.askroot.security;

import com.askroot.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.time.*;
import java.util.*;

@Service
public class JwtService {
    private final SecretKey key;
    private final AskRootProperties props;

    public JwtService(AskRootProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(props.getJwt().getSecret().getBytes());
    }

    public String access(User u) {
        return Jwts.builder()
            .issuer(props.getJwt().getIssuer())
            .subject(u.getId().toString())
            .claim("email", u.getEmail())
            .claim("role", u.getRole().name())
            .claim("typ", "access")
            .issuedAt(new Date())
            .expiration(Date.from(Instant.now().plus(props.getJwt().getAccessTtl())))
            .signWith(key).compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).requireIssuer(props.getJwt().getIssuer())
            .build().parseSignedClaims(token).getPayload();
    }
}
