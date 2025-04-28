package com.jetam6.Archeus;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;


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
                .signWith(SECRET_KEY) // podpisovanie tokenu
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
	        Claims claims = Jwts.parserBuilder()
	                .setSigningKey(SECRET_KEY)
	                .build()
	                .parseClaimsJws(token)
	                .getBody();

	        return claims.getSubject(); // vracia email uložený v "sub" poli
	    }
	}

