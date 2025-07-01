package com.jetam6.ArcheusService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET = "mojevelmitajomneh3slo123456789012345"; // 32 znakov
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    public static String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15 min
                .signWith(SECRET_KEY)
                .compact();
    }

    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token);
            return true; // token je platný
        } catch (Exception e) {
            return false; // token je neplatný
        }
    }

    public static String generateRefreshToken(String email) {
        Instant now = Instant.now();
        Instant expirationTime = now.plus(7, ChronoUnit.DAYS); // 7 dní dopredu

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expirationTime))
                .signWith(SECRET_KEY)
                .compact();
    }

    public static String extractEmail(String token) {
        Claims claims = extractAllClaimsStatic(token);
        return claims.getSubject();
    }

    public String extractUsername(String token) {
        String username = extractAllClaims(token).getSubject();
        System.out.println(">>> JwtService.extractUsername: " + username);
        return username;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        boolean valid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        System.out.println(">>> JwtService.isTokenValid: " + valid);
        return valid;
    }

    private boolean isTokenExpired(String token) {
        boolean expired = extractAllClaims(token).getExpiration().before(new Date());
        System.out.println(">>> JwtService.isTokenExpired: " + expired);
        return expired;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private static Claims extractAllClaimsStatic(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
