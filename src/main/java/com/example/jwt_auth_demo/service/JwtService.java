package com.example.jwt_auth_demo.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtService {

    @Value("${jwt.secret}")
    String jwtSecret;

    @Value("${jwt.expiration}")
    long jwtExpiration;

    @Value("${jwt.issuer}")
    String jwtIssuer;

    @Value("${app.roles.admin}")
    String adminRole;

    @Value("${app.roles.user}")
    String userRole;

    public String generateToken(UserDetails userDetails) { // Accept UserDetails
        Map<String, Object> claims = new HashMap<>();

        List<String> roles = new ArrayList<>();

        if (userDetails.getUsername() != null && userDetails.getUsername().equals("admin")) {
            roles.add(adminRole);
        } else {
            roles.add(userRole);
        }

        claims.put("roles", roles);

        return createToken(claims, userDetails.getUsername()); // Use username from UserDetails
    }


    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .issuer(jwtIssuer)
                .expiration(expiryDate)
                .signWith(getSignKey())
                .compact();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser() // Use parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public List<String> extractRoles(String token) { // New method to extract roles
        Claims claims = extractAllClaims(token);
        return (List<String>) claims.get("roles"); // Or "authorities"
    }
}