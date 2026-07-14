package com.zsecurity.demo.services;

import com.zsecurity.demo.entity.Users;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final String secretKey = System.getenv().getOrDefault("JWT_SECRET",
            "wrewtrsgdfgvbnmuytrtddsghjkhgdfsfghjkhgfghjgffghjkgffghjhgfdhgjkhgfdersdtfghbjnmedfgvbhn");

    public SecretKey getSecreteKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Users users) {
        try {
            return Jwts.builder()
                    .subject(users.getEmail())
                    .claim("email", users.getEmail())
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 5))
                    .signWith(getSecreteKey())
                    .compact();
        } catch (JwtException e) {
            throw new JwtException(e.getLocalizedMessage());
        }
    }

    public String generateRefreshToken(Users users) {
        try {
            return Jwts.builder()
                    .subject(users.getEmail())
                    .claim("email", users.getEmail())
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30))
                    .signWith(getSecreteKey())
                    .compact();
        } catch (JwtException e) {
            throw new JwtException(e.getLocalizedMessage());
        }
    }

    public String getEmailByToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSecreteKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (JwtException e) {
            throw new JwtException(e.getLocalizedMessage());
        }
    }

    public boolean isExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSecreteKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}