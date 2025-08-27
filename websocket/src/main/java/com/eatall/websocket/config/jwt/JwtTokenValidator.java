package com.eatall.websocket.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class JwtTokenValidator implements InitializingBean {

    private Key key;
    private final String secretKey;

    public JwtTokenValidator(@Value("${jwt.secret}") String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public void afterPropertiesSet() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // JWT를 Authentication 객체로 변환하는 메서드
    public Authentication getAuthentication(String accessToken) {
        // access token에서 claim 가져옴
        Claims claims = parseClaims(accessToken);

        List<SimpleGrantedAuthority> authorities = Arrays.stream(
                        claims.get("auth", String.class).split(","))
                .map(SimpleGrantedAuthority::new)
                .toList();

        User user = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(user, accessToken, authorities);
    }

    // 토큰 정보를 검증하는 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
            throw new RuntimeException("error", e);
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
            throw new RuntimeException("error", e);
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
            throw new RuntimeException("error", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
            throw new RuntimeException("error", e);
        }
    }

    // accessToken
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (ExpiredJwtException e) {

            return e.getClaims();
        }
    }

    public Long extractUserIdFromAccessToken(String accessToken){
        if (StringUtils.hasText(accessToken) && accessToken.startsWith("Bearer ")) {
            return Long.parseLong(parseClaims(accessToken.substring(7)).getSubject());
        }
        return null;
    }
}